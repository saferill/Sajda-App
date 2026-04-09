package com.sajda.app.util

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
    val estimatedPerVerseBytes = 480_000L
    val baselineBytes = 3_200_000L
    return maxOf(baselineBytes, totalVerses * estimatedPerVerseBytes)
}
