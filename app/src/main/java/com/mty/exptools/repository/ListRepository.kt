package com.mty.exptools.repository

import com.mty.exptools.domain.syn.toDomain
import com.mty.exptools.logic.dao.AppDatabase
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ListRepository @Inject constructor() {
    val synthesisDao = AppDatabase.get().synthesisDao()

    fun observeAllSynDraft() = synthesisDao.observeAllDraftWithSteps()
        .map {
            it.map { it.toDomain() }
        }
}