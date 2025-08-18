package com.mty.exptools.ui.share.edit.other

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mty.exptools.domain.other.OtherDraft
import com.mty.exptools.util.toMillisTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadOtherSheet(
    visible: Boolean,
    drafts: List<OtherDraft>,
    onDismiss: () -> Unit,
    setBackgroundBlur: (Boolean) -> Unit = {},
    onPick: (OtherDraft) -> Unit
) {
    if (!visible) return

    var query by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf<Long?>(null) }

    val filtered = remember(drafts, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) drafts
        else drafts.filter { d ->
            d.taskName.lowercase().contains(q) ||
                    d.summary.lowercase().contains(q) ||
                    (d.completedAt.toMillisTime().toDateTime()).lowercase().contains(q)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 根据目标状态切换背景模糊：目标 Hidden → 关闭模糊；其余 → 打开
    LaunchedEffect(sheetState.targetValue) {
        setBackgroundBlur(sheetState.targetValue != SheetValue.Hidden)
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("导入已有任务", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                label = { Text("搜索名称/摘要/结束时间") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()

            if (filtered.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) { Text("没有匹配的记录") }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filtered, key = { it.dbId }) { d ->
                        val isSelected = selectedId == d.dbId
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedId = d.dbId }
                                .padding(horizontal = 4.dp, vertical = 10.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedId = d.dbId }
                            )
                            Text(
                                text = d.taskName.ifBlank { "（未命名任务）" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = d.completedAt.toMillisTime().toDateTime(),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        val subLine = d.summary.takeIf { it.isNotBlank() } ?: "摘要（未填）"

                        Text(
                            text = subLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 44.dp, end = 8.dp, bottom = 8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) { Text("取消") }

                val picked = filtered.firstOrNull { it.dbId == selectedId }
                Button(
                    onClick = { picked?.let(onPick) },
                    enabled = picked != null,
                    modifier = Modifier.weight(1f)
                ) { Text("导入") }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}