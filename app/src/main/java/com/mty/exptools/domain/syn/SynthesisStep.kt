package com.mty.exptools.domain.syn

import android.icu.util.TimeUnit
import com.mty.exptools.domain.StepTimer

data class SynthesisStep(
    val orderIndex: Int = 0,
    val content: String = "",
    val unit: TimeUnit = TimeUnit.MINUTE,                 // 仅用于 UI 录入
    val timer: StepTimer = StepTimer(requiredMillis = 30 * 60 * 1000L) // 默认30分钟
) {
    fun duration() = if (unit == TimeUnit.MINUTE) {
        timer.requiredMillis / 1000 / 60
    } else {
        timer.requiredMillis / 1000 / 60 / 60
    }
}
