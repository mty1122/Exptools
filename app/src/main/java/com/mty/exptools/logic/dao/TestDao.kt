package com.mty.exptools.logic.dao

import androidx.room.*
import com.mty.exptools.logic.model.test.TestDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {

    // 读取
    @Query("SELECT * FROM test_draft WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TestDraftEntity?

    @Query("SELECT * FROM test_draft")
    fun observeAll(): Flow<List<TestDraftEntity>>

    // 写入
    @Upsert
    suspend fun upsert(entity: TestDraftEntity): Long

    @Transaction
    suspend fun upsertDraft(draft: TestDraftEntity): Long {
        val result = upsert(draft)
        return if (result > 0) result else draft.id
    }

    @Query("DELETE FROM test_draft WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}