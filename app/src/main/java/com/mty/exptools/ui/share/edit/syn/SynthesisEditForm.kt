package com.mty.exptools.ui.share.edit.syn

import android.icu.util.TimeUnit
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mty.exptools.domain.syn.SynthesisDraft
import com.mty.exptools.domain.syn.SynthesisStep
import com.mty.exptools.util.MillisTime
import com.mty.exptools.util.asString
import java.util.Locale

@Composable
fun SynthesisEditForm(
    modifier: Modifier,
    mode: SynthesisMode,
    draft: SynthesisDraft,
    currentStepIndex: Int,
    completedAt: Long?,
    onAction: (SynthesisAction) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FieldBlock(
                title = "材料名称",
                value = draft.materialName,
                editable = mode == SynthesisMode.EDIT,
                onValueChange = { onAction(SynthesisAction.UpdateMaterial(it)) }
            )
        }
        item {
            FieldBlock(
                title = "原材料",
                value = draft.rawMaterials,
                editable = mode == SynthesisMode.EDIT,
                onValueChange = { onAction(SynthesisAction.UpdateRaw(it)) }
            )
        }
        item {
            FieldBlock(
                title = "反应条件摘要",
                value = draft.conditionSummary,
                editable = mode == SynthesisMode.EDIT,
                onValueChange = { onAction(SynthesisAction.UpdateSummary(it)) },
                singleLine = false
            )
        }
        item {
            FieldBlock(
                title = "实验细节",
                value = draft.expDetails,
                editable = mode == SynthesisMode.EDIT,
                onValueChange = { onAction(SynthesisAction.UpdateDetail(it)) },
                singleLine = false
            )
        }

        itemsIndexed(draft.steps, key = { _, s -> s.orderIndex }) { idx, step ->
            StepCard(
                index = idx,
                step = step,
                editable = mode == SynthesisMode.EDIT,
                highlight = mode == SynthesisMode.VIEW && idx == currentStepIndex,
                onContentChange = { onAction(SynthesisAction.UpdateStepContent(step.orderIndex, it)) },
                onDurationChange = { onAction(SynthesisAction.UpdateStepDuration(step.orderIndex, it, step.unit)) },
                onUnitChange = { onAction(SynthesisAction.UpdateStepUnit(step.orderIndex, it)) },
                onRemove = { onAction(SynthesisAction.RemoveStep(step.orderIndex)) },
                clickable = mode == SynthesisMode.VIEW,
                onClick = { onAction(SynthesisAction.JumpStep(idx)) }
            )
        }

        if (mode == SynthesisMode.EDIT) {
            item {
                OutlinedButton(
                    onClick = { onAction(SynthesisAction.AddStep) },
                ) { Text(
                    text = "＋ 添加步骤",
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) }
            }
        }

        if (completedAt != null) {
            item {
                FieldBlock(
                    title = if (completedAt > System.currentTimeMillis()) "预计完成时间：" else "完成于：",
                    value = MillisTime(completedAt).toDateTime(),
                    editable = false,
                    onValueChange = {}
                )
            }
        }
    }
}

@Composable
private fun FieldBlock(
    title: String,
    value: String,
    editable: Boolean,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        if (editable) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // 不可编辑且不可选中 → 直接 Text
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = if (value.isBlank()) "—" else value,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun StepCard(
    index: Int,
    step: SynthesisStep,
    editable: Boolean,
    highlight: Boolean,
    clickable: Boolean,
    onClick: () -> Unit,
    onContentChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onUnitChange: (TimeUnit) -> Unit,
    onRemove: () -> Unit
) {
    val borderColor = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = clickable, onClick = { onClick() })
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("步骤 ${index + 1}", style = MaterialTheme.typography.titleSmall)

            FieldBlock(
                title = "内容",
                value = step.content,
                editable = editable,
                onValueChange = onContentChange,
                singleLine = false
            )

            if (editable) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                    OutlinedTextField(
                        value = step.duration().takeIf { it > 0 }?.toString() ?: "",
                        onValueChange = onDurationChange,
                        label = { Text("时长") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    SegmentedButtons(
                        selected = step.unit,
                        onSelected = onUnitChange
                    )
                }
                if (index > 0) {
                    TextButton(onClick = onRemove, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("删除该步骤")
                    }
                }
            } else {
                // 浏览模式：只读展示
                val text = buildString {
                    append("时长：")
                    append(step.duration())
                    append(" ")
                    append(step.unit.asString())
                    when {
                        step.timer.isFinished() -> append(" | 已完成")
                        step.timer.neverStart() -> append(" | 未开始")
                        else -> {
                            append(" | 剩余：")
                            val time = MillisTime(step.timer.remaining()).toTime()
                            append(String.format(Locale.PRC, "%.1f", time.value))
                            append(" ")
                            append(time.unit.asString())
                        }
                    }
                }
                Text(text)
            }
        }
    }
}

@Composable
private fun SegmentedButtons(
    selected: TimeUnit,
    onSelected: (TimeUnit) -> Unit
) {
    Row(
        modifier = Modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
    ) {
        val sel = selected
        SegItem(" 分钟 ", sel == TimeUnit.MINUTE) { onSelected(TimeUnit.MINUTE) }
        SegItem(" 小时 ", sel == TimeUnit.HOUR) { onSelected(TimeUnit.HOUR) }
    }
}

@Composable
private fun SegItem(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(text, color = color) }
}

