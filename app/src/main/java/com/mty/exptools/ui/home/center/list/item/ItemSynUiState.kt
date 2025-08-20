package com.mty.exptools.ui.home.center.list.item

import com.mty.exptools.util.MillisTime

data class ItemSynUiState(
    override val listItemId: Int,
    val materialName: String,
    override val title: String,
    override val info: String = "",
    val progress: Float = 0f,
    override val rightTime: MillisTime,
    override val status: ItemStatus
): ItemUiState
