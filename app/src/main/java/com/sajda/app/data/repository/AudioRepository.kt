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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class AudioRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val quranRepository: QuranRepository,
    private val preferencesDataStore: PreferencesDataStore,
    private val equranApi: EquranApi
) {
    private val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    private val _downloadStates = MutableStateFlow<Map<Int, AudioDownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<Int, AudioDownloadState>> = _downloadStates.asStateFlow()

    fun audioFileFor(surahNumber: Int, reciter: QuranReciter): File = audioFileFor(surahNumber, reciter.id)

    fun resolveBestAudioFile(
        surah: Surah,
        preferredReciter: QuranReciter
    ): File? {
        val preferredFile = audioFileFor(surah.number, preferredReciter)
        if (preferredFile.exists()) return preferredFile

        return QuranReciter.entries
            .map { audioFileFor(surah.number, it) }
            .firstOrNull { it.exists() }
            ?: surah.localAudioPath
                ?.let(::File)
                ?.takeIf { it.exists() }
    }

    suspend fun downloadSurah(surah: Surah) = withContext(Dispatchers.IO) {
        val selectedReciter = preferencesDataStore.settingsFlow.first().selectedQuranReciter
        val audioUrls = resolveAudioUrls(surah.number)
        val totalReciters = QuranReciter.entries.size

        updateProgress(surah.number, 0, isDownloading = true)

        QuranReciter.entries.forEachIndexed { index, reciter ->
            val targetFile = audioFileFor(surah.number, reciter)
            targetFile.parentFile?.mkdirs()
            val completedProgress = ((index.toFloat() / totalReciters) * 100f).roundToInt()

            if (targetFile.exists()) {
                updateProgress(surah.number, completedProgress, isDownloading = true)
                return@forEachIndexed
            }

            val audioUrl = audioUrls[reciter.id] ?: surah.audioUrl
            val downloadId = enqueueDownload(surah, reciter, targetFile, audioUrl)
            val success = monitorDownload(
                downloadId = downloadId,
                surahNumber = surah.number,
                completedReciters = index,
                totalReciters = totalReciters
            )
            if (!success) {
                markFailed(surah.number)
                return@withContext
            }
        }

        val representativeFile = audioFileFor(surah.number, selectedReciter)
            .takeIf { it.exists() }
            ?: QuranReciter.entries
                .map { audioFileFor(surah.number, it) }
                .firstOrNull { it.exists() }

        quranRepository.updateAudioState(
            surahNumber = surah.number,
            isDownloaded = representativeFile != null,
            localAudioPath = representativeFile?.absolutePath,
            downloadedAt = System.currentTimeMillis(),
            downloadedReciterId = selectedReciter.id
        )
        _downloadStates.update { it - surah.number }
    }

    suspend fun deleteSurahAudio(surahNumber: Int) = withContext(Dispatchers.IO) {
        QuranReciter.entries.forEach { reciter ->
            val file = audioFileFor(surahNumber, reciter)
            if (file.exists()) {
                file.delete()
            }
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

    private fun enqueueDownload(
        surah: Surah,
        reciter: QuranReciter,
        targetFile: File,
        audioUrl: String
    ): Long {
        val request = DownloadManager.Request(Uri.parse(audioUrl))
            .setTitle("Murattal ${surah.transliteration} - ${reciter.title}")
            .setDescription("Mengunduh paket audio surah untuk offline")
            .setMimeType("audio/mpeg")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationUri(Uri.fromFile(targetFile))
        return downloadManager.enqueue(request)
    }

    private suspend fun monitorDownload(
        downloadId: Long,
        surahNumber: Int,
        completedReciters: Int,
        totalReciters: Int
    ): Boolean = withContext(Dispatchers.IO) {
        while (true) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            downloadManager.query(query).use { cursor ->
                if (!cursor.moveToFirst()) {
                    return@withContext false
                }

                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val downloaded = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                )
                val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val currentProgress = if (total > 0L) {
                    ((downloaded * 100L) / total).toInt().coerceIn(0, 100)
                } else {
                    0
                }
                val overallProgress = (
                    ((completedReciters * 100f) + currentProgress.toFloat()) / totalReciters.toFloat()
                    ).roundToInt()

                when (status) {
                    DownloadManager.STATUS_PENDING,
                    DownloadManager.STATUS_RUNNING,
                    DownloadManager.STATUS_PAUSED -> {
                        updateProgress(surahNumber, overallProgress, isDownloading = true)
                    }

                    DownloadManager.STATUS_SUCCESSFUL -> {
                        val completedProgress = (((completedReciters + 1) * 100f) / totalReciters.toFloat()).roundToInt()
                        updateProgress(surahNumber, completedProgress, isDownloading = true)
                        return@withContext true
                    }

                    DownloadManager.STATUS_FAILED -> return@withContext false
                }
            }
            kotlinx.coroutines.delay(500)
        }
        @Suppress("UNREACHABLE_CODE")
        false
    }

    private fun updateProgress(
        surahNumber: Int,
        progress: Int,
        isDownloading: Boolean,
        isFailed: Boolean = false
    ) {
        _downloadStates.update { current ->
            current + (
                surahNumber to AudioDownloadState(
                    surahNumber = surahNumber,
                    progress = progress.coerceIn(0, 100),
                    isDownloading = isDownloading,
                    isFailed = isFailed
                )
                )
        }
    }

    private fun markFailed(surahNumber: Int) {
        updateProgress(
            surahNumber = surahNumber,
            progress = 0,
            isDownloading = false,
            isFailed = true
        )
    }

    private suspend fun resolveAudioUrls(surahNumber: Int): Map<String, String> {
        val detail = runCatching {
            equranApi.getSurahDetail("https://equran.id/api/v2/surat/$surahNumber")
        }.getOrNull() ?: return emptyMap()
        val data = detail.getAsJsonObject("data") ?: return emptyMap()
        val audioFull = data.getAsJsonObject("audioFull") ?: return emptyMap()
        return QuranReciter.entries.mapNotNull { reciter ->
            audioFull.stringValue(reciter.id)?.let { reciter.id to it }
        }.toMap()
    }

    private fun audioFileFor(surahNumber: Int, reciterId: String): File {
        val musicDir = appContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val targetDir = File(musicDir, Constants.AUDIO_DOWNLOAD_DIR).apply { mkdirs() }
        return File(targetDir, Constants.formatAudioFileName(surahNumber, reciterId))
    }

    private fun JsonObject.stringValue(key: String): String? {
        val raw = get(key) ?: return null
        if (raw.isJsonNull) return null
        return raw.asString
    }
}
