package com.mty.exptools.ui.share.edit.photo

import com.mty.exptools.domain.photo.PhotocatalysisDraft
import com.mty.exptools.ui.share.edit.syn.DialogState

enum class PhotocatalysisMode { EDIT, VIEW }

data class PhotoEditUiState(
    val mode: PhotocatalysisMode = PhotocatalysisMode.EDIT,
    val draft: PhotocatalysisDraft = PhotocatalysisDraft(),
    val currentStepIndex: Int = 0,     // 浏览态当前步高亮
    val loading: Boolean = false,
    val dialogState: DialogState = DialogState(),
    val error: String? = null
)