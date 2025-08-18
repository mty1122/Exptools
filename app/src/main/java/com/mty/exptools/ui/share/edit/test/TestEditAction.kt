package com.mty.exptools.ui.share.edit.test

sealed interface TestAction {
    data class UpdateMaterial(val v: String): TestAction
    data class UpdateSummary(val v: String): TestAction
    data class UpdateDetails(val v: String): TestAction

    object SetStartAt: TestAction

    // 选择已有材料入口
    object PickExistingMaterial: TestAction

    // 保存/编辑切换可在外层顶栏触发
    object Save: TestAction
    object Edit: TestAction

    object DeleteDraft: TestAction
    object LoadTest: TestAction
}