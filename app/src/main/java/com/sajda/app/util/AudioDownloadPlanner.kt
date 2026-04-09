package com.sajda.app.util

import com.sajda.app.domain.model.AudioDownloadMode
import com.sajda.app.domain.model.QuranReciter

data class AudioDownloadPlan(
    val reciters: List<QuranReciter>,
    val estimatedBytes: Long
)

object AudioDownloadPlanner {
    private const val ESTIMATED_PER_VERSE_BYTES = 480_000L
    private const val BASELINE_BYTES_PER_RECITER = 3_200_000L

    fun plan(
        totalVerses: Int,
        selectedReciter: QuranReciter,
        mode: AudioDownloadMode
    ): AudioDownloadPlan {
        val reciters = when (mode) {
            AudioDownloadMode.SELECTED_RECITER_ONLY -> listOf(selectedReciter)
            AudioDownloadMode.ALL_RECITERS -> QuranReciter.entries.toList()
        }
        val perReciterBytes = maxOf(BASELINE_BYTES_PER_RECITER, totalVerses * ESTIMATED_PER_VERSE_BYTES)
        return AudioDownloadPlan(
            reciters = reciters,
            estimatedBytes = perReciterBytes * reciters.size
        )
    }

    fun shouldBlockForNetwork(
        wifiOnly: Boolean,
        isWifiConnected: Boolean
    ): Boolean = wifiOnly && !isWifiConnected
}
