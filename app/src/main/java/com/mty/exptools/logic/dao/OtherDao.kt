package com.mty.exptools.logic.dao

import androidx.room.*
import com.mty.exptools.logic.model.other.OtherDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OtherDao {

    // 读取
    @Query("SELECT * FROM other_draft WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): OtherDraftEntity?

    @Query("SELECT * FROM other_draft ORDER BY id DESC")
    fun observeAll(): Flow<List<OtherDraftEntity>>

    // 写入
    @Upsert
    suspend fun upsert(entity: OtherDraftEntity): Long

    @Transaction
    suspend fun upsertDraft(draft: OtherDraftEntity): Long {
        val result = upsert(draft)
        return if (result > 0) result else draft.id
    }

    @Query("DELETE FROM other_draft WHERE id = :id")
    suspend fun deleteById(id: Long): Int

}