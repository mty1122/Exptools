package com.mty.exptools.coordinator

import android.Manifest
import androidx.annotation.RequiresPermission
import com.mty.exptools.R
import com.mty.exptools.repository.ListRepository
import com.mty.exptools.repository.TickRepository
import com.mty.exptools.util.toMillisTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

object LiveUpdateCoordinator {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val listRepository = ListRepository()
    private val tickFlow = TickRepository.autoRefreshTicker

    val liveTaskFlow: Flow<LiveTask?> =
        combine(
            listRepository.observeAllSynDraft(),
            listRepository.observeAllPhotoDraft(),
            tickFlow
        ) { synDrafts, photoDrafts, _ ->

            val candidates = buildList {
                synDrafts.forEach { draft ->
                    if (!draft.isFinished && draft.completedAt != null) {
                        val idx = draft.currentStepIndex
                        val current = draft.steps.getOrNull(idx)
                        val currentTime = System.currentTimeMillis()
                        val remaining = current?.timer?.remaining(currentTime) ?: return@forEach
                        if (remaining <= 0L) return@forEach
                        val finish = currentTime + remaining
                        val title = finish.toMillisTime().toFormatString("HH:mm")
                        add(
                            LiveTask(
                                stableKey = "syn_${draft.materialName}",
                                title = title,
                                content = "${draft.materialName}\n${draft.conditionSummary}\n" +
                                        current.content,
                                smallIcon = R.drawable.icon_exp,
                                finishAtEpochMillis = finish,
                                remainingMillis = remaining
                            )
                        )
                    }
                }

                photoDrafts.forEach { draft ->
                    if (!draft.isFinished && draft.completedAt != null) {
                        val idx = draft.currentStepIndex
                        val current = draft.steps.getOrNull(idx)
                        val currentTime = System.currentTimeMillis()
                        val remaining = current?.timer?.remaining(currentTime) ?: return@forEach
                        if (remaining <= 0L) return@forEach
                        val finish = currentTime + remaining
                        val title = finish.toMillisTime().toFormatString("HH:mm")
                        add(
                            LiveTask(
                                stableKey = "photo_${draft.dbId}",
                                title = title,
                                content = "${draft.catalystName}\n${current.name} | " +
                                        "${draft.target.name} | ${draft.target.wavelengthNm} | " +
                                        draft.light.value,
                                smallIcon = R.drawable.icon_sun,
                                finishAtEpochMillis = finish,
                                remainingMillis = remaining
                            )
                        )
                    }
                }
            }

            candidates.minByOrNull { it.remainingMillis }
        }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun start() {
        scope.launch {
            var lastKey: String? = null
            liveTaskFlow.collect { task ->
                if (task == null) {
                    LiveUpdateNotifier.cancel()
                    lastKey = null
                }
                else if (lastKey != (task.stableKey + task.title)) {
                    LiveUpdateNotifier.showOrUpdate(task)
                    lastKey = task.stableKey + task.title
                }
            }
        }
    }
}