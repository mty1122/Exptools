package com.mty.exptools.ui.home.center.list.item

data class ItemSynUiState(
    override val listItemId: Int,
    val materialName: String,
    val targetStep: String = "",
    val nextStep: String = "",
    val completeInfo: String = "",
    val progress: Float = 0f,
    val rightTimes: Int,
    val status: ItemStatus
): ItemUiState
