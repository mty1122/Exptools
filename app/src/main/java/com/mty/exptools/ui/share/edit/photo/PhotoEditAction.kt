package com.mty.exptools.ui.share.edit.photo

import com.mty.exptools.domain.photo.ConcUnit
import com.mty.exptools.domain.photo.LightSource
import com.mty.exptools.domain.photo.PhotoTargetMaterial

sealed interface PhotoEditAction {
    // 顶部：催化剂名称 & 选择已有
    data class UpdateCatalystName(val v: String) : PhotoEditAction
    object PickExistingCatalyst : PhotoEditAction

    // 反应物/产物信息
    data class UpdateTargetName(val v: String) : PhotoEditAction
    data class UpdateTargetWavelength(val v: String) : PhotoEditAction
    data class UpdateInitialConcValue(val text: String) : PhotoEditAction
    data class UpdateInitialConcUnit(val unit: ConcUnit) : PhotoEditAction
    data class UpdateStdCurveK(val text: String) : PhotoEditAction
    data class UpdateStdCurveB(val text: String) : PhotoEditAction

    // 光源 & 实验细节
    data class UpdateLightSource(val v: LightSource) : PhotoEditAction
    data class UpdateDetails(val v: String) : PhotoEditAction

    // 步骤
    data class UpdateStepName(val orderIndex: Int, val v: String) : PhotoEditAction
    data class UpdateStepIntervalMinute(val orderIndex: Int, val text: String) : PhotoEditAction
    data class UpdateStepConcValue(val orderIndex: Int, val text: String) : PhotoEditAction
    data class UpdateStepConcUnit(val orderIndex: Int, val unit: ConcUnit) : PhotoEditAction
    object AddStep : PhotoEditAction
    data class RemoveStep(val orderIndex: Int) : PhotoEditAction

    // 浏览态点击步骤跳转（由 VM 决定是否弹确认框/如何跳转）
    data class JumpStep(val index: Int) : PhotoEditAction

    object Edit : PhotoEditAction
    object Save : PhotoEditAction
}
