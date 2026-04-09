package com.sajda.app.data.repository

import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.domain.model.Bookmark
import com.sajda.app.domain.model.LastRead
import com.sajda.app.domain.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class LocalDataBackup(
    val version: Int = 1,
    val exportedAt: String,
    val settings: UserSettings,
    val bookmarks: List<Bookmark>,
    val lastRead: LastRead?
)

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val quranRepository: QuranRepository,
    private val preferencesDataStore: PreferencesDataStore
) {
    private val gson = Gson()

    suspend fun exportToDefaultFile(): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val exportedAt = java.time.LocalDateTime.now().toString()
            val backup = LocalDataBackup(
                exportedAt = exportedAt,
                settings = preferencesDataStore.settingsFlow.first(),
                bookmarks = quranRepository.getAllBookmarks(),
                lastRead = quranRepository.getLastRead()
            )
            val file = defaultBackupFile()
            file.parentFile?.mkdirs()
            file.writeText(gson.toJson(backup))
            preferencesDataStore.setLastBackupAt()
            file
        }
    }

    suspend fun restoreFromDefaultFile(): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val file = defaultBackupFile()
            require(file.exists()) { "File backup belum tersedia" }
            restoreFromFile(file)
            file
        }
    }

    suspend fun restoreFromFile(file: File) = withContext(Dispatchers.IO) {
        val payload = gson.fromJson(file.readText(), LocalDataBackup::class.java)
        preferencesDataStore.restoreSettings(payload.settings)
        quranRepository.replaceBookmarks(payload.bookmarks)
        quranRepository.replaceLastRead(payload.lastRead)
        preferencesDataStore.setLastRestoreAt()
    }

    fun defaultBackupFile(): File {
        val docsDir = appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: appContext.filesDir
        return File(docsDir, "nurapp-backup.json")
    }
}
