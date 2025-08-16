package com.mty.exptools.ui.share.edit.syn

import com.mty.exptools.domain.syn.SynthesisDraft
import kotlin.Boolean

enum class SynthesisMode { EDIT, VIEW }
data class SynthesisDialogState(
    val openPrevConfirmDialog: Boolean = false,
    val openSubsConfirmDialog: Boolean = false,
    val openDeleteConfirmDialog: Boolean = false,
    val openCompleteConfirmDialog: Boolean = false,
    val openManualCompleteAtDialog: Boolean = false,
    val openLoadOtherSheet: Boolean = false
) {
    fun closeAll() = copy(
        openPrevConfirmDialog = false,
        openSubsConfirmDialog = false,
        openDeleteConfirmDialog = false,
        openCompleteConfirmDialog = false,
        openManualCompleteAtDialog = false,
        openLoadOtherSheet = false
    )
}

data class SynthesisEditUiState(
    val mode: SynthesisMode = SynthesisMode.EDIT,
    val draft: SynthesisDraft = SynthesisDraft(),
    val nameEditable: Boolean = true,
    val currentStepIndex: Int = 0,
    val running: Boolean = false,
    val loading: Boolean = false,
    val dialogState: SynthesisDialogState = SynthesisDialogState(),
    val backgroundBlur: Boolean = false,
    val jumpTargetIndex: Int? = null,
    val error: String? = null
)
