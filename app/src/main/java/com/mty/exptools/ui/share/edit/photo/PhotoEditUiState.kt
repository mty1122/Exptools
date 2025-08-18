package com.mty.exptools.ui.share.edit.photo

import com.mty.exptools.domain.photo.PhotocatalysisDraft

enum class PhotocatalysisMode { EDIT, VIEW }
data class PhotoDialogState(
    val openLoadMaterialSheet: Boolean = false,
    val openPrevConfirmDialog: Boolean = false,
    val openSubsConfirmDialog: Boolean = false,
    val openDeleteConfirmDialog: Boolean = false,
    val openCompleteConfirmDialog: Boolean = false,
    val openManualCompleteAtDialog: Boolean = false,
    val openLoadOtherSheet: Boolean = false
) {
    fun closeAll() = copy(
        openLoadMaterialSheet = false,
        openPrevConfirmDialog = false,
        openSubsConfirmDialog = false,
        openDeleteConfirmDialog = false,
        openCompleteConfirmDialog = false,
        openManualCompleteAtDialog = false,
        openLoadOtherSheet = false
    )
}

data class PhotoEditUiState(
    val mode: PhotocatalysisMode = PhotocatalysisMode.EDIT,
    val draft: PhotocatalysisDraft = PhotocatalysisDraft(),
    val currentStepIndex: Int = 0,     // 浏览态当前步高亮
    val loading: Boolean = false,
    val isNew: Boolean = false,
    val running: Boolean = false,
    val dialogState: PhotoDialogState = PhotoDialogState(),
    val backgroundBlur: Boolean = false,
    val jumpTargetIndex: Int? = null,
    val error: String? = null
)