package com.mty.exptools.ui.share.edit.photo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mty.exptools.domain.photo.PhotocatalysisDraft
import com.mty.exptools.domain.photo.PhotocatalysisStep
import com.mty.exptools.domain.syn.SynthesisDraft
import com.mty.exptools.repository.PhotoRepository
import com.mty.exptools.repository.TickRepository
import com.mty.exptools.ui.PhotoEditRoute
import com.mty.exptools.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoEditViewModel @Inject constructor(
    private val repo: PhotoRepository,
    tickRepo: TickRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PhotoEditUiState(loading = true)
    )
    val uiState: StateFlow<PhotoEditUiState> = _uiState.asStateFlow()

    private val tickFlow = tickRepo.autoRefreshTicker
    private val _tick = MutableStateFlow(0) // 自动刷新
    val tick: StateFlow<Int> = _tick.asStateFlow()

    init {
        viewModelScope.launch {
            // 强类型路由
            val route = savedStateHandle.toRoute<PhotoEditRoute>()
            val dbId = route.dbId

            if (dbId == null) {
                // 新增
                _uiState.value = PhotoEditUiState(
                    mode = PhotocatalysisMode.EDIT,
                    draft = PhotocatalysisDraft(), // 新建
                    loading = false,
                    isNew = true
                )
            } else {
                // 加载已有实验
                val loaded = repo.getById(dbId)
                _uiState.value = if (loaded != null) {
                    PhotoEditUiState(
                        mode = PhotocatalysisMode.VIEW,
                        draft = loaded,
                        currentStepIndex = loaded.currentStepIndex,
                        running = loaded.steps[loaded.currentStepIndex].timer.isRunning(),
                        loading = false
                    )
                } else {
                    // 找不到则给一个“可编辑空草稿”（也可选择提示错误）
                    PhotoEditUiState(
                        mode = PhotocatalysisMode.EDIT,
                        draft = PhotocatalysisDraft(),
                        loading = false,
                        isNew = true,
                        error = "未找到记录（id=$dbId），已进入新增模式"
                    )
                }
            }
        }
        // 每过10秒刷新浏览模式的剩余时间和状态
        viewModelScope.launch {
            tickFlow.onEach {
                if (isActive) {
                    val state = _uiState.value
                    if (state.mode == PhotocatalysisMode.VIEW && state.running) {
                        _tick.update { it + 1 }
                        // 如果已经完成，则跳转至下一步或者完成所有步骤（数据结构设计之初就考虑了这种情况，只需更新UI即可）
                        if (state.draft.steps[state.currentStepIndex].timer.isFinished())
                            _uiState.update {
                                val currentIndex = it.currentStepIndex
                                val hasNext = currentIndex < it.draft.steps.lastIndex
                                it.copy(
                                    draft = it.draft.copy(
                                        completedAt = if (hasNext) null else it.draft.completedAt
                                    ),
                                    running = false,
                                    currentStepIndex = if (hasNext) currentIndex + 1 else currentIndex
                                )
                            }
                    }
                    // 光催化实验（同名）连续进行，不暂停
                    if (!state.running && !state.draft.isFinished && state.currentStepIndex > 0) {
                        val lastStep = state.draft.steps[state.currentStepIndex - 1]
                        val currentStep = state.draft.steps[state.currentStepIndex]
                        if (lastStep.name == currentStep.name)
                            onAction(PhotoEditAction.ToggleRun)
                    }
                }
            }.launchIn(this)
        }
    }

    fun onAction(a: PhotoEditAction) {
        when (a) {
            PhotoEditAction.PickExistingCatalyst -> {
                _uiState.update {
                    it.copy(
                        dialogState = it.dialogState.copy(openLoadMaterialSheet = true),
                        backgroundBlur = true
                    )
                }
            }

            is PhotoEditAction.UpdateCatalystName ->
                _uiState.update { it.copy(draft = it.draft.copy(catalystName = a.v)) }

            // ---------- Target（反应物/产物） ----------
            is PhotoEditAction.UpdateTargetName ->
                _uiState.update { it.copy(draft = it.draft.copy(target = it.draft.target.copy(name = a.v))) }

            is PhotoEditAction.UpdateTargetWavelength ->
                _uiState.update { it.copy(draft = it.draft.copy(target = it.draft.target.copy(wavelengthNm = a.v))) }

            is PhotoEditAction.UpdateInitialConcValue ->
                _uiState.update { it.copy(draft = it.draft.copy(target = it.draft.target.copy(initialConcValue = a.text))) }

            is PhotoEditAction.UpdateInitialConcUnit ->
                _uiState.update { it.copy(draft = it.draft.copy(target = it.draft.target.copy(initialConcUnit = a.unit))) }

            is PhotoEditAction.UpdateStdCurveK ->
                _uiState.update { it.copy(draft = it.draft.copy(target = it.draft.target.copy(stdCurveK = a.text))) }

            is PhotoEditAction.UpdateStdCurveB ->
                _uiState.update { it.copy(draft = it.draft.copy(target = it.draft.target.copy(stdCurveB = a.text))) }

            // ---------- 光源 ----------
            is PhotoEditAction.UpdateLightSource ->
                _uiState.update { it.copy(draft = it.draft.copy(light = a.v)) }

            // ---------- 实验细节 ----------
            is PhotoEditAction.UpdateDetails ->
                _uiState.update { it.copy(draft = it.draft.copy(details = a.v)) }

            // ---------- 步骤 ----------
            is PhotoEditAction.UpdateStepName ->
                _uiState.update { it.copy(draft = it.draft.copy(
                    steps = it.draft.steps.map { s -> if (s.orderIndex == a.orderIndex) s.copy(name = a.v) else s }
                )) }

            is PhotoEditAction.UpdateStepIntervalMinute ->
                _uiState.update {
                    val n = a.text.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    val millis = n * 60_000L
                    it.copy(
                        draft = it.draft.copy(
                            steps = it.draft.steps.map { s ->
                                if (s.orderIndex == a.orderIndex) s.copy(
                                    timer = s.timer.copy(requiredMillis = millis)
                                ) else s
                            }
                        )
                    )
                }

            is PhotoEditAction.UpdateStepConcValue ->
                _uiState.update { it.copy(draft = it.draft.copy(
                    steps = it.draft.steps.map { s ->
                        if (s.orderIndex == a.orderIndex) s.copy(concValueText = a.text) else s
                    }
                )) }

            is PhotoEditAction.UpdateStepConcUnit ->
                _uiState.update { it.copy(draft = it.draft.copy(
                    steps = it.draft.steps.map { s ->
                        if (s.orderIndex == a.orderIndex) s.copy(concUnit = a.unit) else s
                    }
                )) }

            PhotoEditAction.AddStep ->
                _uiState.update { st ->
                    val next = st.draft.steps.size
                    st.copy(draft = st.draft.copy(
                        steps = st.draft.steps + PhotocatalysisStep(orderIndex = next)
                    ))
                }

            is PhotoEditAction.RemoveStep ->
                _uiState.update { st ->
                    val removed = a.orderIndex

                    val filtered = st.draft.steps.filterNot { it.orderIndex == a.orderIndex }

                    val normalized = if (filtered.isEmpty()) {
                        listOf(PhotocatalysisStep())
                    } else {
                        filtered.mapIndexed { idx, s -> s.copy(orderIndex = idx) }
                    }

                    val newCurrent = when {
                        st.currentStepIndex > removed -> st.currentStepIndex - 1
                        st.currentStepIndex >= normalized.size -> normalized.lastIndex
                        else -> st.currentStepIndex
                    }
                    val newRunning = normalized[newCurrent].timer.isRunning()

                    st.copy(
                        draft = st.draft.copy(steps = normalized),
                        currentStepIndex = newCurrent,
                        running = newRunning
                    )
                }

            PhotoEditAction.ToggleRun ->
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
                            dbId = state.draft.dbId,
                            orderIndexes = listOf(idx),
                            accumulatedMillis = newTimer.accumulatedMillis,
                            startEpochMs = newTimer.startEpochMs
                        )
                    }

                    val now = System.currentTimeMillis()
                    var remaining = 0L
                    for (index in idx until steps.size) {
                        remaining += steps[index].timer.remaining()
                    }
                    val completedAt = if (newTimer.isRunning()) now + remaining else null
                    setCompletedAt(completedAt)

                    state.copy(
                        draft = state.draft.copy(steps = steps, completedAt = completedAt),
                        running = newTimer.isRunning()
                    )
                }

            is PhotoEditAction.JumpStep -> {
                val targetIdx = a.index
                val currentIdx = _uiState.value.currentStepIndex

                when {
                    targetIdx < currentIdx -> {
                        _uiState.update {
                            it.copy(
                                dialogState = it.dialogState.copy(openPrevConfirmDialog = true),
                                jumpTargetIndex = targetIdx,
                                backgroundBlur = true
                            )
                        }
                    }
                    targetIdx > currentIdx -> {
                        _uiState.update {
                            it.copy(
                                dialogState = it.dialogState.copy(openSubsConfirmDialog = true),
                                jumpTargetIndex = targetIdx,
                                backgroundBlur = true
                            )
                        }
                    }
                    // 对于最后一步，如果还未完成，点击自身为完成
                    else -> {
                        val hasNext = uiState.value.currentStepIndex < _uiState.value.draft.steps.lastIndex
                        if (!hasNext && !uiState.value.draft.isFinished)
                            _uiState.update {
                                it.copy(
                                    dialogState = it.dialogState.copy(openCompleteConfirmDialog = true),
                                    backgroundBlur = true
                                )
                            }
                    }
                }
            }

            PhotoEditAction.Save -> save()
            PhotoEditAction.Edit -> _uiState.update { it.copy(mode = PhotocatalysisMode.EDIT) }

            PhotoEditAction.DeleteDraft -> _uiState.update {
                it.copy(
                    dialogState = it.dialogState.copy(openDeleteConfirmDialog = true),
                    backgroundBlur = true
                )
            }

            PhotoEditAction.ManualCompletedAt ->
                _uiState.update {
                    it.copy(
                        dialogState = it.dialogState.copy(openManualCompleteAtDialog = true),
                        backgroundBlur = true
                    )
                }

            PhotoEditAction.LoadPhoto ->
                _uiState.update {
                    it.copy(
                        dialogState = it.dialogState.copy(openLoadOtherSheet = true),
                        backgroundBlur = true
                    )
                }
        }
    }

    /** 保存：使用 upsert（不检查名称重复；id=0 视为新增） */
    private fun save() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentDraft = currentState.draft
            currentDraft.catalystName.ifBlank {
                toast("催化剂名称不能为空！")
                return@launch
            }
            val dbId = repo.upsert(currentDraft)    // 新增返回新id；更新返回原id

            // 编辑后当前步骤索引
            val currentStepIndex = currentDraft.currentStepIndex

            val now = System.currentTimeMillis()
            var completedAt: Long? = null
            when {
                // 若draft已完成，则处理
                currentDraft.isFinished -> {
                    // 删除所有未完成的步骤
                    if (currentDraft.completedAt == null || currentDraft.completedAt > now) {
                        setCompletedAt(now)
                        completedAt = now
                        // 删除操作，且原本就已完成
                    } else {
                        completedAt = currentDraft.completedAt
                    }
                    _uiState.update {
                        it.copy(
                            mode = PhotocatalysisMode.VIEW, running = false,
                            currentStepIndex = currentStepIndex,
                            draft = it.draft.copy(dbId = dbId, completedAt = completedAt),
                            isNew = false
                        )
                    }
                }
                // 若draft未完成，则暂停
                else -> {
                    val steps = currentState.draft.steps.toMutableList()
                    val cur = steps.getOrNull(currentStepIndex) ?: return@launch
                    // 若未暂停，则进行暂停
                    if (cur.timer.isRunning()) {
                        val newTimer = cur.timer.pause()
                        steps[currentStepIndex] = cur.copy(timer = newTimer)
                        repo.updateStepsTimerByIndex(
                            dbId = currentState.draft.dbId,
                            orderIndexes = listOf(currentStepIndex),
                            accumulatedMillis = newTimer.accumulatedMillis,
                            startEpochMs = newTimer.startEpochMs
                        )
                        setCompletedAt(null)
                        _uiState.update {
                            it.copy(
                                mode = PhotocatalysisMode.VIEW, running = false,
                                currentStepIndex = currentStepIndex,
                                draft = it.draft.copy(dbId = dbId, steps = steps, completedAt = null),
                                isNew = false
                            )
                        }
                        // 若已暂停，则可以不用处理完成时间
                    } else {
                        // 这里还是需要设置一次null，因为如果原本是running，但是running的那个步骤被删除了，则不会变为null
                        setCompletedAt(null)
                        _uiState.update {
                            it.copy(
                                mode = PhotocatalysisMode.VIEW, running = false,
                                currentStepIndex = currentStepIndex,
                                draft = it.draft.copy(dbId = dbId, completedAt = null),
                                isNew = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun completeLastStep() {
        _uiState.update { state ->
            val idx = state.currentStepIndex
            val steps = state.draft.steps.toMutableList()
            val cur = steps.getOrNull(idx) ?: return@update state
            steps[idx] = cur.copy(timer = cur.timer.complete())

            // 持久化时间信息
            viewModelScope.launch {
                repo.updateStepsTimerByIndex(
                    dbId = state.draft.dbId,
                    orderIndexes = listOf(idx),
                    accumulatedMillis = steps[idx].timer.accumulatedMillis,
                    startEpochMs = steps[idx].timer.startEpochMs
                )
            }

            val now = System.currentTimeMillis()
            setCompletedAt(now)

            state.copy(
                draft = state.draft.copy(steps = steps, completedAt = now),
                running = false // 按需求：自动暂停
            )
        }
    }

    fun goToPreviousStep() {
        val targetIndex = uiState.value.jumpTargetIndex
        if (targetIndex == null) return
        _uiState.update { state ->
            val currentIndex = state.currentStepIndex
            if (currentIndex <= 0 || targetIndex >= currentIndex) return@update state
            val steps = state.draft.steps.toMutableList()
            if (currentIndex > steps.lastIndex) return@update state

            // 复位沿路所有步骤
            for (idx in targetIndex .. currentIndex) {
                steps[idx] = steps[idx].copy(timer = steps[idx].timer.reset())
            }

            // 持久化时间信息
            viewModelScope.launch {
                repo.updateStepsTimerByIndex(
                    dbId = state.draft.dbId,
                    orderIndexes = (targetIndex .. currentIndex).toList(),
                    accumulatedMillis = steps[targetIndex].timer.accumulatedMillis,
                    startEpochMs = steps[targetIndex].timer.startEpochMs
                )
            }

            setCompletedAt(null)

            state.copy(
                draft = state.draft.copy(steps = steps, completedAt = null),
                currentStepIndex = targetIndex,
                running = false // 自动暂停
            )
        }
    }

    fun goToSubsequentStep() {
        val targetIndex = uiState.value.jumpTargetIndex
        if (targetIndex == null) return
        _uiState.update { state ->
            val currentIndex = state.currentStepIndex
            if (currentIndex < 0 || targetIndex <= currentIndex) return@update state
            val steps = state.draft.steps.toMutableList()
            if (targetIndex > steps.lastIndex) return@update state

            // 完成沿路所有步骤
            for (idx in currentIndex until targetIndex) {
                steps[idx] = steps[idx].copy(timer = steps[idx].timer.complete())
            }
            // 持久化时间信息
            viewModelScope.launch {
                repo.completeStepsByIndex(
                    dbId = state.draft.dbId,
                    orderIndexes = (currentIndex until targetIndex).toList(),
                )
            }

            setCompletedAt(null)

            state.copy(
                draft = state.draft.copy(steps = steps, completedAt = null),
                currentStepIndex = targetIndex,
                running = false // 自动暂停
            )
        }
    }

    fun deleteCurrentDraft() {
        viewModelScope.launch {
            val result = repo.deleteDraftByDbId(_uiState.value.draft.dbId)
            toast(if (result) "删除成功" else "删除失败")

            _uiState.value = PhotoEditUiState(
                mode = PhotocatalysisMode.EDIT,
                draft = PhotocatalysisDraft(),
                loading = false
            )
        }
    }

    private fun setCompletedAt(time: Long?) {
        val state = _uiState.value
        // 不重复更新
        if (state.draft.completedAt == time) return
        val dbId = state.draft.dbId
        viewModelScope.launch { repo.setCompletedAt(dbId, time) }
    }

    fun setCompletedAtWithUiState(time: Long?) {
        val state = _uiState.value
        // 不重复更新
        if (state.draft.completedAt == time) return
        val dbId = state.draft.dbId
        viewModelScope.launch { repo.setCompletedAt(dbId, time) }
        _uiState.update { it.copy(draft = it.draft.copy(completedAt = time)) }
    }

    // 用 Flow 暴露“全量列表”给导入对话框
    val allSynDrafts: StateFlow<List<SynthesisDraft>> =
        repo.observeAllSynDrafts() // Flow<List<SynthesisDraft>>
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 用 Flow 暴露“全量列表”给导入对话框
    val allPhotoDrafts: StateFlow<List<PhotocatalysisDraft>> =
        repo.observeAllPhotoDrafts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 全量导入选中的草稿
    fun loadDraftFrom(selected: PhotocatalysisDraft) {
        _uiState.update { state ->
            val newDraft = selected.copy(
                dbId = 0L,
                completedAt = null,
                steps = selected.steps.map {
                    it.copy(
                        concValueText = "", // 重置步骤浓度
                        timer = it.timer.reset() // 重置步骤完成进度
                    )
                }
            )
            state.copy(draft = newDraft)
        }
    }

    fun closeDialog() {
        _uiState.update {
            it.copy(
                dialogState = it.dialogState.closeAll(),
                backgroundBlur = false
            )
        }
    }

    fun setBackgroundBlur(blur: Boolean) {
        _uiState.update { it.copy(backgroundBlur = blur) }
    }

}
