package com.mty.exptools.ui.home.center.more

import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mty.exptools.ExptoolsApp
import com.mty.exptools.util.toast
import java.time.Instant
import java.time.ZoneId
import androidx.core.net.toUri

@Composable
fun MoreScreen(
    setBackgroundBlur: (Boolean) -> Unit,
    viewModel: MoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // SAF：创建文档
    var pending by remember { mutableStateOf<Pair<String, String>?>(null) } // mime to payload
    val createDoc = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val (_, payload) = pending ?: return@rememberLauncherForActivityResult
        if (uri != null) viewModel.writeToUri(uri, payload)
        pending = null
    }

    // 订阅事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { e ->
            when (e) {
                is MoreUiEvent.RequestCreateDocument -> {
                    pending = e.mime to e.payload
                    createDoc.launch(e.fileName)
                }
                is MoreUiEvent.Toast -> {
                    toast(e.msg)
                }
            }
        }
    }

    // —— UI —— //
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1) 自动刷新间隔
        AutoRefreshCard(
            current = uiState.autoRefreshSeconds,
            onPick = { viewModel.setAutoRefreshSeconds(it) }
        )

        // 2) 导出指定时间范围
        ExportRangeCard(
            setBackgroundBlur = setBackgroundBlur,
            onExport = { start, end ->
                viewModel.exportRange(start, end)
            }
        )

        // 3) 导出所有数据库
        ExportAllCard(onExportAll = { viewModel.exportAll() })

        ExportDirCard(
            folderUri = uiState.exportDirUri,
            onPickDir = { viewModel.setExportDir(it) }
        )

        // 4) 关于
        AboutCard(
            versionName = runCatching {
                val context = LocalContext.current
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
            }.getOrDefault("1.0")
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoRefreshCard(
    current: Int,
    onPick: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(5, 10, 15, 30, 60)

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("自动刷新间隔", style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = "$current 秒",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("选择间隔") },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { s ->
                        DropdownMenuItem(
                            text = { Text("$s 秒") },
                            onClick = {
                                onPick(s)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportRangeCard(
    onExport: (startMillis: Long, endMillis: Long) -> Unit,
    setBackgroundBlur: (Boolean) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    var startMillis by remember { mutableStateOf<Long?>(null) }
    var endMillis by remember { mutableStateOf<Long?>(null) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("导出光催化（时间范围）", style = MaterialTheme.typography.titleMedium)
            Text(
                text = buildString {
                    append("已选择：")
                    append(startMillis?.let { millisToDate(it) } ?: "—")
                    append(" 至 ")
                    append(endMillis?.let { millisToDate(it) } ?: "—")
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { open = true }) { Text("选择范围") }
                Button(
                    onClick = {
                        val s = startMillis
                        val e = endMillis
                        if (s != null && e != null && s <= e) onExport(s, e)
                    },
                    enabled = startMillis != null && endMillis != null && startMillis!! <= endMillis!!
                ) { Text("导出") }
            }
        }
    }

    if (open) {
        setBackgroundBlur(true)
        DateRangePickerDialog(
            onDismiss = { open = false },
            onConfirm = { s, e ->
                startMillis = s
                endMillis = e
                open = false
            }
        )
    } else {
        setBackgroundBlur(false)
    }
}

@Composable
private fun ExportAllCard(onExportAll: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("导出数据库（全部）", style = MaterialTheme.typography.titleMedium)
            Text("导出全部数据为多个 JSON 文件。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onExportAll) { Text("导出全部") }
        }
    }
}

@Composable
private fun ExportDirCard(
    folderUri: String?,
    onPickDir: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) onPickDir(uri)
    }

    val label = remember(folderUri) {
        folderUri?.let { getFolderLabel(it.toUri()) } ?: ""
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("导出目录", style = MaterialTheme.typography.titleMedium)
            if (label.isBlank())
                Text("设置默认导出目录，之后导出会自动保存到该目录。")
            else
                Text("当前设置：$label")
            Button(onClick = { launcher.launch(null) }) { Text("选择目录") }
        }
    }
}

@Composable
private fun AboutCard(versionName: String, email: String = "admin@mxmnb.cn") {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("关于", style = MaterialTheme.typography.titleMedium)
            Text("版本：$versionName", fontWeight = FontWeight.Medium)
            Text("联系作者：$email")
        }
    }
}

private fun millisToDate(ms: Long): String {
    val ld = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
    return "%04d-%02d-%02d".format(ld.year, ld.monthValue, ld.dayOfMonth)
}

private fun getFolderLabel(treeUri: Uri): String {
    // 优先用 SAF 的 documentId（多数存储卷会是 "<volume>:<path>" 结构）
    val docId = try { DocumentsContract.getTreeDocumentId(treeUri) } catch (_: Exception) { null }

    if (docId != null && ':' in docId) {
        val (vol, path) = docId.split(':', limit = 2).let { it[0] to it.getOrNull(1).orEmpty() }
        val volName = when {
            vol == "primary" -> "内部存储"
            vol.matches(Regex("(?i)[0-9a-f]{4}-[0-9a-f]{4}")) -> "SD卡($vol)"
            else -> vol // 其他卷标，直接显示原样
        }
        return if (path.isNotEmpty()) "$volName/$path" else volName
    }

    // 兜底：取目录名 + provider authority
    val name = DocumentFile.fromTreeUri(ExptoolsApp.context, treeUri)?.name
    val provider = treeUri.authority?.substringBefore('.') ?: "Provider"
    return when {
        !name.isNullOrBlank() -> "$provider/$name"
        else -> treeUri.toString() // 最终兜底
    }
}