package com.mty.exptools.ui.home.center.list.item

import com.mty.exptools.util.MillisTime

data class ItemOtherUiState(
    override val listItemId: Int,
    val title: String,
    val info: String,
    val endDate: String,
    override val rightTime: MillisTime,
    override val status: ItemStatus
): ItemUiState
