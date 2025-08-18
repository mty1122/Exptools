package com.mty.exptools.ui.share.edit.test

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mty.exptools.domain.syn.SynthesisDraft
import com.mty.exptools.domain.test.TestDraft
import com.mty.exptools.repository.TestRepository
import com.mty.exptools.ui.TestEditRoute
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
class TestEditViewModel @Inject constructor(
    private val repo: TestRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestEditUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val route = savedStateHandle.toRoute<TestEditRoute>()
            val dbId = route.dbId
            if (dbId == null) {
                // 新增
                _uiState.value = TestEditUiState(
                    mode = TestMode.EDIT,
                    draft = TestDraft(),
                    isNew = true
                )
            } else {
                val loaded = repo.getById(dbId)
                _uiState.value = if (loaded != null) {
                    TestEditUiState(
                        mode = TestMode.VIEW,
                        draft = loaded
                    )
                } else {
                    TestEditUiState(
                        mode = TestMode.EDIT,
                        draft = TestDraft(),
                        isNew = true
                    )
                }
            }
        }
    }

    fun onAction(a: TestAction) {
        when (a) {
            is TestAction.UpdateMaterial -> _uiState.update { it.copy(draft = it.draft.copy(materialName = a.v)) }
            is TestAction.UpdateSummary -> _uiState.update { it.copy(draft = it.draft.copy(summary = a.v)) }
            is TestAction.UpdateDetails -> _uiState.update { it.copy(draft = it.draft.copy(details = a.v)) }

            is TestAction.SetStartAt -> _uiState.update {
                it.copy(dialogState = it.dialogState.copy(openSetStartAtDialog = true))
            }

            TestAction.PickExistingMaterial -> {
                _uiState.update {
                    it.copy(
                        dialogState = it.dialogState.copy(openLoadMaterialSheet = true),
                        backgroundBlur = true
                    )
                }
            }

            TestAction.Save -> save()
            TestAction.Edit -> _uiState.update { it.copy(mode = TestMode.EDIT) }
            TestAction.DeleteDraft -> {
                _uiState.update {
                    it.copy(
                        dialogState = it.dialogState.copy(openDeleteConfirmDialog = true),
                        backgroundBlur = true
                    )
                }
            }
            TestAction.LoadTest -> {
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
            currentDraft.materialName.ifBlank {
                toast("材料名称不能为空！")
                return@launch
            }
            val dbId = repo.upsert(currentDraft)    // 新增返回新id；更新返回原id
            _uiState.update {
                it.copy(
                    mode = TestMode.VIEW,
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

            _uiState.value = TestEditUiState(
                mode = TestMode.EDIT,
                draft = TestDraft()
            )
        }
    }

    fun setStartAt(startAt: Long) {
        _uiState.update { it.copy(draft = it.draft.copy(startAt = startAt)) }
    }

    // 用 Flow 暴露“全量列表”给导入对话框
    val allSynDrafts: StateFlow<List<SynthesisDraft>> =
        repo.observeAllSynDrafts() // Flow<List<SynthesisDraft>>
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 用 Flow 暴露“全量列表”给导入对话框
    val allTestDrafts: StateFlow<List<TestDraft>> =
        repo.observeAllTestDrafts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 全量导入选中的草稿
    fun loadDraftFrom(selected: TestDraft) {
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