package com.mty.exptools.ui.home.center.more

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (startMillis: Long, endMillis: Long) -> Unit
) {
    val state = rememberDateRangePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true),
        confirmButton = {
            TextButton(
                onClick = {
                    val start = state.selectedStartDateMillis
                    val end = state.selectedEndDateMillis
                    if (start != null && end != null) {
                        onConfirm(
                            alignToLocal00(start),
                            alignToLocal23(end)
                        )
                    } else onDismiss()
                }
            ) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    ) {
        DateRangePicker(
            title = {},
            headline = { CompactRangeHeadline(state) },
            state = state
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactRangeHeadline(state: DateRangePickerState) {
    val zone = remember { ZoneId.systemDefault() }
    fun Long.toText(): String {
        val d = Instant.ofEpochMilli(this).atZone(zone)
        return d.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    }
    val text = when {
        state.selectedStartDateMillis == null && state.selectedEndDateMillis == null -> "选择日期"
        state.selectedStartDateMillis != null && state.selectedEndDateMillis == null ->
            state.selectedStartDateMillis!!.toText() + " - 结束日期"
        state.selectedStartDateMillis == null && state.selectedEndDateMillis != null ->
            "开始日期 - " + state.selectedEndDateMillis!!.toText()
        else -> {
            val s = state.selectedStartDateMillis!!
            val e = state.selectedEndDateMillis!!
            val sd = Instant.ofEpochMilli(s).atZone(zone)
            val ed = Instant.ofEpochMilli(e).atZone(zone)
            val left  = sd.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
            val right = ed.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
            "$left - $right"
        }
    }
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        maxLines = 1
    )
}

/** 起始对齐到本地 00:00; 结束对齐到本地 23:59:59.999 */
private fun alignToLocal00(ms: Long): Long {
    val z = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault())
    return z.withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant().toEpochMilli()
}
private fun alignToLocal23(ms: Long): Long {
    val z = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault())
    return z.withHour(23).withMinute(59).withSecond(59).withNano(999_000_000).toInstant().toEpochMilli()
}
