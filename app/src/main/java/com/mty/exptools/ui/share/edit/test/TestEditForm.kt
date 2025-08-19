package com.mty.exptools.ui.share.edit.test

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mty.exptools.util.toMillisTime

@Composable
fun TestEditForm(
    modifier: Modifier = Modifier,
    ui: TestEditUiState,
    onAction: (TestAction) -> Unit,
    onClickMaterialName: (String) -> Unit
) {
    val draft = ui.draft
    val mode = ui.mode

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 材料 + 选择已有材料（按钮与文本框同一行右侧）
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "材料名称",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (mode == TestMode.EDIT) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = draft.materialName,
                            onValueChange = { onAction(TestAction.UpdateMaterial(it)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            modifier = Modifier.height(56.dp),
                            onClick = { onAction(TestAction.PickExistingMaterial) },
                            label = { Text("选择已有材料", color = MaterialTheme.colorScheme.primary) }
                        )
                    }
                } else {
                    val text = draft.materialName
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable(
                            enabled = text.isNotBlank(),
                            onClick = { onClickMaterialName(text) }
                        )
                    ) {
                        Text(
                            text = text.ifBlank { "—" },
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // 测试内容摘要
        item {
            FieldBlock(
                title = "测试内容摘要",
                value = draft.summary,
                editable = mode == TestMode.EDIT,
                onValueChange = { onAction(TestAction.UpdateSummary(it)) },
                multiLine = false
            )
        }

        // 测试细节
        item {
            FieldBlock(
                title = "测试细节",
                value = draft.details,
                editable = mode == TestMode.EDIT,
                onValueChange = { onAction(TestAction.UpdateDetails(it)) },
                multiLine = true
            )
        }

        // 开始时间（只读文本框 + 编辑模式显示时钟按钮）
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("开始时间", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ReadonlyBox(
                        text = draft.startAt.toMillisTime().toDateTime(),
                    )
                    if (mode == TestMode.EDIT) {
                        IconButton(onClick = { onAction(TestAction.SetStartAt) }) {
                            Icon(Icons.Default.Schedule, contentDescription = "设置开始时间")
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(10.dp)) // 最后一项抬高
        }
    }
}

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
private fun ReadonlyBox(text: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(text, modifier = Modifier.padding(12.dp))
    }
}
