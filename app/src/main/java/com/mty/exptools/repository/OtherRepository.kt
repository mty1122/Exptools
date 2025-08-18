package com.mty.exptools.repository

import com.mty.exptools.domain.other.OtherDraft
import com.mty.exptools.domain.other.toDomain
import com.mty.exptools.domain.other.toEntity
import com.mty.exptools.logic.dao.AppDatabase
import com.mty.exptools.logic.dao.OtherDao
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OtherRepository @Inject constructor() {
    val dao: OtherDao = AppDatabase.get().otherDao()

    suspend fun getById(dbId: Long): OtherDraft? = dao.getById(dbId)?.toDomain()

    suspend fun upsert(draft: OtherDraft): Long = dao.upsertDraft(draft.toEntity())

    suspend fun deleteDraftByDbId(dbId: Long): Boolean = dao.deleteById(dbId) > 0

    fun observeAllOtherDrafts() = dao.observeAll()
        .map {
            it.map { it.toDomain() }
        }
}