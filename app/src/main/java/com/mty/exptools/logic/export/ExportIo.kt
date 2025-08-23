package com.mty.exptools.logic.export

import androidx.documentfile.provider.DocumentFile
import com.mty.exptools.ExptoolsApp.Companion.context
import com.mty.exptools.logic.dao.PreferenceDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import java.io.OutputStream

object ExportIo {

    suspend fun tryWriteToChosenDir(
        fileName: String,
        mime: String,
        writer: (OutputStream) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        val dirStr = PreferenceDao.getExportDirUri() ?: return@withContext false
        val dirUri = dirStr.toUri()
        val tree = DocumentFile.fromTreeUri(context, dirUri) ?: return@withContext false
        val target = tree.findFile(fileName)
            ?: tree.createFile(mime, fileName)
            ?: return@withContext false
        context.contentResolver.openOutputStream(target.uri)?.use {
            writer(it)
            true
        } == true
    }

}