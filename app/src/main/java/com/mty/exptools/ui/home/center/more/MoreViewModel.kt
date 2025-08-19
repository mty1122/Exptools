package com.mty.exptools.ui.home.center.more

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.exptools.ExptoolsApp
import com.mty.exptools.logic.export.ExportIo
import com.mty.exptools.logic.export.model.PhotoDraftExport
import com.mty.exptools.repository.MoreRepository
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
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

    private val _events = MutableSharedFlow<MoreUiEvent>()
    val events = _events.asSharedFlow()

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
    fun exportAll() = viewModelScope.launch {
        //val data = repo.getAllOnce()
        //val json = json.encodeToString(data)
        //saveOrAsk("Exptools_All_${stamp()}.json", json)
        _events.emit(MoreUiEvent.Toast("该功能正在开发中，敬请期待！"))
    }

    fun exportRange(start: Long, end: Long) = viewModelScope.launch {
        val data = repo.getPhotoDraftsByTimeRange(start, end)
        val listSerializer = ListSerializer(PhotoDraftExport.serializer())
        val jsonString = json.encodeToString(listSerializer, data)
        saveOrAsk("Exptools_PhotoRange_${stamp()}.json", jsonString)
    }

    /** 第一步：尽量写入“已选择的目录”，失败再让 UI 打开 SAF */
    private suspend fun saveOrAsk(fileName: String, json: String) {
        val ok = ExportIo.tryWriteToChosenDir(fileName, "application/json", json)
        if (ok) {
            _events.emit(MoreUiEvent.Toast("已导出到预设目录：$fileName"))
        } else {
            _events.emit(MoreUiEvent.RequestCreateDocument(fileName, "application/json", json))
        }
    }

    /** 第二步：UI 把 SAF 返回的 Uri 传入，真正写入 */
    fun writeToUri(uri: Uri, payload: String) = viewModelScope.launch {
        val ok = ExportIo.writeToUri(uri, payload)
        _events.emit(MoreUiEvent.Toast(if (ok) "导出完成" else "导出失败"))
    }

    private fun stamp(): String =
        SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())

    fun setExportDir(uri: Uri) = viewModelScope.launch {
        // 申请持久化权限
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        getApplication(ExptoolsApp.context).contentResolver.takePersistableUriPermission(uri, flags)

        repo.setExportDirUri(uri.toString())
        _uiState.update { it.copy(exportDirUri = uri.toString()) }
        _events.emit(MoreUiEvent.Toast("已设置导出目录"))
    }

}
