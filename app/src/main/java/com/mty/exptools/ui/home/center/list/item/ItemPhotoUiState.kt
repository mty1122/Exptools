package com.mty.exptools.ui.home.center.list.item

import com.mty.exptools.enum.LightSource

data class ItemPhotoUiState(
    val id: Int,
    val materialName: String,
    val targetPollutant: String,
    val pollutantWavelength: Int,
    val lightSource: LightSource,
    val elapsedMinutes: Int = 1,
    val totalMinutes: Int = 1,
    val rightTimes: Int,
    val status: ItemStatus
)