package com.mty.exptools.repository

import androidx.room.withTransaction
import com.mty.exptools.domain.photo.toDomain
import com.mty.exptools.logic.dao.AppDatabase
import com.mty.exptools.logic.dao.PhotoDao
import com.mty.exptools.logic.dao.PreferenceDao
import com.mty.exptools.logic.dao.SynthesisDao
import com.mty.exptools.logic.export.model.PhotoDraftWithSynthesis
import com.mty.exptools.logic.export.model.toExport
import com.mty.exptools.logic.model.syn.SynthesisDraftEntity
import javax.inject.Inject

class MoreRepository @Inject constructor() {
    private val prefsDao: PreferenceDao = PreferenceDao
    private val photoDao: PhotoDao = AppDatabase.get().photoDao()
    private val synDao: SynthesisDao = AppDatabase.get().synthesisDao()

    suspend fun setAutoRefreshSeconds(seconds: Int) = prefsDao.setAutoRefreshSeconds(seconds)

    suspend fun getAutoRefreshSeconds() = prefsDao.getAutoRefreshSeconds()

    suspend fun setExportDirUri(uri: String) = prefsDao.setExportDirUri(uri)

    suspend fun getExportDirUri() = prefsDao.getExportDirUri()

    suspend fun getPhotoDraftsWithSynByTimeRange(start: Long, end: Long): List<PhotoDraftWithSynthesis> =
        AppDatabase.get().withTransaction {
            // 1. 查询时间范围内的所有 photo_draft
            val photoDrafts = photoDao.getDraftsByTimeRange(start, end)

            // 2. 转换成 domain，然后一个 catalystName 对应多个 photoDraft
            val groupedByCatalyst = photoDrafts
                .map { it.toDomain() }
                .groupBy { it.catalystName }

            // 3. 遍历 catalystName，查对应的 synthesisDraft
            groupedByCatalyst.map { (catalystName, photoDraftList) ->
                val synDraft = synDao.getWithoutStepsByMaterialName(catalystName)
                    ?: SynthesisDraftEntity(
                        materialName = catalystName,
                        conditionSummary = "",
                        rawMaterials = "",
                        expDetails = ""
                    )

                // 4. 合并成最终导出对象
                (synDraft to photoDraftList).toExport()
            }
        }.sortedBy { it.catalystName }
}