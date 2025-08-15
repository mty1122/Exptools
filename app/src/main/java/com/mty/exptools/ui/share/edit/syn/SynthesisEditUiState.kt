package com.mty.exptools.ui.share.edit.syn

import com.mty.exptools.domain.syn.SynthesisDraft

enum class SynthesisMode { EDIT, VIEW }
data class DialogState(
    val openPrevConfirmDialog: Boolean = false,
    val openSubsConfirmDialog: Boolean = false,
    val openDeleteConfirmDialog: Boolean = false,
    val openCompleteConfirmDialog: Boolean = false,
    val openManualCompleteAtDialog: Boolean = false,
    val openLoadOtherDialog: Boolean = false
) {
    fun closeAll() = copy(
        openPrevConfirmDialog = false,
        openSubsConfirmDialog = false,
        openDeleteConfirmDialog = false,
        openCompleteConfirmDialog = false,
        openManualCompleteAtDialog = false,
        openLoadOtherDialog = false
    )
    fun isOpen() = openPrevConfirmDialog || openSubsConfirmDialog || openDeleteConfirmDialog
            || openCompleteConfirmDialog || openManualCompleteAtDialog || openLoadOtherDialog
}

data class SynthesisEditUiState(
    val mode: SynthesisMode = SynthesisMode.EDIT,
    val draft: SynthesisDraft = SynthesisDraft(),
    val nameEditable: Boolean = true,
    val currentStepIndex: Int = 0,
    val running: Boolean = false,
    val loading: Boolean = false,
    val dialogState: DialogState = DialogState(),
    val jumpTargetIndex: Int? = null,
    val error: String? = null
)
