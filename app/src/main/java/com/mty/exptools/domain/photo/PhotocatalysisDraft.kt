package com.mty.exptools.domain.photo

enum class ConcUnit { ABSORBANCE_A, MG_L }

data class PhotocatalysisDraft(
    val dbId: Long = 0L,
    val catalystName: String = "",
    val target: PhotocatalysisTarget = PhotocatalysisTarget(),
    val light: LightSource = LightSource(""),
    val details: String = "",
    val completedAt: Long? = null,
    val steps: List<PhotocatalysisStep> = listOf(PhotocatalysisStep())
) {
    val currentStepIndex: Int
        get() = steps.firstOrNull { step -> !step.timer.isFinished() }
            ?.orderIndex
            ?: steps.lastOrNull()?.orderIndex
            ?: 0

    val isFinished: Boolean
        get() = steps.lastOrNull()?.timer?.isFinished() == true
}
