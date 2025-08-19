package com.mty.exptools.logic.export

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.mty.exptools.ExptoolsApp.Companion.context
import com.mty.exptools.logic.dao.PreferenceDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

object ExportIo {
    /** 先尝试写入已选择的目录（若有） */
    suspend fun tryWriteToChosenDir(
        fileName: String,
        mime: String,
        payload: String
    ): Boolean = withContext(Dispatchers.IO) {
        val dirStr = PreferenceDao.getExportDirUri() ?: return@withContext false
        val dirUri = dirStr.toUri()
        val tree = DocumentFile.fromTreeUri(context, dirUri) ?: return@withContext false
        val target = tree.findFile(fileName)
            ?: tree.createFile(mime, fileName)
            ?: return@withContext false
        context.contentResolver.openOutputStream(target.uri)?.use {
            it.write(payload.toByteArray())
            true
        } == true
    }

    /** 真实往 SAF Uri 写（UI 通过 CreateDocument 给到的 Uri） */
    suspend fun writeToUri(uri: Uri, payload: String): Boolean =
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(payload.toByteArray())
                true
            } == true
        }
}