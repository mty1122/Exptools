package com.mty.exptools.ui.share.edit.syn

import android.icu.util.TimeUnit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mty.exptools.domain.syn.SynthesisDraft
import com.mty.exptools.domain.syn.SynthesisStep
import com.mty.exptools.repository.SynthesisRepository
import com.mty.exptools.ui.SynthesisEditRoute
import com.mty.exptools.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SynthesisEditViewModel @Inject constructor(
    private val repo: SynthesisRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(SynthesisEditUiState(loading = true))
    val uiState: StateFlow<SynthesisEditUiState> = _uiState.asStateFlow()

    // 从强类型路由参数取材料名（为空=新增）
    private val materialNameArg: String? =
        savedStateHandle.toRoute<SynthesisEditRoute>().materialName

    init {
        viewModelScope.launch {
            if (materialNameArg == null) {
                _uiState.value = SynthesisEditUiState(
                    mode = SynthesisMode.EDIT,
                    draft = SynthesisDraft(),
                    loading = false
                )
            } else {
                val draft = repo.getByMaterialName(materialNameArg)
                _uiState.update {
                    if (draft != null) {
                        it.copy(
                            mode = SynthesisMode.VIEW,       // 列表点击进入即为浏览
                            draft = draft,
                            currentStepIndex = draft.currentStepIndex,
                            running = draft.steps[draft.currentStepIndex].timer.isRunning(),
                            loading = false,
                            error = null
                        )
                    } else {
                        // 找不到：提供一个同名空草稿进入编辑（也可选择提示错误）
                        SynthesisEditUiState(
                            mode = SynthesisMode.EDIT,
                            draft = SynthesisDraft(materialName = materialNameArg),
                            loading = false
                        )
                    }
                }
            }
        }
    }

    fun onAction(a: SynthesisAction) {
        when (a) {
            is SynthesisAction.UpdateMaterial ->
                _uiState.update { it.copy(draft = it.draft.copy(materialName = a.v)) }

            is SynthesisAction.UpdateRaw ->
                _uiState.update { it.copy(draft = it.draft.copy(rawMaterials = a.v)) }

            is SynthesisAction.UpdateSummary ->
                _uiState.update { it.copy(draft = it.draft.copy(conditionSummary = a.v)) }

            is SynthesisAction.UpdateDetail ->
                _uiState.update { it.copy(draft = it.draft.copy(expDetails = a.v)) }

            is SynthesisAction.UpdateStepContent ->
                _uiState.update {
                    it.copy(
                        draft = it.draft.copy(
                        steps = it.draft.steps.map { s ->
                            if (s.orderIndex == a.orderIndex) s.copy(
                                content = a.v
                            ) else s
                        }
                    ))
                }

            is SynthesisAction.UpdateStepDuration ->
                _uiState.update {
                    val n = a.numberText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    val millis = if (a.unit == TimeUnit.HOUR) n * 60L * 60_000L else n * 60_000L
                    it.copy(
                        draft = it.draft.copy(
                        steps = it.draft.steps.map { s ->
                            if (s.orderIndex == a.orderIndex) s.copy(
                                unit = a.unit,
                                timer = s.timer.copy(requiredMillis = millis)
                            ) else s
                        }
                    ))
                }

            is SynthesisAction.UpdateStepUnit ->
                _uiState.update {
                    it.copy(
                        draft = it.draft.copy(
                        steps = it.draft.steps.map { s ->
                            if (s.orderIndex == a.orderIndex) s.copy(
                                unit = a.unit
                            ) else s
                        }
                    ))
                }

            // 添加步骤时，下一步骤的索引为整个数组的大小
            SynthesisAction.AddStep ->
                _uiState.update {
                    it.copy(
                        draft = it.draft.copy(
                            steps = it.draft.steps + SynthesisStep(orderIndex = it.draft.steps.size)
                        )
                    )
                }

            is SynthesisAction.RemoveStep ->
                _uiState.update {
                    val newList = it.draft.steps.filterNot { s -> s.orderIndex == a.orderIndex }
                    it.copy(
                        draft = it.draft.copy(steps = newList.ifEmpty { listOf(SynthesisStep()) }),
                        currentStepIndex = it.currentStepIndex.coerceAtMost(
                            (newList.size - 1).coerceAtLeast(
                                0
                            )
                        )
                    )
                }

            SynthesisAction.Save -> viewModelScope.launch {
                // 持久化 draft
                val current = _uiState.value.draft
                repo.upsert(current)
                _uiState.update {
                    it.copy(
                        mode = SynthesisMode.VIEW, running = false,
                        currentStepIndex = current.currentStepIndex
                    )
                }
            }

            SynthesisAction.Edit ->
                _uiState.update { it.copy(mode = SynthesisMode.EDIT) }

            SynthesisAction.ToggleRun ->
                _uiState.update { state ->
                    val idx = state.currentStepIndex
                    val steps = state.draft.steps.toMutableList()
                    val cur = steps.getOrNull(idx) ?: return@update state
                    val newTimer =
                        if (cur.timer.isRunning()) cur.timer.pause() else cur.timer.start()
                    steps[idx] = cur.copy(timer = newTimer)

                    // 持久化时间信息
                    viewModelScope.launch {
                        repo.updateStepsTimerByIndex(
                            materialName = state.draft.materialName,
                            orderIndexes = listOf(idx),
                            accumulatedMillis = newTimer.accumulatedMillis,
                            startEpochMs = newTimer.startEpochMs
                        )
                    }

                    state.copy(
                        draft = state.draft.copy(steps = steps),
                        running = newTimer.isRunning()
                    )
                }

            SynthesisAction.CompleteCurrentStep -> {
                val hasNext = uiState.value.currentStepIndex < _uiState.value.draft.steps.lastIndex
                _uiState.update {
                    if (hasNext)
                        it.copy(openNextConfirmDialog = true)
                    else
                        it.copy(openCompleteConfirmDialog = true)
                }
        }

            SynthesisAction.GoToPreviousStep ->
                _uiState.update {
                    it.copy(openPrevConfirmDialog = true)
                }

            is SynthesisAction.SetCurrentIndex ->
                _uiState.update { it.copy(currentStepIndex = a.index.coerceIn(0, it.draft.steps.lastIndex)) }

            SynthesisAction.DeleteDraft ->
                _uiState.update {
                    it.copy(openDeleteConfirmDialog = true)
                }
        }
    }

    fun closeConfirmDialog() {
        _uiState.update { it.copy(
            openNextConfirmDialog = false,
            openPrevConfirmDialog = false,
            openDeleteConfirmDialog = false,
            openCompleteConfirmDialog = false
        ) }
    }

    fun completeCurrentStep() {
        _uiState.update { state ->
            val idx = state.currentStepIndex
            val steps = state.draft.steps.toMutableList()
            val cur = steps.getOrNull(idx) ?: return@update state
            steps[idx] = cur.copy(timer = cur.timer.complete())
            val hasNext = idx < steps.lastIndex

            // 持久化时间信息
            viewModelScope.launch {
                repo.updateStepsTimerByIndex(
                    materialName = state.draft.materialName,
                    orderIndexes = listOf(idx),
                    accumulatedMillis = steps[idx].timer.accumulatedMillis,
                    startEpochMs = steps[idx].timer.startEpochMs
                )
            }

            state.copy(
                draft = state.draft.copy(steps = steps),
                currentStepIndex = if (hasNext) idx + 1 else idx,
                running = false // 按需求：自动暂停
            )
        }
    }

    fun goToPreviousStep() {
        _uiState.update { state ->
            val idx = state.currentStepIndex
            if (idx <= 0) return@update state
            val steps = state.draft.steps.toMutableList()
            // 1) 复位“当前步骤”
            steps.getOrNull(idx)?.let { cur ->
                steps[idx] = cur.copy(timer = cur.timer.reset())
            }
            // 2) 复位“上一步骤”的计时并跳转
            val prevIdx = idx - 1
            steps.getOrNull(prevIdx)?.let { prev ->
                steps[prevIdx] = prev.copy(timer = prev.timer.reset())
            }

            // 持久化时间信息
            viewModelScope.launch {
                repo.updateStepsTimerByIndex(
                    materialName = state.draft.materialName,
                    orderIndexes = listOf(idx - 1, idx),
                    accumulatedMillis = steps[idx].timer.accumulatedMillis,
                    startEpochMs = steps[idx].timer.startEpochMs
                )
            }

            state.copy(
                draft = state.draft.copy(steps = steps),
                currentStepIndex = prevIdx,
                running = false // 自动暂停
            )
        }
    }

    fun deleteCurrentDraft() {
        viewModelScope.launch {
            val result = repo.deleteDraftByName(_uiState.value.draft.materialName)
            toast(if (result) "删除成功" else "删除失败")

            _uiState.value = SynthesisEditUiState(
                mode = SynthesisMode.EDIT,
                draft = SynthesisDraft(),
                loading = false
            )
        }
    }

}