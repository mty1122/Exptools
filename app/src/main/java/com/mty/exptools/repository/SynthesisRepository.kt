package com.mty.exptools.repository

import com.mty.exptools.ui.share.edit.syn.SynthesisDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SynthesisRepository @Inject constructor(){
    //suspend fun getByMaterialName(name: String): SynthesisDraft?
    fun observeByMaterialName(name: String): Flow<SynthesisDraft?> = flow { SynthesisDraft() }
    suspend fun upsert(draft: SynthesisDraft) {}
}