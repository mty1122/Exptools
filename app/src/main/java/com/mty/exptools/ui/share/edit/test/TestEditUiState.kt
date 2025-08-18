package com.mty.exptools.ui.share.edit.test

import com.mty.exptools.domain.test.TestDraft

enum class TestMode { EDIT, VIEW }

data class TestDialogState(
    val openLoadMaterialSheet: Boolean = false,
    val openDeleteConfirmDialog: Boolean = false,
    val openSetStartAtDialog: Boolean = false,
    val openLoadOtherSheet: Boolean = false
) {
    fun closeAll() = copy(
        openLoadMaterialSheet = false,
        openDeleteConfirmDialog = false,
        openSetStartAtDialog = false,
        openLoadOtherSheet = false
    )
}

data class TestEditUiState(
    val mode: TestMode = TestMode.EDIT,
    val draft: TestDraft = TestDraft(),
    val isNew: Boolean = false,
    val dialogState: TestDialogState = TestDialogState(),
    val backgroundBlur: Boolean = false
)