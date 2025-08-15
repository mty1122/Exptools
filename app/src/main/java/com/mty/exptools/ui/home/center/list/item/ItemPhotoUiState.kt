package com.mty.exptools.ui.home.center.list.item

import com.mty.exptools.domain.photo.LightSource
import com.mty.exptools.domain.photo.PhotoTargetMaterial

data class ItemPhotoUiState(
    override val listItemId: Int,
    val materialName: String,
    val target: PhotoTargetMaterial,
    val lightSource: LightSource,
    val elapsedMinutes: Int = 1,
    val totalMinutes: Int = 1,
    val progress: Float = 1f,
    val rightTimes: Int,
    val status: ItemStatus
): ItemUiState