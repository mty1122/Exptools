package com.mty.exptools.domain.photo

data class PhotocatalysisTarget(
    val name: String = "",
    val wavelengthNm: String = "",      // 可空文本
    val initialConcValue: String = "",  // 初始浓度文本；单位见 initialConcUnit
    val initialConcUnit: ConcUnit = ConcUnit.MG_L,
    val stdCurveK: String = "",         // 标准曲线 y=kx+b 的 k；可空文本
    val stdCurveB: String = ""          // 标准曲线 y=kx+b 的 b；可空文本
)
