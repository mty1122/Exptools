package com.mty.exptools.ui.share.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import java.time.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetMillisTimeDialog(
    visible: Boolean,
    initialCompletedAt: Long?,          // 传入当前“已完成时间”，可为 null
    onConfirm: (Long) -> Unit,          // 返回毫秒时间戳（必须 ≤ 当前时刻）
    onDismiss: () -> Unit
) {
    if (!visible) return

    // —— 时区：DatePicker 按 UTC 起始天，时间选择按本地时区 —— //
    val zoneLocal = remember { ZoneId.systemDefault() }

    // 初值：有传入就用传入；否则对齐到分钟的“现在”
    val initLocal = remember(initialCompletedAt) {
        val base = initialCompletedAt?.let { Instant.ofEpochMilli(it) } ?: Instant.now()
        base.atZone(zoneLocal).withSecond(0).withNano(0)
    }

    var selectedDate by remember(initialCompletedAt) { mutableStateOf(initLocal.toLocalDate()) }
    // 时间选择（24小时制）
    val tpState = rememberTimePickerState(
        initialHour = initLocal.hour,
        initialMinute = initLocal.minute,
        is24Hour = true
    )
    var showDatePicker by remember { mutableStateOf(false) }

    // 组合出候选毫秒
    fun candidateMillis(): Long {
        val zdt = ZonedDateTime.of(
            selectedDate.year, selectedDate.monthValue, selectedDate.dayOfMonth,
            tpState.hour, tpState.minute, 0, 0, zoneLocal
        )
        return zdt.toInstant().toEpochMilli()
    }

    val candidate = candidateMillis()

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = true),
        shape = RoundedCornerShape(12),
        title = { Text("设置时间") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 显示当前选中日期
                ListItem(
                    headlineContent = { Text("日期") },
                    supportingContent = {
                        Text(
                            "%04d-%02d-%02d".format(
                                selectedDate.year,
                                selectedDate.monthValue,
                                selectedDate.dayOfMonth
                            )
                        )
                    },
                    trailingContent = {
                        TextButton(onClick = { showDatePicker = true }) { Text("选择日期") }
                    }
                )
                Spacer(Modifier.height(16.dp))

                TimePicker(state = tpState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(candidate) }
            ) { Text("确定") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    val nowAligned = Instant.now().atZone(zoneLocal)
                        .withSecond(0).withNano(0)
                    selectedDate = nowAligned.toLocalDate()
                    tpState.hour = nowAligned.hour
                    tpState.minute = nowAligned.minute
                    tpState.selection = TimePickerSelectionMode.Hour
                }) { Text("现在") }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )

    // —— 日期选择弹窗 —— //
    if (showDatePicker) {
        // DatePicker 以 UTC 的 00:00 作为某一天的 millis
        val dpState = rememberDatePickerState(
            initialSelectedDateMillis = candidate // 初始高亮
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { millis ->
                        // 转回本地日期
                        selectedDate = Instant.ofEpochMilli(millis).atZone(zoneLocal).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = dpState)
        }
    }
}