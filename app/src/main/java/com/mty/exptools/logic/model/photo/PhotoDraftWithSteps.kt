package com.mty.exptools.logic.model.photo

import androidx.room.*
import kotlinx.serialization.Serializable

@Serializable
data class PhotoDraftWithSteps(
    @Embedded val draft: PhotoDraftEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "draft_id",
        entity = PhotoStepEntity::class
    )
    val steps: List<PhotoStepEntity>
)