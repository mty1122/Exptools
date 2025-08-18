package com.mty.exptools.repository

import com.mty.exptools.domain.syn.toDomain
import com.mty.exptools.domain.test.TestDraft
import com.mty.exptools.logic.dao.AppDatabase
import com.mty.exptools.logic.dao.SynthesisDao
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TestRepository @Inject constructor() {
    val synDao: SynthesisDao = AppDatabase.get().synthesisDao()
    // val testDao: TestDao = AppDatabase.get().testDao()

    fun observeAllSynDrafts() = synDao.observeAllDraftWithSteps()
        .map {
            it.map { it.toDomain() }
        }

    suspend fun getById(dbId: Long): TestDraft? = null
    suspend fun upsert(draft: TestDraft): Long = 0L
    suspend fun deleteDraftByDbId(dbId: Long): Boolean = false
    fun observeAllTestDrafts() = flowOf(emptyList<TestDraft>())

}