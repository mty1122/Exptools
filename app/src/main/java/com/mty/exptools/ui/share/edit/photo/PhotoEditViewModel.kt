package com.mty.exptools.ui.share.edit.photo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mty.exptools.domain.photo.PhotocatalysisDraft
import com.mty.exptools.domain.photo.PhotocatalysisStep
import com.mty.exptools.domain.syn.SynthesisDraft
import com.mty.exptools.repository.PhotoRepository
import com.mty.exptools.ui.PhotoEditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoEditViewModel @Inject constructor(
    private val repo: PhotoRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PhotoEditUiState(loading = true)
    )
    val uiState: StateFlow<PhotoEditUiState> = _uiState.asStateFlow()

    private val _tick = MutableStateFlow(0) // 自动刷新
    val tick: StateFlow<Int> = _tick.asStateFlow()

    init {
        viewModelScope.launch {
            // 强类型路由（navigation-compose 2.7+）
            val route = savedStateHandle.toRoute<PhotoEditRoute>()
            val dbId = route.dbId

            if (dbId == null) {
                // 新增
                _uiState.value = PhotoEditUiState(
                    mode = PhotocatalysisMode.EDIT,
                    draft = PhotocatalysisDraft(), // 新建
                    loading = false
                )
            } else {
                // 加载已有实验
                val loaded = repo.loadById(dbId)
                _uiState.value = if (loaded != null) {
                    PhotoEditUiState(
                        mode = PhotocatalysisMode.VIEW,
                        draft = loaded,
                        currentStepIndex = 0,
                        loading = false
                    )
                } else {
                    // 找不到则给一个“可编辑空草稿”（也可选择提示错误）
                    PhotoEditUiState(
                        mode = PhotocatalysisMode.EDIT,
                        draft = PhotocatalysisDraft(),
                        loading = false,
                        error = "未找到记录（id=$dbId），已进入新增模式"
                    )
                }
            }
        }
    }

    fun onAction(a: PhotoEditAction) {
        when (a) {
            is PhotoEditAction.PickExistingCatalyst -> {
                // 交给 UI 弹选择器，不改状态
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
                _uiState.update { it.copy(draft = it.draft.copy(
                    steps = it.draft.steps.map { s ->
                        if (s.orderIndex == a.orderIndex) s.copy(intervalMinuteText = a.text) else s
                    }
                )) }

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
                    val filtered = st.draft.steps.filterNot { it.orderIndex == a.orderIndex }
                    val normalized = if (filtered.isEmpty()) {
                        listOf(PhotocatalysisStep(orderIndex = 0))
                    } else {
                        filtered.mapIndexed { idx, s -> s.copy(orderIndex = idx) }
                    }
                    val newCurrent = st.currentStepIndex.coerceAtMost((normalized.size - 1).coerceAtLeast(0))
                    st.copy(
                        draft = st.draft.copy(steps = normalized),
                        currentStepIndex = newCurrent
                    )
                }

            is PhotoEditAction.JumpStep ->
                _uiState.update { st ->
                    if (st.mode == PhotocatalysisMode.VIEW)
                        st.copy(currentStepIndex = a.index.coerceIn(0, st.draft.steps.lastIndex))
                    else st
                }

            PhotoEditAction.Save -> save()
            PhotoEditAction.Edit -> _uiState.update { it.copy(mode = PhotocatalysisMode.EDIT) }
        }
    }

    /** 保存：使用 upsert（不检查名称重复；id=0 视为新增） */
    private fun save() {
        viewModelScope.launch {
            val cur = _uiState.value
            val draft = cur.draft
            repo.upsert(draft)    // 新增返回新id；更新返回原id
            _uiState.update {
                it.copy(
                    mode = PhotocatalysisMode.VIEW,
                    error = null
                )
            }
        }
    }

    // 用 Flow 暴露“全量列表”给导入对话框
    val allSynDrafts: StateFlow<List<SynthesisDraft>> =
        repo.observeAllSynDrafts() // Flow<List<SynthesisDraft>>
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun closeDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.closeAll()) }
    }

}
