package com.mty.exptools.coordinator

import com.mty.exptools.domain.photo.PhotocatalysisDraft
import com.mty.exptools.repository.PhotoRepository
import com.mty.exptools.repository.TickRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

object AutoContinueCoordinator {
    private val photoRepo =  PhotoRepository()
    private val tickFlow = TickRepository.autoRefreshTicker
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val queue = Channel<List<PhotocatalysisDraft>>(capacity = Channel.CONFLATED)

    fun start() {
        scope.launch {
            launch {
                combine(photoRepo.observeAllPhotoDrafts(), tickFlow) { drafts, _ ->
                    drafts
                }.collect { drafts ->
                    queue.send(drafts)
                }
            }

            launch {
                for (drafts in queue) {
                    for (draft in drafts) {
                        if (shouldAutoContinue(draft)) performAutoContinue(draft)
                    }
                }
            }
        }
    }

    private fun shouldAutoContinue(draft: PhotocatalysisDraft): Boolean {
        if (draft.isFinished) return false
        if (draft.currentStepIndex <= 0) return false

        val current = draft.steps.getOrNull(draft.currentStepIndex) ?: return false
        val previous = draft.steps.getOrNull(draft.currentStepIndex - 1) ?: return false

        return !current.timer.isRunning() &&
                current.timer.neverStart() &&
                previous.name == current.name
    }

    private suspend fun performAutoContinue(draft: PhotocatalysisDraft) {
        val idx = draft.currentStepIndex
        val steps = draft.steps.toMutableList()
        val current = steps[idx]
        val newTimer = current.timer.start()

        val now = System.currentTimeMillis()
        var remaining = 0L
        for (index in idx until steps.size) {
            remaining += steps[index].timer.remaining()
        }
        val completedAt = now + remaining

        photoRepo.updateStepsTimerByIndex(
            dbId = draft.dbId,
            orderIndexes = listOf(idx),
            accumulatedMillis = newTimer.accumulatedMillis,
            startEpochMs = newTimer.startEpochMs
        )
        photoRepo.setCompletedAt(draft.dbId, completedAt)
    }
}