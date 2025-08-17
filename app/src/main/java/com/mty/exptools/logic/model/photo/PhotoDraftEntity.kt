package com.mty.exptools.logic.model.photo

import androidx.room.*

@Entity(tableName = "photo_draft")
data class PhotoDraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,          // = PhotocatalysisDraft.dbId
    @ColumnInfo(name = "catalyst_name") val catalystName: String,

    // Target
    @ColumnInfo(name = "target_name") val targetName: String,
    @ColumnInfo(name = "target_wavelength_nm") val targetWavelengthNm: String,
    @ColumnInfo(name = "initial_conc_value") val initialConcValue: String,
    @ColumnInfo(name = "initial_conc_unit") val initialConcUnit: String, // "ABSORBANCE_A"/"MG_L"
    @ColumnInfo(name = "std_curve_k") val stdCurveK: String,
    @ColumnInfo(name = "std_curve_b") val stdCurveB: String,

    // Light (value class → TEXT)
    @ColumnInfo(name = "light") val light: String,

    // Others
    @ColumnInfo(name = "details") val details: String,
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
