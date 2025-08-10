package com.mty.exptools.ui.home.center.list.item

data class ItemOtherUiState(
    override val listItemId: Int,
    val title: String,
    val info: String,
    val endDate: String,
    val rightTimes: Int,
    val status: ItemStatus
): ItemUiState
