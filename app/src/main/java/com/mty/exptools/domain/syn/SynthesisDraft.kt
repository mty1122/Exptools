package com.mty.exptools.domain.syn

data class SynthesisDraft(
    val materialName: String = "",
    val rawMaterials: String = "",
    val conditionSummary: String = "",
    val expDetails: String = "",
    val steps: List<SynthesisStep> = listOf(SynthesisStep())
) {
    val currentStepIndex: Int
        get() = steps.firstOrNull { step -> !step.timer.isFinished() }
            ?.orderIndex
            ?: steps.lastOrNull()?.orderIndex
            ?: 0

    val isFinished: Boolean
        get() = steps.lastOrNull()?.timer?.isFinished() == true
}
