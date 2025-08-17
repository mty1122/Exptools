package com.mty.exptools.repository

import com.mty.exptools.domain.photo.PhotocatalysisDraft
import com.mty.exptools.domain.photo.toEntity
import com.mty.exptools.domain.photo.toDomain
import com.mty.exptools.domain.syn.toDomain
import com.mty.exptools.logic.dao.AppDatabase
import com.mty.exptools.logic.dao.PhotoDao
import com.mty.exptools.logic.dao.SynthesisDao
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PhotoRepository @Inject constructor() {
    val synDao: SynthesisDao = AppDatabase.get().synthesisDao()
    val photoDao: PhotoDao = AppDatabase.get().photoDao()

    fun observeAllSynDrafts() = synDao.observeAllDraftWithSteps()
        .map {
            it.map { it.toDomain() }
        }

    suspend fun getById(dbId: Long): PhotocatalysisDraft? = photoDao.getById(dbId)?.toDomain()

    suspend fun upsert(draft: PhotocatalysisDraft): Long {
        val draftEntity = draft.toEntity()
        val stepsEntity = draft.steps.map { it.toEntity(draftEntity.id) }
        return photoDao.upsertDraftWithSteps(draftEntity, stepsEntity)
    }

    suspend fun updateStepsTimerByIndex(
        dbId: Long, orderIndexes: List<Int>, accumulatedMillis: Long, startEpochMs: Long?
    ) {
        photoDao.updateStepsTimerByIndex(dbId, orderIndexes, accumulatedMillis, startEpochMs)
    }

    suspend fun completeStepsByIndex(dbId: Long, orderIndexes: List<Int>) {
        photoDao.completeStepsByIndex(dbId, orderIndexes)
    }

    suspend fun deleteDraftByDbId(dbId: Long): Boolean = photoDao.deleteDraftById(dbId) > 0

    suspend fun setCompletedAt(dbId: Long, completedAt: Long?) {
        photoDao.setCompletedAt(dbId, completedAt, System.currentTimeMillis())
    }

    fun observeAllPhotoDrafts() = photoDao.observeAllDraftWithSteps()
        .map {
            it.map { it.toDomain() }
        }
}