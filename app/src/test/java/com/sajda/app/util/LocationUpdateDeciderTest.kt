package com.sajda.app.util

import com.sajda.app.domain.model.PrayerTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LocationUpdateDeciderTest {

    @Test
    fun refreshWhenCityChanges() {
        val today = LocalDate.of(2026, 4, 9)
        assertTrue(
            LocationUpdateDecider.shouldRefresh(
                savedCity = "Kota Bogor",
                newCity = "Kabupaten Bandung",
                cachedSchedules = emptyList(),
                today = today
            )
        )
    }

    @Test
    fun skipWhenCitySameAndTodayScheduleExists() {
        val today = LocalDate.of(2026, 4, 9)
        val schedules = listOf(
            PrayerTime(
                date = today.toString(),
                locationName = "Kota Bogor",
                latitude = -6.59,
                longitude = 106.79,
                fajr = "04:30",
                dhuhr = "11:55",
                asr = "15:18",
                maghrib = "17:52",
                isha = "19:01",
                qiblaDirection = 294.0
            )
        )

        assertFalse(
            LocationUpdateDecider.shouldRefresh(
                savedCity = "Bogor",
                newCity = "Kota Bogor",
                cachedSchedules = schedules,
                today = today
            )
        )
    }
}
