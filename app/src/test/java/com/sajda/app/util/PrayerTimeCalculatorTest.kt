package com.sajda.app.util

import com.sajda.app.domain.model.AsrMadhhab
import com.sajda.app.domain.model.PrayerCalculationMethod
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PrayerTimeCalculatorTest {

    @Test
    fun prayerTimes_areOrderedAcrossMajorIndonesianCities() {
        val testDate = LocalDate.of(2026, 4, 2)

        LocationConstants.cityPresets.forEach { city ->
            PrayerCalculationMethod.entries.forEach { method ->
                val details = PrayerTimeCalculator.calculateDetailedPrayerTimes(
                    date = testDate,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    locationName = city.name,
                    calculationMethod = method,
                    asrMadhhab = AsrMadhhab.SHAFII
                )

                val imsak = DateTimeUtils.parseTime(details.imsak)
                val fajr = DateTimeUtils.parseTime(details.fajr)
                val sunrise = DateTimeUtils.parseTime(details.sunrise)
                val dhuhr = DateTimeUtils.parseTime(details.dhuhr)
                val asr = DateTimeUtils.parseTime(details.asr)
                val maghrib = DateTimeUtils.parseTime(details.maghrib)
                val isha = DateTimeUtils.parseTime(details.isha)

                assertTrue("Imsak should be before fajr for ${city.name} / ${method.name}", imsak.isBefore(fajr))
                assertTrue("Fajr should be before sunrise for ${city.name} / ${method.name}", fajr.isBefore(sunrise))
                assertTrue("Sunrise should be before dhuhr for ${city.name} / ${method.name}", sunrise.isBefore(dhuhr))
                assertTrue("Dhuhr should be before asr for ${city.name} / ${method.name}", dhuhr.isBefore(asr))
                assertTrue("Asr should be before maghrib for ${city.name} / ${method.name}", asr.isBefore(maghrib))
                assertTrue("Maghrib should be before isha for ${city.name} / ${method.name}", maghrib.isBefore(isha))
                assertTrue(
                    "Qibla direction should be normalized for ${city.name}",
                    details.qiblaDirection in 0.0..360.0
                )
            }
        }
    }
}
