package com.mty.exptools.repository

import com.mty.exptools.domain.photo.toDomain
import com.mty.exptools.domain.syn.toDomain
import com.mty.exptools.domain.test.toDomain
import com.mty.exptools.logic.dao.AppDatabase
import com.mty.exptools.logic.dao.PhotoDao
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ListRepository @Inject constructor() {
    val synthesisDao = AppDatabase.get().synthesisDao()
    val photoDao: PhotoDao = AppDatabase.get().photoDao()
    val testDao = AppDatabase.get().testDao()

    fun observeAllSynDraft() = synthesisDao.observeAllDraftWithSteps()
        .map {
            it.map { it.toDomain() }
        }
    fun observeAllPhotoDraft() = photoDao.observeAllDraftWithSteps()
        .map {
            it.map { it.toDomain() }
        }
    fun observeAllTestDraft() = testDao.observeAll()
        .map {
            it.map { it.toDomain() }
        }

}