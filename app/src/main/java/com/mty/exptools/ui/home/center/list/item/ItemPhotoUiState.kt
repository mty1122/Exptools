package com.mty.exptools.ui.home.center.list.item

import com.mty.exptools.domain.photo.LightSource
import com.mty.exptools.domain.photo.PhotoTargetMaterial
import com.mty.exptools.util.MillisTime
import com.mty.exptools.util.Time

data class ItemPhotoUiState(
    override val listItemId: Int,
    val dbId: Long = 0L,
    val materialName: String,
    val target: PhotoTargetMaterial,
    val lightSource: LightSource,
    val progress: Float = 1f,
    val remainTime: Time = MillisTime(0).toTime(),
    val performanceList: List<String> = emptyList(),
    override val rightTime: MillisTime,
    override val status: ItemStatus
): ItemUiState