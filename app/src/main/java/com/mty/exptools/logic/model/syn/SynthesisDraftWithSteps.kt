package com.mty.exptools.logic.model.syn

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Serializable
data class SynthesisDraftWithSteps(
    @Embedded val draft: SynthesisDraftEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "draft_id",
        entity = SynthesisStepEntity::class
    )
    val steps: List<SynthesisStepEntity>
)