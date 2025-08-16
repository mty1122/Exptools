package com.mty.exptools.ui.share.edit.photo

import com.mty.exptools.domain.photo.PhotocatalysisDraft

enum class PhotocatalysisMode { EDIT, VIEW }
data class PhotoDialogState(
    val openLoadMaterialDialog: Boolean = false,
    val openPrevConfirmDialog: Boolean = false,
    val openSubsConfirmDialog: Boolean = false,
    val openDeleteConfirmDialog: Boolean = false,
    val openCompleteConfirmDialog: Boolean = false,
    val openManualCompleteAtDialog: Boolean = false,
    val openLoadOtherDialog: Boolean = false
) {
    fun closeAll() = copy(
        openLoadMaterialDialog = false,
        openPrevConfirmDialog = false,
        openSubsConfirmDialog = false,
        openDeleteConfirmDialog = false,
        openCompleteConfirmDialog = false,
        openManualCompleteAtDialog = false,
        openLoadOtherDialog = false
    )
    fun isOpen() = openPrevConfirmDialog || openSubsConfirmDialog || openDeleteConfirmDialog
            || openCompleteConfirmDialog || openManualCompleteAtDialog || openLoadOtherDialog
            || openLoadMaterialDialog
}

data class PhotoEditUiState(
    val mode: PhotocatalysisMode = PhotocatalysisMode.EDIT,
    val draft: PhotocatalysisDraft = PhotocatalysisDraft(),
    val currentStepIndex: Int = 0,     // 浏览态当前步高亮
    val loading: Boolean = false,
    val photoDialogState: PhotoDialogState = PhotoDialogState(),
    val error: String? = null
)