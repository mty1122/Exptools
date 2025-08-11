package com.mty.exptools.util

data class StepTimer(
    val requiredMillis: Long,           // 该步骤总时长
    val accumulatedMillis: Long = 0L,   // 已累计
    val startEpochMs: Long? = null      // 正在运行的开始时刻；暂停时 = null
) {
    fun isRunning(): Boolean = startEpochMs != null
    fun remaining(now: Long = System.currentTimeMillis()): Long {
        val live = startEpochMs?.let { now - it } ?: 0L
        return (requiredMillis - accumulatedMillis - live).coerceAtLeast(0L)
    }
    fun isFinished(now: Long = System.currentTimeMillis()): Boolean = remaining(now) <= 0L
    fun start(now: Long = System.currentTimeMillis()) =
        if (startEpochMs == null) copy(startEpochMs = now) else this
    fun pause(now: Long = System.currentTimeMillis()) =
        startEpochMs?.let { copy(accumulatedMillis = accumulatedMillis + (now - it), startEpochMs = null) } ?: this
    fun complete(): StepTimer = copy(accumulatedMillis = requiredMillis, startEpochMs = null)
}

