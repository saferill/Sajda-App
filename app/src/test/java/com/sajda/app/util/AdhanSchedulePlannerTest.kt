package com.sajda.app.util

import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.AsrMadhhab
import com.sajda.app.domain.model.CalendarDisplayMode
import com.sajda.app.domain.model.PrayerCalculationMethod
import com.sajda.app.domain.model.PrayerTime
import com.sajda.app.domain.model.QuranReciter
import com.sajda.app.domain.model.QuranReadingMode
import com.sajda.app.domain.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class AdhanSchedulePlannerTest {

    @Test
    fun planner_skipsDisabledPrayerAndSortsUpcoming() {
        val settings = UserSettings(
            appLanguage = AppLanguage.INDONESIAN,
            quranReadingMode = QuranReadingMode.ARABIC_INDONESIAN,
            selectedQuranReciter = QuranReciter.MISYARI_RASYID_AL_AFASI,
            calendarDisplayMode = CalendarDisplayMode.HIJRI,
            prayerCalculationMethod = PrayerCalculationMethod.KEMENAG,
            asrMadhhab = AsrMadhhab.SHAFII,
            dhuhrAdzanEnabled = false
        )
        val prayerTimes = listOf(
            PrayerTime(
                date = "2026-04-09",
                locationName = "Yogyakarta",
                latitude = -7.8,
                longitude = 110.3,
                fajr = "04:28",
                dhuhr = "11:42",
                asr = "15:01",
                maghrib = "17:39",
                isha = "18:48",
                qiblaDirection = 294.0
            )
        )

        val results = AdhanSchedulePlanner.upcoming(
            prayerTimes = prayerTimes,
            settings = settings,
            referenceTime = LocalDateTime.of(2026, 4, 9, 11, 30),
            maxItems = 5
        )

        assertEquals("Ashar", results.first().prayerName.label)
        assertTrue(results.none { it.prayerName.label == "Dzuhur" })
    }
}
