package com.mty.exptools.logic.model.test

import androidx.room.*
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "test_draft")
data class TestDraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "material_name") val materialName: String,
    @ColumnInfo(name = "summary") val summary: String,
    @ColumnInfo(name = "details") val details: String,
    @ColumnInfo(name = "start_at") val startAt: Long
)