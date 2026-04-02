package com.sajda.app.data.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.sajda.app.domain.model.AudioDownloadState
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
import kotlinx.coroutines.launch
import java.io.File

class AudioRepository(
    context: Context,
    private val quranRepository: QuranRepository
) {
    private val appContext = context.applicationContext
    private val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _downloadStates = MutableStateFlow<Map<Int, AudioDownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<Int, AudioDownloadState>> = _downloadStates.asStateFlow()

    fun audioFileFor(surahNumber: Int): File {
        val musicDir = appContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val targetDir = File(musicDir, Constants.AUDIO_DOWNLOAD_DIR).apply { mkdirs() }
        return File(targetDir, Constants.formatAudioFileName(surahNumber))
    }

    suspend fun downloadSurah(surah: Surah) {
        val targetFile = audioFileFor(surah.number)
        targetFile.parentFile?.mkdirs()
        if (targetFile.exists()) {
            quranRepository.updateAudioState(
                surahNumber = surah.number,
                isDownloaded = true,
                localAudioPath = targetFile.absolutePath,
                downloadedAt = targetFile.lastModified()
            )
            return
        }

        val request = DownloadManager.Request(Uri.parse(surah.audioUrl))
            .setTitle("Murattal ${surah.transliteration}")
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
            monitorDownload(downloadId, surah, targetFile)
        }
    }

    suspend fun deleteSurahAudio(surahNumber: Int) {
        val file = audioFileFor(surahNumber)
        if (file.exists()) {
            file.delete()
        }
        quranRepository.updateAudioState(
            surahNumber = surahNumber,
            isDownloaded = false,
            localAudioPath = null,
            downloadedAt = null
        )
        _downloadStates.update { it - surahNumber }
    }

    private suspend fun monitorDownload(downloadId: Long, surah: Surah, targetFile: File) {
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
                        quranRepository.updateAudioState(
                            surahNumber = surah.number,
                            isDownloaded = true,
                            localAudioPath = targetFile.absolutePath,
                            downloadedAt = System.currentTimeMillis()
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
}
