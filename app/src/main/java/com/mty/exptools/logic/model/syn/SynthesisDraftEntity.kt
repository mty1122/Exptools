package com.mty.exptools.logic.model.syn

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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

    // 可选：为了恢复时直接高亮当前步
    //@ColumnInfo(name = "current_step_index") val currentStepIndex: Int = 0,

    // 可选：UI 运行状态通常可根据当前步 timer 推断；如需强持久化，也可单独落
    //@ColumnInfo(name = "running") val running: Boolean = false,

    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,

    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)

