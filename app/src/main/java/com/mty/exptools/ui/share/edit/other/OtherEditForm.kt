package com.mty.exptools.ui.share.edit.other

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
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
fun OtherEditForm(
    modifier: Modifier = Modifier,
    ui: OtherEditUiState,
    onAction: (OtherEditAction) -> Unit
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
                    text = "任务名称",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (mode == OtherMode.EDIT) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = draft.taskName,
                            onValueChange = { onAction(OtherEditAction.UpdateTaskName(it)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    val text = draft.taskName
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            text = text.ifBlank { "—" },
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        item {
            FieldBlock(
                title = "任务内容摘要",
                value = draft.summary,
                editable = mode == OtherMode.EDIT,
                onValueChange = { onAction(OtherEditAction.UpdateSummary(it)) },
                multiLine = false
            )
        }

        item {
            FieldBlock(
                title = "任务细节",
                value = draft.details,
                editable = mode == OtherMode.EDIT,
                onValueChange = { onAction(OtherEditAction.UpdateDetails(it)) },
                multiLine = true
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("结束时间", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ReadonlyBox(
                        text = draft.completedAt.toMillisTime().toDateTime(),
                    )
                    if (mode == OtherMode.EDIT) {
                        IconButton(onClick = { onAction(OtherEditAction.SetCompletedAt) }) {
                            Icon(Icons.Default.Schedule, contentDescription = "设置结束时间")
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
