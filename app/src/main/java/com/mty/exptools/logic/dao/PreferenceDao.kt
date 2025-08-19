package com.mty.exptools.logic.dao

import android.content.Context
import com.mty.exptools.ExptoolsApp.Companion.context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// 提供 dataStore 扩展
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull

val Context.dataStore by preferencesDataStore(name = "exptools_prefs")

object PreferenceDao {
    private val KEY_AUTO_REFRESH_S = intPreferencesKey("auto_refresh_s")
    private val KEY_EXPORT_DIR_URI = stringPreferencesKey("export_dir_uri")

    suspend fun getAutoRefreshSeconds(): Int {
        return context.dataStore.data.map { it[KEY_AUTO_REFRESH_S] ?: 10 }.first()
    }

    fun observeAutoRefreshSeconds() = context.dataStore.data.map { it[KEY_AUTO_REFRESH_S] ?: 10 }

    suspend fun setAutoRefreshSeconds(seconds: Int) {
        context.dataStore.edit { it[KEY_AUTO_REFRESH_S] = seconds }
    }

    suspend fun setExportDirUri(uri: String) {
        context.dataStore.edit { it[KEY_EXPORT_DIR_URI] = uri }
    }

    suspend fun getExportDirUri(): String? {
        return context.dataStore.data.map { it[KEY_EXPORT_DIR_URI] }.firstOrNull()
    }
}