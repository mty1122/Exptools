package com.mty.exptools.domain.test

import com.mty.exptools.logic.model.test.TestDraftEntity

data class TestDraft(
    val dbId: Long = 0L,
    val materialName: String = "",
    val summary: String = "",
    val details: String = "",
    val startAt: Long = System.currentTimeMillis()   // 开始时间（毫秒）
)

fun TestDraft.toEntity() = TestDraftEntity(
    id = dbId,
    materialName = materialName,
    summary = summary,
    details = details,
    startAt = startAt
)

fun TestDraftEntity.toDomain() = TestDraft(
    dbId = id,
    materialName = materialName,
    summary = summary,
    details = details,
    startAt = startAt
)