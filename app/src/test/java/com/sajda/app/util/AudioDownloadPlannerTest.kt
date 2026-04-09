package com.sajda.app.util

import com.sajda.app.domain.model.AudioDownloadMode
import com.sajda.app.domain.model.QuranReciter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioDownloadPlannerTest {

    @Test
    fun selectedReciterMode_onlyContainsActiveReciter() {
        val plan = AudioDownloadPlanner.plan(
            totalVerses = 7,
            selectedReciter = QuranReciter.ABDUL_MUHSIN_AL_QASIM,
            mode = AudioDownloadMode.SELECTED_RECITER_ONLY
        )

        assertEquals(listOf(QuranReciter.ABDUL_MUHSIN_AL_QASIM), plan.reciters)
        assertTrue(plan.estimatedBytes > 0)
    }

    @Test
    fun allRecitersMode_containsEveryReciter() {
        val plan = AudioDownloadPlanner.plan(
            totalVerses = 286,
            selectedReciter = QuranReciter.MISYARI_RASYID_AL_AFASI,
            mode = AudioDownloadMode.ALL_RECITERS
        )

        assertEquals(QuranReciter.entries.size, plan.reciters.size)
        assertTrue(plan.estimatedBytes > 286L)
    }

    @Test
    fun wifiOnlyDownload_blocksWhenWifiUnavailable() {
        assertTrue(AudioDownloadPlanner.shouldBlockForNetwork(wifiOnly = true, isWifiConnected = false))
        assertFalse(AudioDownloadPlanner.shouldBlockForNetwork(wifiOnly = true, isWifiConnected = true))
        assertFalse(AudioDownloadPlanner.shouldBlockForNetwork(wifiOnly = false, isWifiConnected = false))
    }
}
