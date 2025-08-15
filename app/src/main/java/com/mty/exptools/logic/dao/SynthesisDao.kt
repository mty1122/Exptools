package com.mty.exptools.logic.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.mty.exptools.logic.model.syn.SynthesisDraftEntity
import com.mty.exptools.logic.model.syn.SynthesisDraftWithSteps
import com.mty.exptools.logic.model.syn.SynthesisStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SynthesisDao {

    /**
     * 获取草稿 + 步骤
     */
    @Transaction
    @Query("SELECT * FROM synthesis_draft WHERE material_name = :name LIMIT 1")
    suspend fun getByMaterialName(name: String): SynthesisDraftWithSteps?

    /* 内部函数 start */
    @Query("SELECT id FROM synthesis_draft WHERE material_name = :name LIMIT 1")
    suspend fun findDraftIdByName(name: String): Long?

    @Query("SELECT * FROM synthesis_step WHERE draft_id = :draftId ORDER BY order_index")
    suspend fun getStepsByDraft(draftId: Long): List<SynthesisStepEntity>

    @Upsert
    suspend fun upsertDraft(draft: SynthesisDraftEntity): Long

    @Upsert
    suspend fun upsertSteps(steps: List<SynthesisStepEntity>)

    @Query("DELETE FROM synthesis_step WHERE draft_id = :draftId AND id NOT IN (:keepIds)")
    suspend fun deleteRemovedSteps(draftId: Long, keepIds: List<Long>)
    /* 内部函数 end */

    /**
     * 原子写入：草稿 + 步骤（替换/新增/删除）
     * @param draft 不需要id，由数据库自增，材料名称为索引
     * @param steps 不需要id，由数据库自增，draftId 为外键，orderIndex为索引
     */
    @Transaction
    suspend fun upsertDraftWithSteps(
        draft: SynthesisDraftEntity,
        steps: List<SynthesisStepEntity>
    ): Long {
        var draftId = findDraftIdByName(draft.materialName)
        if (draftId == null)
            draftId = upsertDraft(draft) // 新增
        else
            upsertDraft(draft.copy(id = draftId)) // 修改

        // 绑定外键 & 排序号
        val oldStep = getStepsByDraft(draftId)
        val normalized = steps
            .sortedBy { it.orderIndex }
            .mapIndexed { idx, s ->
                s.copy(
                    id = if (idx < oldStep.size) oldStep[idx].id else 0L,
                    draftId = draftId,
                    orderIndex = idx // 统一规范为 0..N
                )
            }

        deleteRemovedSteps(draftId, normalized.map { it.id })
        upsertSteps(normalized)
        return draftId
    }

    @Query("""
        UPDATE synthesis_step
        SET accumulated_millis = :acc, start_epoch_ms = :start
        WHERE draft_id = :draftId AND order_index IN (:orderIndexes)
    """)
    suspend fun updateStepsTimerByIndex(
        draftId: Long,
        orderIndexes: List<Int>,
        acc: Long,
        start: Long?
    ): Int

    @Query("""
        UPDATE synthesis_step
        SET accumulated_millis = required_millis, start_epoch_ms = NULL
        WHERE draft_id = :draftId AND order_index IN (:orderIndexes)
    """)
    suspend fun completeStepsByIndex(draftId: Long, orderIndexes: List<Int>): Int

    @Query("DELETE FROM synthesis_draft WHERE material_name = :name")
    suspend fun deleteDraftByName(name: String): Int

    @Query("UPDATE synthesis_draft SET completed_at = :completedTime, updated_at = :updateTime WHERE id = :id")
    suspend fun setCompletedAt(id: Long, completedTime: Long?, updateTime: Long)


    // 用于列表读取
    @Query("SELECT * FROM synthesis_draft")
    fun observeAllDraftWithSteps(): Flow<List<SynthesisDraftWithSteps>>

}
