package com.mty.exptools.domain

data class StepTimer(
    val requiredMillis: Long,           // 该步骤总时长
    val accumulatedMillis: Long = 0L,   // 已累计
    val startEpochMs: Long? = null      // 正在运行的开始时刻；暂停时 = null
) {
    fun neverStart(): Boolean = startEpochMs == null && accumulatedMillis == 0L
    fun isRunning(): Boolean = startEpochMs != null
    fun isFinished(): Boolean = remaining() <= 0L

    fun remaining(now: Long = System.currentTimeMillis()): Long {
        val live = startEpochMs?.let { now - it } ?: 0L
        return (requiredMillis - accumulatedMillis - live).coerceAtLeast(0L)
    }
    fun progress(): Float =
        ((requiredMillis - remaining()).toFloat() / requiredMillis).coerceIn(0f, 1f)

    fun start(now: Long = System.currentTimeMillis()) =
        if (startEpochMs == null) copy(startEpochMs = now) else this
    fun pause(now: Long = System.currentTimeMillis()) =
        startEpochMs?.let {
            copy(accumulatedMillis = accumulatedMillis + (now - it), startEpochMs = null)
        } ?: this
    fun complete(): StepTimer = copy(accumulatedMillis = requiredMillis, startEpochMs = null)
    fun reset(): StepTimer = copy(accumulatedMillis = 0L, startEpochMs = null)
}