package com.mty.exptools.ui.share.edit.other

sealed interface OtherEditAction {
    data class UpdateTaskName(val v: String): OtherEditAction
    data class UpdateSummary(val v: String): OtherEditAction
    data class UpdateDetails(val v: String): OtherEditAction

    object SetCompletedAt: OtherEditAction

    object LoadOther: OtherEditAction

    // 保存/编辑切换可在外层顶栏触发
    object Save: OtherEditAction
    object Edit: OtherEditAction
    object DeleteDraft: OtherEditAction
}