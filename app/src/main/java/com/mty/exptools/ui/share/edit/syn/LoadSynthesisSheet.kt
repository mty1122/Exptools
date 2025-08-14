package com.mty.exptools.ui.share.edit.syn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mty.exptools.domain.syn.SynthesisDraft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadSynthesisSheet(
    visible: Boolean,
    drafts: List<SynthesisDraft>,        // 来自 VM 的列表（Flow 收集后传进来）
    onDismiss: () -> Unit,
    onPick: (SynthesisDraft) -> Unit     // 选定后回调
) {
    if (!visible) return

    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<String?>(null) }

    val filtered = remember(drafts, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) drafts
        else drafts.filter { d ->
            d.materialName.lowercase().contains(q) ||
                    d.rawMaterials.lowercase().contains(q) ||
                    d.conditionSummary.lowercase().contains(q)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("导入已有材料", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                label = { Text("搜索名称/原料/摘要") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()

            if (filtered.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) { Text("没有匹配的材料") }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filtered, key = { it.materialName }) { d ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selected = d.materialName
                                }
                                .padding(horizontal = 4.dp, vertical = 10.dp)
                        ) {
                            RadioButton(
                                selected = selected == d.materialName,
                                onClick = { selected = d.materialName }
                            )
                            Text(
                                d.materialName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selected == d.materialName) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                            Spacer(Modifier.weight(1f))
                            Text("步骤 ${d.steps.size}", style = MaterialTheme.typography.labelMedium)
                        }

                        val sub = d.conditionSummary.takeIf { it.isNotBlank() } ?: d.rawMaterials
                        if (sub.isNotBlank()) {
                            Text(
                                text = sub.take(60),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 44.dp, end = 8.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) { Text("取消") }

                val picked = filtered.firstOrNull { it.materialName == selected }
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