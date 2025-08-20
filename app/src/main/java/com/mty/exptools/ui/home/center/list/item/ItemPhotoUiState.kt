package com.mty.exptools.ui.home.center.list.item

import com.mty.exptools.util.MillisTime

data class ItemPhotoUiState(
    override val listItemId: Int,
    val dbId: Long = 0L,
    override val title: String,
    override val info: String = "",
    val progress: Float = 1f,
    override val rightTime: MillisTime,
    override val status: ItemStatus
): ItemUiState