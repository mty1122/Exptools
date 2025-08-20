package com.mty.exptools.ui.home.center.list

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.exptools.domain.other.OtherDraft
import com.mty.exptools.domain.photo.PhotocatalysisDraft
import com.mty.exptools.domain.syn.SynthesisDraft
import com.mty.exptools.domain.test.TestDraft
import com.mty.exptools.repository.ListRepository
import com.mty.exptools.repository.TickRepository
import com.mty.exptools.ui.home.center.list.item.ItemOtherUiState
import com.mty.exptools.ui.home.center.list.item.ItemPhotoUiState
import com.mty.exptools.ui.home.center.list.item.ItemStatus
import com.mty.exptools.ui.home.center.list.item.ItemSynUiState
import com.mty.exptools.ui.home.center.list.item.ItemTestUiState
import com.mty.exptools.ui.home.center.list.item.ItemUiState
import com.mty.exptools.util.MillisTime
import com.mty.exptools.util.asString
import com.mty.exptools.util.toMillisTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class ListViewModel @Inject constructor(
    repo: ListRepository,
    tickRepo: TickRepository
) : ViewModel() {

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()


    // 每10秒钟触发一次的节拍
    private val tickFlow = tickRepo.autoRefreshTicker
    // 手动刷新触发器（支持背压丢弃：最新的一次即可）
    private val manualRefresh = MutableSharedFlow<Unit>(
        replay = 0, extraBufferCapacity = 1
    )
    // 刷新触发流：首次启动即触发一次（onStart）
    private val refreshTrigger: Flow<Unit> =
        merge(tickFlow, manualRefresh).onStart { emit(Unit) }

    // Syn数据库流
    private val synDraftFlow = repo.observeAllSynDraft()
    // Photo数据库流
    private val photoDraftFlow = repo.observeAllPhotoDraft()
    // Test数据库流
    private val testDraftFlow = repo.observeAllTestDraft()
    // Other数据库流
    private val otherDraftFlow = repo.observeAllOtherDraft()

    // 任一触发（DB变化 / 定时 / 手动），都重算 UIState
    val itemUiStateList: StateFlow<List<ItemUiState>> =
        combine(synDraftFlow, photoDraftFlow, testDraftFlow, otherDraftFlow, refreshTrigger
        ) { synDrafts, photoDrafts, testDrafts, otherDrafts, _ ->
            val synUiStates = synDrafts.map { it.toItemSynUiState() }
            val photoUiStates = photoDrafts.map { it.toItemPhotoUiState() }
            val testUiStates = testDrafts.map { it.toItemTestUiState() }
            val otherUiStates = otherDrafts.map { it.toItemOtherUiState() }

            val itemUiStates = synUiStates + photoUiStates + testUiStates + otherUiStates
            itemUiStates.sortedBy { item ->
                if (item.status == ItemStatus.STATUS_COMPLETE)
                    2 * System.currentTimeMillis() + item.rightTime.millis
                else
                    item.rightTime.millis
            }.mapIndexed { index, itemUiState ->
                when (itemUiState) {
                    is ItemSynUiState -> itemUiState.copy(listItemId = index)
                    is ItemPhotoUiState -> itemUiState.copy(listItemId = index)
                    is ItemOtherUiState -> itemUiState.copy(listItemId = index)
                    is ItemTestUiState -> itemUiState.copy(listItemId = index)
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    // 下拉手动刷新：立刻重算一次
    fun refresh() {
        if (_refreshing.value) return
        viewModelScope.launch {
            _refreshing.value = true

            val start = SystemClock.elapsedRealtime()
            val old = itemUiStateList.value  // 抓当前引用

            try {
                manualRefresh.emit(Unit)         // 触发一次重算

                withTimeout(2_000) {
                    itemUiStateList
                        .dropWhile { it == old }   // 只要还是同一个实例就继续丢
                        .first()                    // 完成发射刷新成功
                }

                // 200ms保底，避免进度圈来不及收回去
                val elapsed = SystemClock.elapsedRealtime() - start
                if (elapsed < 200) {
                    delay(200 - elapsed)
                }
            } finally {
                _refreshing.value = false
            }
        }
    }

    private fun SynthesisDraft.toItemSynUiState(): ItemSynUiState {
        val draft = this
        // 已经结束的，计算完成了多久
        val time = if (draft.isFinished) {
            draft.completedAt?.let {
                (System.currentTimeMillis() - it).toMillisTime()
            } ?: MillisTime(0)
            // 未结束的计算当前步骤剩余时间
        } else {
            draft.steps[draft.currentStepIndex].timer.remaining()
                .toMillisTime()
        }
        // 进度 = 已经完成的步骤的进度 + 当前步骤的进度
        val currentProgress = draft.steps[draft.currentStepIndex].timer.progress()
        val progress = draft.currentStepIndex / draft.steps.size.toFloat() +
                1f / draft.steps.size * currentProgress
        val status = when {
            draft.isFinished -> ItemStatus.STATUS_COMPLETE
            draft.steps.getOrNull(draft.currentStepIndex)?.timer
                ?.isRunning() == false -> ItemStatus.STATUS_PAUSE
            else -> ItemStatus.STATUS_START
        }
        val info = buildString {
            if (status == ItemStatus.STATUS_COMPLETE) {
                append(draft.conditionSummary)
            }
            else {
                val targetStep = draft.steps[draft.currentStepIndex].content
                val nextStep = draft.steps.getOrNull(draft.currentStepIndex + 1)?.content ?: ""
                append(targetStep)
                if (nextStep.isNotBlank()) {
                    append("\n${nextStep}")
                }
            }
        }
        return ItemSynUiState(
            listItemId = 0, // 交由上级统一排序
            materialName = draft.materialName,
            title = draft.materialName,
            info = info,
            progress = progress,
            rightTime = time,
            status = status
        )
    }

    private fun PhotocatalysisDraft.toItemPhotoUiState(): ItemPhotoUiState {
        val draft = this
        // 已经结束的，计算完成了多久
        val time = if (draft.isFinished) {
            draft.completedAt?.let {
                (System.currentTimeMillis() - it).toMillisTime()
            } ?: MillisTime(0)
            // 未结束的计算当前步骤剩余时间
        } else {
            draft.steps[draft.currentStepIndex].timer.remaining()
                .toMillisTime()
        }
        // 进度 = 已经完成的步骤的进度 + 当前步骤的进度
        val currentProgress = draft.steps[draft.currentStepIndex].timer.progress()
        val progress = draft.currentStepIndex / draft.steps.size.toFloat() +
                1f / draft.steps.size * currentProgress
        // 剩余时间
        var remaining = 0L
        steps.forEach { remaining += it.timer.remaining() }
        val remainTime = remaining.toMillisTime().toTime()

        val status = when {
            draft.isFinished -> ItemStatus.STATUS_COMPLETE
            draft.steps.getOrNull(draft.currentStepIndex)?.timer
                ?.isRunning() == false -> ItemStatus.STATUS_PAUSE
            else -> ItemStatus.STATUS_START
        }

        val info = buildString {
            append(draft.target.name)
            if (draft.target.wavelengthNm.isNotBlank())
                append(" | ${draft.target.wavelengthNm}nm")
            if (draft.light.value.isNotBlank())
                append(" | ${draft.light.value}")
            if (status != ItemStatus.STATUS_COMPLETE) {
                append("\n预计结束于 ${remainTime.stringValue} ")
                append(remainTime.unit.asString())
                append("后")
            } else if (draft.performanceList.isNotEmpty()) {
                append("\n性能 ")
                append(draft.performanceList.joinToString(" "))
            }
        }

        return ItemPhotoUiState(
            listItemId = 0, // 交由上级统一排序
            dbId = draft.dbId,
            title = draft.catalystName,
            info = info,
            progress = progress,
            rightTime = time,
            status = when {
                draft.isFinished -> ItemStatus.STATUS_COMPLETE
                draft.steps.getOrNull(draft.currentStepIndex)?.timer
                    ?.isRunning() == false -> ItemStatus.STATUS_PAUSE
                else -> ItemStatus.STATUS_START
            }
        )
    }

    private fun TestDraft.toItemTestUiState(): ItemTestUiState {
        val draft = this
        val time = (System.currentTimeMillis() - draft.startAt)
        val info = buildString {
            if (draft.summary.isNotBlank()) {
                append(draft.summary)
                append(" | ")
            }
            append(draft.startAt.toMillisTime().toDate())
        }
        return ItemTestUiState(
            listItemId = 0,
            dbId = draft.dbId,
            title = draft.materialName,
            info = info,
            rightTime = time.absoluteValue.toMillisTime(),
            status = if (time > 0) ItemStatus.STATUS_COMPLETE else ItemStatus.STATUS_START
        )
    }

    private fun OtherDraft.toItemOtherUiState(): ItemOtherUiState {
        val draft = this
        val time = (System.currentTimeMillis() - draft.completedAt)
        val info = buildString {
            if (draft.summary.isNotBlank()) {
                append(draft.summary)
                append(" | ")
            }
            append(draft.completedAt.toMillisTime().toDate())
        }
        return ItemOtherUiState(
            listItemId = 0,
            dbId = draft.dbId,
            title = draft.taskName,
            info = info,
            rightTime = time.absoluteValue.toMillisTime(),
            status = if (time > 0) ItemStatus.STATUS_COMPLETE else ItemStatus.STATUS_START
        )
    }

}
