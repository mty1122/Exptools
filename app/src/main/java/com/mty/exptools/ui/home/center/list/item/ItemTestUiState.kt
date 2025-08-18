package com.mty.exptools.ui.home.center.list.item

import com.mty.exptools.util.MillisTime

data class ItemTestUiState(
    override val listItemId: Int,
    val dbId: Long = 0L,
    val materialName: String,
    val testInfo: String,
    val testDate: String,
    override val rightTime: MillisTime,
    override val status: ItemStatus
): ItemUiState
