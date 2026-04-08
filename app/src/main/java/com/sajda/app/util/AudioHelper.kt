package com.sajda.app.util

import com.sajda.app.domain.model.QuranReciter
import com.sajda.app.domain.model.Surah
import java.io.File

fun Surah.hasDownloadedAudioFor(reciter: QuranReciter): Boolean {
    if (downloadedReciterId != reciter.id) return false
    val path = localAudioPath ?: return false
    return File(path).exists()
}
