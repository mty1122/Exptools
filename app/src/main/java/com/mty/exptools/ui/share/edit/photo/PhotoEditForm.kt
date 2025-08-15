package com.mty.exptools.ui.share.edit.photo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mty.exptools.domain.photo.ConcUnit
import com.mty.exptools.domain.photo.LightSource
import com.mty.exptools.domain.photo.PhotoTargetMaterial
import com.mty.exptools.domain.photo.PhotocatalysisStep
import com.mty.exptools.domain.photo.PhotocatalysisTarget
import com.mty.exptools.domain.photo.calcPerformance
import com.mty.exptools.domain.photo.toMgL

@Composable
fun PhotoEditForm(
    modifier: Modifier,
    ui: PhotoEditUiState,
    onAction: (PhotoEditAction) -> Unit,
    onPickExistingCatalyst: () -> Unit, // 点击“选择已有材料”的入口（弹出你现有的选择器）
    existingTargets: List<PhotoTargetMaterial> = emptyList(),  // 新增：已有反应物/产物名称
    existingLights: List<String> = emptyList()    // 新增：已有光源文案（如“氙灯 420nm”）
) {
    val draft = ui.draft
    val mode = ui.mode

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 催化剂名称 + 选择已有材料
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "催化剂名称",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (mode == PhotocatalysisMode.EDIT) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = draft.catalystName,
                            onValueChange = { onAction(PhotoEditAction.UpdateCatalystName(it)) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            modifier = Modifier.height(56.dp),
                            onClick = {
                                onAction(PhotoEditAction.PickExistingCatalyst)
                                onPickExistingCatalyst()
                            },
                            label = { Text("选择已有材料", color = MaterialTheme.colorScheme.primary) }
                        )
                    }
                } else {
                    ReadonlyBox(draft.catalystName.ifBlank { "—" })
                }
            }
        }

        // 反应物/产物信息卡片
        item {
            TargetCard(
                mode = mode,
                target = draft.target,
                onAction = onAction,
                suggestions = existingTargets
            )
        }

        // 光源
        item {
            FieldBlockWithSuggestions(
                title = "光源",
                text = draft.light.value,
                editable = mode == PhotocatalysisMode.EDIT,
                suggestions = existingLights,
                onTextChange = { onAction(PhotoEditAction.UpdateLightSource(LightSource(it))) }
            )
        }

        // 实验细节
        item {
            FieldBlock(
                title = "实验细节",
                value = draft.details,
                editable = mode == PhotocatalysisMode.EDIT,
                onValueChange = { onAction(PhotoEditAction.UpdateDetails(it)) },
                multiLine = true
            )
        }

        // 步骤列表
        itemsIndexed(draft.steps, key = { _, s -> s.orderIndex }) { idx, step ->
            StepCardPhoto(
                index = idx,
                mode = mode,
                step = step,
                target = draft.target,
                highlight = mode == PhotocatalysisMode.VIEW && idx == ui.currentStepIndex,
                onAction = onAction,
                onJump = { onAction(PhotoEditAction.JumpStep(idx)) }
            )
        }

        if (mode == PhotocatalysisMode.EDIT) {
            item {
                OutlinedButton(onClick = { onAction(PhotoEditAction.AddStep) }) {
                    Text("＋ 添加步骤")
                }
            }
        }
    }
}

/* -------------------------- 组件细节 -------------------------- */

@Composable
private fun FieldBlock(
    title: String,
    value: String,
    editable: Boolean,
    onValueChange: (String) -> Unit,
    multiLine: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        if (editable) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = !multiLine,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            ReadonlyBox(if (value.isBlank()) "—" else value)
        }
    }
}

@Composable
private fun FieldBlockWithSuggestions(
    title: String,
    text: String,
    editable: Boolean,
    suggestions: List<String>,
    onTextChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        if (editable) {
            SuggestionTextField(
                value = text,
                onValueChange = onTextChange,
                suggestions = suggestions
            )
        } else {
            ReadonlyBox(text.ifBlank { "—" })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuggestionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }
    var tfv by remember { mutableStateOf(TextFieldValue(value)) }

    // 外部 value 变化时，同步内部并把光标放到末尾
    LaunchedEffect(value) {
        if (value != tfv.text) {
            tfv = TextFieldValue(text = value, selection = TextRange(value.length))
        }
    }

    // 过滤
    val filtered = remember(tfv.text, suggestions) {
        val v = tfv.text.trim()
        if (v.isEmpty()) suggestions else suggestions.filter { it.contains(v, ignoreCase = true) }
    }
    val actuallyExpanded = expanded && hasFocus && filtered.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = actuallyExpanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = tfv,
            onValueChange = {
                tfv = it
                onValueChange(it.text)
                expanded = true
            },
            singleLine = true,
            label = label,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable) // 或者直接 .menuAnchor()
                .fillMaxWidth()
                .onFocusChanged { f ->
                    hasFocus = f.isFocused
                    if (!f.isFocused) expanded = false
                }
        )

        // 下拉提示列表
        ExposedDropdownMenu(
            expanded = actuallyExpanded,
            onDismissRequest = { expanded = false }
        ) {
            filtered.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s) },
                    onClick = {
                        // 选中建议后把光标放到末尾
                        tfv = TextFieldValue(s, selection = TextRange(s.length))
                        onValueChange(s)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ReadonlyBox(text: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(text, modifier = Modifier.padding(12.dp))
    }
}

/** 反应物/产物信息卡片 */
@Composable
private fun TargetCard(
    mode: PhotocatalysisMode,
    target: PhotocatalysisTarget,
    onAction: (PhotoEditAction) -> Unit,
    suggestions: List<PhotoTargetMaterial>
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("反应物 / 产物", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

            // 第一行：名称 + 吸收波长
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f)) {
                    FieldBlockWithSuggestions(
                        title = "名称",
                        text = target.name,
                        editable = mode == PhotocatalysisMode.EDIT,
                        suggestions = suggestions.map { it.name },
                        onTextChange = { newName ->
                            onAction(PhotoEditAction.UpdateTargetName(newName))
                            val new = suggestions.firstOrNull { it.name == newName }
                            if (new != null) {
                                onAction(PhotoEditAction.UpdateTargetWavelength(new.waveLength))
                            }
                        }
                    )
                }
                Column(Modifier.weight(1f)) {
                    FieldBlock(
                        title = "吸收波长（nm，可空）",
                        value = target.wavelengthNm,
                        editable = mode == PhotocatalysisMode.EDIT,
                        onValueChange = { onAction(PhotoEditAction.UpdateTargetWavelength(it)) }
                    )
                }
            }

            // 第二行：初始/期望浓度（A / mg/L）
            Column {
                Text("初始 / 期望浓度", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                if (mode == PhotocatalysisMode.EDIT) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = target.initialConcValue,
                            onValueChange = { onAction(PhotoEditAction.UpdateInitialConcValue(it)) },
                            label = { Text(if (target.initialConcUnit == ConcUnit.ABSORBANCE_A) "A" else "mg/L") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        ConcUnitSegment(
                            selected = target.initialConcUnit,
                            onSelected = { onAction(PhotoEditAction.UpdateInitialConcUnit(it)) }
                        )
                    }
                } else {
                    val unit = if (target.initialConcUnit == ConcUnit.ABSORBANCE_A) "A" else "mg/L"
                    ReadonlyBox((target.initialConcValue.ifBlank { "—" }) + " $unit")
                }
            }

            // 第三行：标准曲线 y = kx（k 可空）
            FieldBlock(
                title = "标准曲线（y = kx，填写 k；可空）",
                value = target.stdCurveK,
                editable = mode == PhotocatalysisMode.EDIT,
                onValueChange = { onAction(PhotoEditAction.UpdateStdCurveK(it)) }
            )
        }
    }
}

/** 步骤卡片（光催化） */
@Composable
private fun StepCardPhoto(
    index: Int,
    mode: PhotocatalysisMode,
    step: PhotocatalysisStep,
    target: PhotocatalysisTarget,
    highlight: Boolean,
    onAction: (PhotoEditAction) -> Unit,
    onJump: () -> Unit,
) {
    val borderColor = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = mode == PhotocatalysisMode.VIEW) { onJump() }
    ) {
        val borderColor = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = mode == PhotocatalysisMode.VIEW) { onJump() }
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("步骤 ${index + 1}", style = MaterialTheme.typography.titleSmall)

                // 行1：步骤名称 + 测试间隔（分钟）
                if (mode == PhotocatalysisMode.EDIT) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = step.name,
                            onValueChange = {
                                onAction(
                                    PhotoEditAction.UpdateStepName(
                                        step.orderIndex,
                                        it
                                    )
                                )
                            },
                            label = { Text("步骤名称") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = step.intervalMinuteText,
                            onValueChange = {
                                onAction(
                                    PhotoEditAction.UpdateStepIntervalMinute(
                                        step.orderIndex,
                                        it
                                    )
                                )
                            },
                            label = { Text("间隔（分钟）") },
                            singleLine = true,
                            modifier = Modifier.widthIn(min = 120.dp).weight(1f)
                        )
                    }

                    // 行2：浓度值 + 单位切换（A / mg·L⁻¹）
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = step.concValueText,
                            onValueChange = {
                                onAction(
                                    PhotoEditAction.UpdateStepConcValue(
                                        step.orderIndex,
                                        it
                                    )
                                )
                            },
                            label = { Text(if (step.concUnit == ConcUnit.ABSORBANCE_A) "浓度（A）" else "浓度（mg/L）") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        ConcUnitSegment(
                            selected = step.concUnit,
                            onSelected = {
                                onAction(
                                    PhotoEditAction.UpdateStepConcUnit(
                                        step.orderIndex,
                                        it
                                    )
                                )
                            }
                        )
                    }

                    if (index > 0) {
                        TextButton(
                            onClick = { onAction(PhotoEditAction.RemoveStep(step.orderIndex)) },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("删除该步骤") }
                    }
                } else {
                    // 浏览态：优先显示“分解率/产率”，失败回退“浓度”
                    val c0 = toMgL(
                        valueText = target.initialConcValue,
                        unit = target.initialConcUnit,
                        kText = target.stdCurveK
                    )
                    val ci = toMgL(
                        valueText = step.concValueText,
                        unit = step.concUnit,
                        kText = target.stdCurveK
                    )
                    val perf = calcPerformance(c0, ci)
                    val line1 = "间隔：${step.intervalMinuteText.ifBlank { "—" }} 分钟"
                    val line2 = when {
                        perf != null -> "性能：${"%.1f".format((perf * 100).coerceIn(0.0, 100.0))} %"
                        else -> {
                            val unit = if (step.concUnit == ConcUnit.ABSORBANCE_A) "A" else "mg/L"
                            "浓度：${step.concValueText.ifBlank { "—" }} $unit"
                        }
                    }
                    Text(line1)
                    Text(line2)
                }
            }
        }
    }
}

/** 浓度单位切换（A / mg/L） */
@Composable
private fun ConcUnitSegment(
    selected: ConcUnit,
    onSelected: (ConcUnit) -> Unit
) {
    Row(
        modifier = Modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(2.dp)
    ) {
        SegBtn("   A   ", selected == ConcUnit.ABSORBANCE_A) { onSelected(ConcUnit.ABSORBANCE_A) }
        SegBtn("   mg/L   ", selected == ConcUnit.MG_L) { onSelected(ConcUnit.MG_L) }
    }
}

@Composable
private fun SegBtn(text: String, selected: Boolean, onClick: () -> Unit) {
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