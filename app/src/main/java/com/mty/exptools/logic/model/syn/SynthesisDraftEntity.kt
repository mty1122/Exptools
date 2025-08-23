package com.mty.exptools.logic.model.syn

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "synthesis_draft",
    indices = [Index(value = ["material_name"], unique = true)]
)
data class SynthesisDraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "material_name") val materialName: String,
    @ColumnInfo(name = "raw_materials") val rawMaterials: String,
    @ColumnInfo(name = "condition_summary") val conditionSummary: String,
    @ColumnInfo(name = "exp_details") val expDetails: String,

    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)

