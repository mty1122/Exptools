package com.mty.exptools.repository

import com.mty.exptools.domain.syn.SynthesisDraft
import com.mty.exptools.domain.syn.toDomain
import com.mty.exptools.domain.syn.toEntity
import com.mty.exptools.logic.dao.AppDatabase
import com.mty.exptools.logic.dao.SynthesisDao
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SynthesisRepository @Inject constructor(){
    val dao: SynthesisDao = AppDatabase.get().synthesisDao()

    suspend fun getByMaterialName(name: String): SynthesisDraft? =
        dao.getByMaterialName(name)?.toDomain()

    suspend fun upsert(draft: SynthesisDraft) {
        dao.upsertDraftWithSteps(
            draft = draft.toEntity(),
            steps = draft.steps.map { it.toEntity(orderIndex = it.orderIndex) }
        )
    }

    suspend fun updateStepsTimerByIndex(
        materialName: String,
        orderIndexes: List<Int>,
        accumulatedMillis: Long,
        startEpochMs: Long?
    ) {
        val draftId = dao.findDraftIdByName(materialName)!!
        dao.updateStepsTimerByIndex(draftId, orderIndexes, accumulatedMillis, startEpochMs)
    }

    suspend fun deleteDraftByName(name: String): Boolean = dao.deleteDraftByName(name) > 0

    suspend fun setCompletedAt(materialName: String, completedTime: Long?) {
        val draftId = dao.findDraftIdByName(materialName)!!
        dao.setCompletedAt(draftId, completedTime, System.currentTimeMillis())
    }

    suspend fun findDraftIdByName(name: String): Long? = dao.findDraftIdByName(name)

    fun observeAllDrafts() = dao.observeAllDraftWithSteps()
        .map {
            it.map { it.toDomain() }
        }
}