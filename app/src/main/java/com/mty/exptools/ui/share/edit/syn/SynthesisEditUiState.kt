package com.mty.exptools.ui.share.edit.syn

import android.icu.util.TimeUnit
import com.mty.exptools.util.StepTimer

enum class SynthesisMode { EDIT, VIEW }

data class SynthesisStep(
    val id: Long = System.nanoTime(),
    val content: String = "",
    val unit: TimeUnit = TimeUnit.MINUTE,                 // 仅用于 UI 录入
    val timer: StepTimer = StepTimer(requiredMillis = 30 * 60 * 1000L) // 默认30分钟
) {
    fun duration() = if (unit == TimeUnit.MINUTE) {
        timer.requiredMillis / 1000 / 60
    } else {
        timer.requiredMillis / 1000 / 60 / 60
    }
}

data class SynthesisDraft(
    val materialName: String = "",
    val rawMaterials: String = "",
    val conditionSummary: String = "",
    val steps: List<SynthesisStep> = listOf(SynthesisStep())
)

data class SynthesisEditUiState(
    val mode: SynthesisMode = SynthesisMode.EDIT,
    val draft: SynthesisDraft = SynthesisDraft(),
    val currentStepIndex: Int = 0,
    val running: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)
