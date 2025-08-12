package com.mty.exptools.logic.model.syn

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "synthesis_step",
    foreignKeys = [
        ForeignKey(
            entity = SynthesisDraftEntity::class,
            parentColumns = ["id"],
            childColumns = ["draft_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["draft_id", "order_index"], unique = true)]
)
data class SynthesisStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,   // 自增
    @ColumnInfo(name = "draft_id") val draftId: Long,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    @ColumnInfo(name = "content") val content: String,

    // UI 输入单位（仅展示/录入使用）
    @ColumnInfo(name = "unit") val unit: String, // "MINUTE" / "HOUR"

    // —— 计时核心：持久化三字段 —— //
    @ColumnInfo(name = "required_millis") val requiredMillis: Long,
    @ColumnInfo(name = "accumulated_millis") val accumulatedMillis: Long,
    @ColumnInfo(name = "start_epoch_ms") val startEpochMs: Long? // null=暂停
)
