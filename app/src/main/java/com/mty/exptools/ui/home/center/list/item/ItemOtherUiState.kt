package com.mty.exptools.ui.home.center.list.item

import com.mty.exptools.util.MillisTime

data class ItemOtherUiState(
    override val listItemId: Int,
    val dbId: Long = 0L,
    override val title: String,
    override val info: String = "",
    override val rightTime: MillisTime,
    override val status: ItemStatus
): ItemUiState
