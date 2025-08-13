package com.mty.exptools.util

import android.icu.util.TimeUnit

data class Time(val value: Float, val unit: TimeUnit)

@JvmInline
value class MillisTime(val millis: Long) {

    companion object {
        val OneSecond = MillisTime(1000)
        val OneMinute = MillisTime(60 * OneSecond.millis)
        val OneHour = MillisTime(60 * OneMinute.millis)
        val OneDay = MillisTime(24 * OneHour.millis)
    }

    fun toSeconds(): Float = millis / OneSecond.millis.toFloat()
    fun toMinutes(): Float = millis / OneMinute.millis.toFloat()
    fun toHours(): Float = millis / OneHour.millis.toFloat()
    fun toDays(): Float = millis / OneDay.millis.toFloat()

    operator fun minus(other: MillisTime): MillisTime = MillisTime(this.millis - other.millis)

    fun toTime(): Time = when {
        millis < OneMinute.millis -> Time(toSeconds(), TimeUnit.SECOND)
        millis < OneHour.millis -> Time(toMinutes(), TimeUnit.MINUTE)
        millis < OneDay.millis -> Time(toHours(), TimeUnit.HOUR)
        else -> Time(toDays(), TimeUnit.DAY)
    }
}

fun TimeUnit.asString(): String = when(this) {
    TimeUnit.SECOND -> "秒"
    TimeUnit.MINUTE -> "分钟"
    TimeUnit.HOUR -> "小时"
    TimeUnit.DAY -> "天"
    else -> throw IllegalArgumentException("Unknown time unit: $this")
}

fun String.toTimeUnit(): TimeUnit = when(this) {
    "秒" -> TimeUnit.SECOND
    "分钟" -> TimeUnit.MINUTE
    "小时" -> TimeUnit.HOUR
    "天" -> TimeUnit.DAY
    else -> throw IllegalArgumentException("Unknown time unit: $this")
}