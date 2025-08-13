package com.mty.exptools.ui.share.edit.syn

import com.mty.exptools.domain.syn.SynthesisDraft

enum class SynthesisMode { EDIT, VIEW }

data class SynthesisEditUiState(
    val mode: SynthesisMode = SynthesisMode.EDIT,
    val draft: SynthesisDraft = SynthesisDraft(),
    val currentStepIndex: Int = 0,
    val running: Boolean = false,
    val loading: Boolean = false,
    val openPrevConfirmDialog: Boolean = false,
    val openSubsConfirmDialog: Boolean = false,
    val openDeleteConfirmDialog: Boolean = false,
    val openCompleteConfirmDialog: Boolean = false,
    val openManualCompleteAtDialog: Boolean = false,
    val jumpTargetIndex: Int? = null,
    val error: String? = null
)
