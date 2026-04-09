package com.sajda.app.util

import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.PrayerTime
import com.sajda.app.domain.model.UserSettings
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ScheduledPrayerEntry(
    val prayerTime: PrayerTime,
    val prayerName: PrayerName,
    val timeValue: String,
    val scheduledAt: LocalDateTime
)

object AdhanSchedulePlanner {
    fun upcoming(
        prayerTimes: List<PrayerTime>,
        settings: UserSettings,
        referenceTime: LocalDateTime,
        maxItems: Int
    ): List<ScheduledPrayerEntry> {
        return prayerTimes.asSequence().flatMap { prayerTime ->
            PrayerName.entries.asSequence()
                .filter { isEnabled(settings, it) }
                .map { prayerName ->
                    val timeValue = timeFor(prayerTime, prayerName)
                    ScheduledPrayerEntry(
                        prayerTime = prayerTime,
                        prayerName = prayerName,
                        timeValue = timeValue,
                        scheduledAt = LocalDateTime.of(
                            LocalDate.parse(prayerTime.date),
                            LocalTime.parse(timeValue)
                        )
                    )
                }
        }
            .filter { it.scheduledAt.isAfter(referenceTime) }
            .sortedBy { it.scheduledAt }
            .take(maxItems)
            .toList()
    }

    fun isEnabled(settings: UserSettings, prayerName: PrayerName): Boolean {
        return when (prayerName) {
            PrayerName.FAJR -> settings.fajrAdzanEnabled
            PrayerName.DHUHR -> settings.dhuhrAdzanEnabled
            PrayerName.ASR -> settings.asrAdzanEnabled
            PrayerName.MAGHRIB -> settings.maghribAdzanEnabled
            PrayerName.ISHA -> settings.ishaAdzanEnabled
        }
    }

    fun timeFor(prayerTime: PrayerTime, prayerName: PrayerName): String {
        return when (prayerName) {
            PrayerName.FAJR -> prayerTime.fajr
            PrayerName.DHUHR -> prayerTime.dhuhr
            PrayerName.ASR -> prayerTime.asr
            PrayerName.MAGHRIB -> prayerTime.maghrib
            PrayerName.ISHA -> prayerTime.isha
        }
    }
}
