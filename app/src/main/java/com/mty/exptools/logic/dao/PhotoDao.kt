package com.mty.exptools.logic.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.mty.exptools.logic.model.photo.PhotoDraftEntity
import com.mty.exptools.logic.model.photo.PhotoDraftWithSteps
import com.mty.exptools.logic.model.photo.PhotoStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Transaction
    @Query("SELECT * FROM photo_draft WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PhotoDraftWithSteps?

    // 辅助函数 start
    @Upsert
    suspend fun upsertDraft(draft: PhotoDraftEntity): Long

    @Upsert
    suspend fun upsertSteps(steps: List<PhotoStepEntity>)

    @Query("SELECT * FROM photo_step WHERE draft_id = :draftId ORDER BY order_index")
    suspend fun getStepsByDraft(draftId: Long): List<PhotoStepEntity>

    @Query("DELETE FROM photo_step WHERE draft_id = :draftId AND id NOT IN (:keepIds)")
    suspend fun deleteRemovedSteps(draftId: Long, keepIds: List<Long>)
    // 辅助函数 end

    @Transaction
    suspend fun upsertDraftWithSteps(
        draft: PhotoDraftEntity,
        steps: List<PhotoStepEntity>
    ): Long {
        val result = upsertDraft(draft)
        val draftId = if (result > 0) result else draft.id

        val oldSteps = getStepsByDraft(draftId)
        val normalized = steps
            .sortedBy { it.orderIndex }
            .mapIndexed { idx, s ->
                s.copy(
                    id = if (idx < oldSteps.size) oldSteps[idx].id else 0L,
                    draftId = draftId,
                    orderIndex = idx                 // 强制 0..N
                )
            }

        deleteRemovedSteps(draftId, normalized.map { it.id })
        upsertSteps(normalized)
        return draftId
    }

    // 单字段更新：完成时间
    @Query("UPDATE photo_draft SET completed_at = :completedAt, updated_at = :updateTime WHERE id = :draftId")
    suspend fun setCompletedAt(draftId: Long, completedAt: Long?, updateTime: Long)

    // 批量更新计时（按 order_index 定位）
    @Query("""
        UPDATE photo_step
        SET accumulated_millis = :acc, start_epoch_ms = :start
        WHERE draft_id = :draftId AND order_index IN (:orderIndexes)
    """)
    suspend fun updateStepsTimerByIndex(
        draftId: Long,
        orderIndexes: List<Int>,
        acc: Long,
        start: Long?
    ): Int

    // 删除整个草稿（级联删除步骤）
    @Query("DELETE FROM photo_draft WHERE id = :id")
    suspend fun deleteDraftById(id: Long): Int

    @Transaction
    @Query("SELECT * FROM photo_draft ORDER BY id DESC")
    fun observeAllDraftWithSteps(): Flow<List<PhotoDraftWithSteps>>

    @Query("""
        UPDATE photo_step
        SET accumulated_millis = required_millis, start_epoch_ms = NULL
        WHERE draft_id = :draftId AND order_index IN (:orderIndexes)
    """)
    suspend fun completeStepsByIndex(draftId: Long, orderIndexes: List<Int>): Int

    @Transaction
    @Query("SELECT * FROM photo_draft WHERE completed_at BETWEEN :start AND :end")
    suspend fun getDraftsByTimeRange(start: Long, end: Long): List<PhotoDraftWithSteps>

}