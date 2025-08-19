package com.mty.exptools.repository

import com.mty.exptools.logic.export.model.toExport
import com.mty.exptools.domain.photo.toDomain
import com.mty.exptools.logic.dao.AppDatabase
import com.mty.exptools.logic.dao.PhotoDao
import com.mty.exptools.logic.dao.PreferenceDao
import javax.inject.Inject

class MoreRepository @Inject constructor() {
    private val prefsDao: PreferenceDao = PreferenceDao
    private val photoDao: PhotoDao = AppDatabase.get().photoDao()

    suspend fun setAutoRefreshSeconds(seconds: Int) = prefsDao.setAutoRefreshSeconds(seconds)

    suspend fun getAutoRefreshSeconds() = prefsDao.getAutoRefreshSeconds()

    suspend fun setExportDirUri(uri: String) = prefsDao.setExportDirUri(uri)

    suspend fun getExportDirUri() = prefsDao.getExportDirUri()

    suspend fun getPhotoDraftsByTimeRange(start: Long, end: Long) =
        photoDao.getDraftsByTimeRange(start, end)
            .map { it.toDomain().toExport() }
}