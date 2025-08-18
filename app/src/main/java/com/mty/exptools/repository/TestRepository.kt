package com.mty.exptools.repository

import com.mty.exptools.domain.syn.toDomain
import com.mty.exptools.domain.test.TestDraft
import com.mty.exptools.domain.test.toDomain
import com.mty.exptools.domain.test.toEntity
import com.mty.exptools.logic.dao.AppDatabase
import com.mty.exptools.logic.dao.SynthesisDao
import com.mty.exptools.logic.dao.TestDao
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TestRepository @Inject constructor() {
    val synDao: SynthesisDao = AppDatabase.get().synthesisDao()
    val testDao: TestDao = AppDatabase.get().testDao()

    fun observeAllSynDrafts() = synDao.observeAllDraftWithSteps()
        .map {
            it.map { it.toDomain() }
        }

    suspend fun getById(dbId: Long): TestDraft? = testDao.getById(dbId)?.toDomain()

    suspend fun upsert(draft: TestDraft): Long = testDao.upsertDraft(draft.toEntity())

    suspend fun deleteDraftByDbId(dbId: Long): Boolean = testDao.deleteById(dbId) > 0

    fun observeAllTestDrafts() = testDao.observeAll()
        .map {
            it.map { it.toDomain() }
        }

}