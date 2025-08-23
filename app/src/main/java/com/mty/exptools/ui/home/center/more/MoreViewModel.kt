package com.mty.exptools.ui.home.center.more

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.exptools.ExptoolsApp
import com.mty.exptools.logic.export.ExportIo
import com.mty.exptools.repository.MoreRepository
import com.mty.exptools.util.toast
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val repo: MoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MoreUiState>(MoreUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    autoRefreshSeconds = repo.getAutoRefreshSeconds(),
                    exportDirUri = repo.getExportDirUri()
                )
            }
        }
    }

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    // ------- 偏好设置 -------
    fun setAutoRefreshSeconds(seconds: Int) = viewModelScope.launch {
        repo.setAutoRefreshSeconds(seconds)
        _uiState.update { it.copy(autoRefreshSeconds = seconds) }
    }

    // ------- 导出入口 -------
    @OptIn(ExperimentalSerializationApi::class)
    fun exportAll() = viewModelScope.launch {
        _uiState.update { it.copy(exportingAll = true) }
        val data = repo.getAllOnce()
        saveToFile("Exptools_All_${stamp()}.json") { os ->
            os.buffered().use {
                json.encodeToStream(data, it)
            }
        }
        _uiState.update { it.copy(exportingAll = false) }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun exportRange(start: Long, end: Long) = viewModelScope.launch {
        _uiState.update { it.copy(exportingRange = true) }
        val data = repo.getPhotoDraftsWithSynByTimeRange(start, end)
        saveToFile("Exptools_PhotoRange_${stamp()}.json") { os ->
            os.buffered().use {
                json.encodeToStream(data, it)
            }
        }
        _uiState.update { it.copy(exportingRange = false) }
    }

    private suspend fun saveToFile(fileName: String, writer: (OutputStream) -> Unit) {
        val ok = ExportIo.tryWriteToChosenDir(fileName, "application/json", writer)
        if (ok) {
            toast("已导出到预设目录：$fileName")
        } else {
            toast("请先设置导出目录")
        }
    }

    private fun stamp(): String =
        SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())

    fun setExportDir(uri: Uri) = viewModelScope.launch {
        // 申请持久化权限
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        getApplication(ExptoolsApp.context).contentResolver.takePersistableUriPermission(uri, flags)

        repo.setExportDirUri(uri.toString())
        _uiState.update { it.copy(exportDirUri = uri.toString()) }
        toast("已设置导出目录")
    }

}
