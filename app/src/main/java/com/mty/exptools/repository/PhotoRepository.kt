package com.mty.exptools.repository

import com.mty.exptools.domain.photo.PhotocatalysisDraft
import com.mty.exptools.domain.syn.toDomain
import com.mty.exptools.logic.dao.AppDatabase
import com.mty.exptools.logic.dao.SynthesisDao
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PhotoRepository @Inject constructor() {
    val synDao: SynthesisDao = AppDatabase.get().synthesisDao()
    fun observeAllSynDrafts() = synDao.observeAllDraftWithSteps()
        .map {
            it.map { it.toDomain() }
        }

    // TODO 后台待开发
    fun loadById(dbId: Long): PhotocatalysisDraft? = null
    fun upsert(draft: PhotocatalysisDraft) {}
}