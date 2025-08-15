package com.mty.exptools.domain.photo

data class PhotocatalysisStep(
    val orderIndex: Int = 0,
    val name: String = "",
    val intervalMinuteText: String = "",
    val concValueText: String = "",
    val concUnit: ConcUnit = ConcUnit.MG_L
)
