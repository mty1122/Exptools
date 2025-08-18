package com.mty.exptools.domain.other

import com.mty.exptools.logic.model.other.OtherDraftEntity

data class OtherDraft(
    val dbId: Long = 0L,
    val taskName: String = "",
    val summary: String = "",
    val details: String = "",
    val completedAt: Long = System.currentTimeMillis()   // 结束时间（毫秒）
)

fun OtherDraft.toEntity() = OtherDraftEntity(
    id = dbId,
    taskName = taskName,
    summary = summary,
    details = details,
    completedAt = completedAt
)

fun OtherDraftEntity.toDomain() = OtherDraft(
    dbId = id,
    taskName = taskName,
    summary = summary,
    details = details,
    completedAt = completedAt
)