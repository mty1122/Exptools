package com.mty.exptools.ui.home.center.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mty.exptools.enum.LightSource
import com.mty.exptools.enum.Pollutant
import com.mty.exptools.ui.home.center.list.item.ItemPhotoUiState
import com.mty.exptools.ui.home.center.list.item.ItemPhotocatalysis
import com.mty.exptools.ui.home.center.list.item.ItemStatus
import com.mty.exptools.ui.home.center.list.item.ItemUiState

@Composable
fun ListScreen() {
    val uiState1 = ItemPhotoUiState(
        listItemId = 1,
        materialName = "钨酸铋-实验2-22",
        targetPollutant = Pollutant.TC,
        lightSource = LightSource.XENON_L,
        elapsedMinutes = 70,
        totalMinutes = 90,
        rightTimes = 20,
        status = ItemStatus.STATUS_START
    )
    val uiState2 = ItemPhotoUiState(
        listItemId = 2,
        materialName = "钨酸铋-实验2-21",
        targetPollutant = Pollutant.TC,
        lightSource = LightSource.XENON_L,
        rightTimes = 1,
        status = ItemStatus.STATUS_COMPLETE
    )

    val itemUiStateList = listOf<ItemUiState>(uiState1, uiState2)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(itemUiStateList, key = { it.listItemId }) { uiState ->
            when (uiState) {
                is ItemPhotoUiState -> ItemPhotocatalysis(uiState)
            }
        }
    }
}