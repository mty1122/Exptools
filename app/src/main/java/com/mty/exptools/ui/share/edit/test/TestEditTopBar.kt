package com.mty.exptools.ui.share.edit.test

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
fun TestEditTopBar(
    mode: TestMode,
    isNew: Boolean,
    onBack: () -> Unit,
    onLoadOther: () -> Unit,
    onSave: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                when (mode) {
                    TestMode.EDIT -> {
                        when {
                            isNew -> "测试（新增）"
                            else -> "测试（修改）"
                        }
                    }
                    TestMode.VIEW -> "测试"
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
        },
        actions = {
            if (mode == TestMode.EDIT) {
                TextButton(onClick = onLoadOther) { Text("导入已有实验") }
                IconButton(onClick = onSave) { Icon(Icons.Default.Save, null) }
            } else {
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null) }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null) }
            }
        }
    )
}