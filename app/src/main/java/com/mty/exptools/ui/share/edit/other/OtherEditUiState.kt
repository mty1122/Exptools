package com.mty.exptools.ui.share.edit.other

import com.mty.exptools.domain.other.OtherDraft

enum class OtherMode { EDIT, VIEW }

data class OtherDialogState(
    val openDeleteConfirmDialog: Boolean = false,
    val openSetCompletedAtDialog: Boolean = false,
    val openLoadOtherSheet: Boolean = false
) {
    fun closeAll() = copy(
        openDeleteConfirmDialog = false,
        openSetCompletedAtDialog = false,
        openLoadOtherSheet = false
    )
}

data class OtherEditUiState(
    val mode: OtherMode = OtherMode.EDIT,
    val draft: OtherDraft = OtherDraft(),
    val isNew: Boolean = false,
    val dialogState: OtherDialogState = OtherDialogState(),
    val backgroundBlur: Boolean = false
)