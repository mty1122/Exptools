package com.mty.exptools.ui.home.center.list.item

import com.mty.exptools.util.MillisTime

sealed interface ItemUiState {
    val listItemId: Int
    val rightTime: MillisTime
    val status: ItemStatus
}