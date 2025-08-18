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

    val performanceList: List<String>
        get() = getPerfList()

    private fun getPerfList(): List<String> {
        if (!isFinished) return emptyList()
        val perfList = List(size = steps.size) { idx ->
            val step = steps[idx]
            val c0 = toMgL(
                valueText = target.initialConcValue,
                unit = target.initialConcUnit,
                kText = target.stdCurveK,
                bText = target.stdCurveB
            )
            val ci = toMgL(
                valueText = step.concValueText,
                unit = step.concUnit,
                kText = target.stdCurveK,
                bText = target.stdCurveB
            )
            val perf = calcPerformance(c0, ci)
            if (perf != null) "${"%.1f".format((perf * 100).coerceIn(0.0, 100.0))}%"
            else return emptyList()
        }
        return perfList
    }
}
