package com.mty.exptools.ui.home.center.list.item

import com.mty.exptools.enum.LightSource
import com.mty.exptools.enum.Pollutant

data class ItemPhotoUiState(
    override val listItemId: Int,
    val materialName: String,
    val targetPollutant: Pollutant,
    val lightSource: LightSource,
    val elapsedMinutes: Int = 1,
    val totalMinutes: Int = 1,
    val rightTimes: Int,
    val status: ItemStatus
): ItemUiState