package com.mty.exptools.ui.home.center.list.item

import com.mty.exptools.util.MillisTime

data class ItemSynUiState(
    override val listItemId: Int,
    val materialName: String,
    val targetStep: String = "",
    val nextStep: String = "",
    val completeInfo: String = "",
    val progress: Float = 0f,
    override val rightTime: MillisTime,
    override val status: ItemStatus
): ItemUiState
