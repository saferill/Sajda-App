package com.sajda.app.util

import com.sajda.app.domain.model.AudioDownloadMode
import com.sajda.app.domain.model.QuranReciter
import com.sajda.app.domain.model.Surah
import java.io.File

fun Surah.hasDownloadedAudioFor(reciter: QuranReciter): Boolean {
    if (downloadedReciterIds.contains(reciter.id)) return true
    if (downloadedReciterId != reciter.id) return false
    val path = localAudioPath ?: return false
    return File(path).exists()
}

fun Surah.hasAnyDownloadedAudio(): Boolean = downloadedReciterIds.isNotEmpty() || !localAudioPath.isNullOrBlank()

fun Surah.audioBundleSizeBytes(): Long {
    if (downloadedAudioBytes > 0L) return downloadedAudioBytes
    return AudioDownloadPlanner
        .plan(
            totalVerses = totalVerses,
            selectedReciter = QuranReciter.MISYARI_RASYID_AL_AFASI,
            mode = AudioDownloadMode.ALL_RECITERS
        )
        .estimatedBytes
}

fun Surah.audioBundleSizeBytes(
    mode: AudioDownloadMode,
    selectedReciter: QuranReciter
): Long {
    if (downloadedAudioBytes > 0L && mode == AudioDownloadMode.ALL_RECITERS) return downloadedAudioBytes
    return AudioDownloadPlanner
        .plan(
            totalVerses = totalVerses,
            selectedReciter = selectedReciter,
            mode = mode
        )
        .estimatedBytes
}
