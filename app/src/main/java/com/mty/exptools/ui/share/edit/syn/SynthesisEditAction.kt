package com.mty.exptools.ui.share.edit.syn

import android.icu.util.TimeUnit

sealed interface SynthesisAction {
    data class UpdateMaterial(val v: String) : SynthesisAction
    data class UpdateRaw(val v: String) : SynthesisAction
    data class UpdateSummary(val v: String) : SynthesisAction

    data class UpdateStepContent(val id: Long, val v: String) : SynthesisAction
    /** 将“数值+单位”转换成 requiredMillis 写入计时器 */
    data class UpdateStepDuration(val id: Long, val numberText: String, val unit: TimeUnit) : SynthesisAction
    data class UpdateStepUnit(val id: Long, val unit: TimeUnit) : SynthesisAction

    object AddStep : SynthesisAction
    data class RemoveStep(val id: Long) : SynthesisAction

    object Save : SynthesisAction
    object Edit : SynthesisAction

    /** 开/停当前步骤（只改 StepTimer 三字段） */
    object ToggleRun : SynthesisAction
    /** 将当前步骤标记完成 → 自动跳到下一步并暂停 */
    object CompleteCurrentStep : SynthesisAction
    /** 可选：手动指定当前步骤（浏览模式下高亮/开始于某步） */
    data class SetCurrentIndex(val index: Int) : SynthesisAction
}
