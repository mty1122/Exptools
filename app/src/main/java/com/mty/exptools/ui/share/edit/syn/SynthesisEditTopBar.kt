package com.mty.exptools.ui.share.edit.syn

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SynthesisEditTopBar(
    mode: SynthesisMode,
    running: Boolean,
    isFinished: Boolean,
    onBack: () -> Unit,
    onLoadOther: () -> Unit,
    onSave: () -> Unit,
    onEdit: () -> Unit,
    onSetAlarm: () -> Unit,
    onToggleRun: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = { Text(if (mode == SynthesisMode.EDIT) "合成步骤" else "合成步骤") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
        },
        actions = {
            if (mode == SynthesisMode.EDIT) {
                TextButton(onClick = onLoadOther) { Text("载入其他实验") }
                IconButton(onClick = onSave) { Icon(Icons.Default.Save, null) }
            } else {
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null) }
                if (!isFinished) {
                    IconButton(onClick = onSetAlarm) { Icon(Icons.Default.Alarm, null) }
                    IconButton(onClick = onToggleRun) {
                        Icon(if (running) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                    }
                }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null) }
            }
        }
    )
}
