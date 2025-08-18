package com.mty.exptools.ui.share.edit.other

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mty.exptools.domain.other.OtherDraft
import com.mty.exptools.repository.OtherRepository
import com.mty.exptools.ui.OtherEditRoute
import com.mty.exptools.util.toast
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
class OtherEditViewModel @Inject constructor(
    private val repo: OtherRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtherEditUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val route = savedStateHandle.toRoute<OtherEditRoute>()
            val dbId = route.dbId
            if (dbId == null) {
                // 新增
                _uiState.value = OtherEditUiState(
                    mode = OtherMode.EDIT,
                    draft = OtherDraft(),
                    isNew = true
                )
            } else {
                val loaded = repo.getById(dbId)
                _uiState.value = if (loaded != null) {
                    OtherEditUiState(
                        mode = OtherMode.VIEW,
                        draft = loaded
                    )
                } else {
                    OtherEditUiState(
                        mode = OtherMode.EDIT,
                        draft = OtherDraft(),
                        isNew = true
                    )
                }
            }
        }
    }

    fun onAction(a: OtherEditAction) {
        when (a) {
            is OtherEditAction.UpdateTaskName -> _uiState.update { it.copy(draft = it.draft.copy(taskName = a.v)) }
            is OtherEditAction.UpdateSummary -> _uiState.update { it.copy(draft = it.draft.copy(summary = a.v)) }
            is OtherEditAction.UpdateDetails -> _uiState.update { it.copy(draft = it.draft.copy(details = a.v)) }

            is OtherEditAction.SetCompletedAt -> _uiState.update {
                it.copy(dialogState = it.dialogState.copy(openSetCompletedAtDialog = true))
            }

            OtherEditAction.Save -> save()
            OtherEditAction.Edit -> _uiState.update { it.copy(mode = OtherMode.EDIT) }
            OtherEditAction.DeleteDraft -> {
                _uiState.update {
                    it.copy(
                        dialogState = it.dialogState.copy(openDeleteConfirmDialog = true),
                        backgroundBlur = true
                    )
                }
            }
            OtherEditAction.LoadOther -> {
                _uiState.update {
                    it.copy(
                        dialogState = it.dialogState.copy(openLoadOtherSheet = true),
                        backgroundBlur = true
                    )
                }
            }
        }
    }

    private fun save() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentDraft = currentState.draft
            currentDraft.taskName.ifBlank {
                toast("任务名称不能为空！")
                return@launch
            }
            val dbId = repo.upsert(currentDraft)    // 新增返回新id；更新返回原id
            _uiState.update {
                it.copy(
                    mode = OtherMode.VIEW,
                    draft = it.draft.copy(dbId = dbId),
                    isNew = false
                )
            }
        }
    }

    fun deleteCurrentDraft() {
        viewModelScope.launch {
            val result = repo.deleteDraftByDbId(_uiState.value.draft.dbId)
            toast(if (result) "删除成功" else "删除失败")

            _uiState.value = OtherEditUiState(
                mode = OtherMode.EDIT,
                draft = OtherDraft()
            )
        }
    }

    fun setCompletedAt(completedAt: Long) {
        _uiState.update { it.copy(draft = it.draft.copy(completedAt = completedAt)) }
    }

    // 用 Flow 暴露“全量列表”给导入对话框
    val allOtherDrafts: StateFlow<List<OtherDraft>> =
        repo.observeAllOtherDrafts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 全量导入选中的草稿
    fun loadDraftFrom(selected: OtherDraft) {
        _uiState.update { state ->
            val newDraft = selected.copy(dbId = 0L)
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