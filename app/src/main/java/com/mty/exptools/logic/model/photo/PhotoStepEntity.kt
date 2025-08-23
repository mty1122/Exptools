package com.mty.exptools.logic.model.photo

import androidx.room.*
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "photo_step",
    foreignKeys = [
        ForeignKey(
            entity = PhotoDraftEntity::class,
            parentColumns = ["id"],
            childColumns = ["draft_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["draft_id"]),
        Index(value = ["draft_id", "order_index"], unique = true)
    ]
)
data class PhotoStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "draft_id") val draftId: Long,
    @ColumnInfo(name = "order_index") val orderIndex: Int,

    // UI 字段
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "conc_value_text") val concValueText: String,
    @ColumnInfo(name = "conc_unit") val concUnit: String, // "ABSORBANCE_A"/"MG_L"

    // 计时（与 StepTimer 对齐）
    @ColumnInfo(name = "required_millis") val requiredMillis: Long,
    @ColumnInfo(name = "accumulated_millis") val accumulatedMillis: Long,
    @ColumnInfo(name = "start_epoch_ms") val startEpochMs: Long? // null=暂停
)