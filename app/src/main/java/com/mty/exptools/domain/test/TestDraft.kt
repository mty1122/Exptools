package com.mty.exptools.domain.test

data class TestDraft(
    val dbId: Long = 0L,
    val materialName: String = "",
    val summary: String = "",
    val details: String = "",
    val startAt: Long = System.currentTimeMillis()   // 开始时间（毫秒）
)
