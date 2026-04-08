package com.sajda.app.data.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.google.gson.JsonObject
import com.sajda.app.data.api.EquranApi
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.domain.model.AudioDownloadState
import com.sajda.app.domain.model.QuranReciter
import com.sajda.app.domain.model.Surah
import com.sajda.app.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class AudioRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val quranRepository: QuranRepository,
    private val preferencesDataStore: PreferencesDataStore,
    private val equranApi: EquranApi
) {
    private val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _downloadStates = MutableStateFlow<Map<Int, AudioDownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<Int, AudioDownloadState>> = _downloadStates.asStateFlow()

    fun audioFileFor(surahNumber: Int, reciter: QuranReciter): File {
        val musicDir = appContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val targetDir = File(musicDir, Constants.AUDIO_DOWNLOAD_DIR).apply { mkdirs() }
        return File(targetDir, Constants.formatAudioFileName(surahNumber, reciter.id))
    }

    suspend fun downloadSurah(surah: Surah) {
        val reciter = preferencesDataStore.settingsFlow.first().selectedQuranReciter
        val targetFile = audioFileFor(surah.number, reciter)
        targetFile.parentFile?.mkdirs()
        if (targetFile.exists()) {
            quranRepository.updateAudioState(
                surahNumber = surah.number,
                isDownloaded = true,
                localAudioPath = targetFile.absolutePath,
                downloadedAt = targetFile.lastModified(),
                downloadedReciterId = reciter.id
            )
            return
        }

        val audioUrl = resolveAudioUrl(surah.number, reciter) ?: surah.audioUrl

        val request = DownloadManager.Request(Uri.parse(audioUrl))
            .setTitle("Murattal ${surah.transliteration} • ${reciter.title}")
            .setDescription("Mengunduh audio surah untuk pemutaran offline")
            .setMimeType("audio/mpeg")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationUri(Uri.fromFile(targetFile))

        val downloadId = downloadManager.enqueue(request)
        _downloadStates.update { current ->
            current + (surah.number to AudioDownloadState(surah.number, progress = 0, isDownloading = true))
        }

        scope.launch {
            monitorDownload(downloadId, surah, targetFile, reciter)
        }
    }

    suspend fun deleteSurahAudio(surahNumber: Int) {
        val reciter = preferencesDataStore.settingsFlow.first().selectedQuranReciter
        val file = audioFileFor(surahNumber, reciter)
        if (file.exists()) {
            file.delete()
        }
        quranRepository.updateAudioState(
            surahNumber = surahNumber,
            isDownloaded = false,
            localAudioPath = null,
            downloadedAt = null,
            downloadedReciterId = null
        )
        _downloadStates.update { it - surahNumber }
    }

    private suspend fun monitorDownload(
        downloadId: Long,
        surah: Surah,
        targetFile: File,
        reciter: QuranReciter
    ) {
        while (true) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            downloadManager.query(query).use { cursor ->
                if (!cursor.moveToFirst()) {
                    markFailed(surah.number)
                    return
                }

                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val downloaded = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                )
                val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val progress = if (total > 0L) ((downloaded * 100L) / total).toInt() else 0

                when (status) {
                    DownloadManager.STATUS_PENDING,
                    DownloadManager.STATUS_RUNNING,
                    DownloadManager.STATUS_PAUSED -> {
                        _downloadStates.update { current ->
                            current + (
                                surah.number to AudioDownloadState(
                                    surahNumber = surah.number,
                                    progress = progress.coerceIn(0, 100),
                                    isDownloading = true
                                )
                                )
                        }
                    }

                    DownloadManager.STATUS_SUCCESSFUL -> {
                        surah.localAudioPath
                            ?.takeIf { it != targetFile.absolutePath }
                            ?.let(::File)
                            ?.takeIf { it.exists() }
                            ?.delete()
                        quranRepository.updateAudioState(
                            surahNumber = surah.number,
                            isDownloaded = true,
                            localAudioPath = targetFile.absolutePath,
                            downloadedAt = System.currentTimeMillis(),
                            downloadedReciterId = reciter.id
                        )
                        _downloadStates.update { it - surah.number }
                        return
                    }

                    DownloadManager.STATUS_FAILED -> {
                        markFailed(surah.number)
                        return
                    }
                }
            }
            delay(500)
        }
    }

    private fun markFailed(surahNumber: Int) {
        _downloadStates.update { current ->
            current + (
                surahNumber to AudioDownloadState(
                    surahNumber = surahNumber,
                    progress = 0,
                    isDownloading = false,
                    isFailed = true
                )
                )
        }
    }

    private suspend fun resolveAudioUrl(
        surahNumber: Int,
        reciter: QuranReciter
    ): String? {
        val detail = runCatching {
            equranApi.getSurahDetail("https://equran.id/api/v2/surat/$surahNumber")
        }.getOrNull() ?: return null
        val data = detail.getAsJsonObject("data") ?: return null
        val audioFull = data.getAsJsonObject("audioFull") ?: return null
        return audioFull.stringValue(reciter.id)
    }

    private fun JsonObject.stringValue(key: String): String? {
        val raw = get(key) ?: return null
        if (raw.isJsonNull) return null
        return raw.asString
    }
}
