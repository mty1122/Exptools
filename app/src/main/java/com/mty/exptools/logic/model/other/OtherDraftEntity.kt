package com.mty.exptools.logic.model.other

import androidx.room.*

@Entity(tableName = "other_draft")
data class OtherDraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "task_name") val taskName: String,
    @ColumnInfo(name = "summary") val summary: String,
    @ColumnInfo(name = "details") val details: String,
    @ColumnInfo(name = "completed_at") val completedAt: Long
)