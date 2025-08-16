package com.mty.exptools.domain.photo

import com.mty.exptools.domain.StepTimer

data class PhotocatalysisStep(
    val orderIndex: Int = 0,
    val name: String = "",
    val concValueText: String = "",
    val concUnit: ConcUnit = ConcUnit.MG_L,
    val timer: StepTimer = StepTimer(requiredMillis = 30 * 60 * 1000L) // 默认30分钟
) {
    fun duration() = timer.requiredMillis / 1000 / 60
}
