package com.mty.exptools.ui.home.center.list.item

data class ItemTestUiState(
    override val listItemId: Int,
    val materialName: String,
    val testInfo: String,
    val testDate: String,
    val rightTimes: Int,
    val status: ItemStatus
): ItemUiState
