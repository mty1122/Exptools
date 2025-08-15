package com.mty.exptools.domain.photo

enum class ConcUnit { ABSORBANCE_A, MG_L }

data class PhotocatalysisDraft(
    val catalystName: String = "",
    val target: PhotocatalysisTarget = PhotocatalysisTarget(),
    val light: LightSource = LightSource(""),
    val details: String = "",
    val steps: List<PhotocatalysisStep> = listOf(PhotocatalysisStep())
)
