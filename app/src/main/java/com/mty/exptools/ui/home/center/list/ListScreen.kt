package com.mty.exptools.ui.home.center.list

import androidx.compose.runtime.Composable
import com.mty.exptools.enum.LightSource
import com.mty.exptools.ui.home.center.list.item.ItemPhotoUiState
import com.mty.exptools.ui.home.center.list.item.ItemPhotocatalysis
import com.mty.exptools.ui.home.center.list.item.ItemStatus

@Composable
fun ListScreen() {
    val uiState = ItemPhotoUiState(
        id = 1,
        materialName = "钨酸铋-实验2-22",
        targetPollutant = "TC",
        pollutantWavelength = 357,
        lightSource = LightSource.XENON_L,
        elapsedMinutes = 70,
        totalMinutes = 90,
        rightTimes = 20,
        status = ItemStatus.STATUS_START
    )
    ItemPhotocatalysis(uiState)
}