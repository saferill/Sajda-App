package com.sajda.app.util

import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.PrayerTime
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtils {
    private val indonesianLocale = Locale("id", "ID")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", indonesianLocale)
    private val displayDateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", indonesianLocale)
    private val displayDateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", indonesianLocale)

    fun today(): LocalDate = LocalDate.now()

    fun todayString(): String = today().toString()

    fun currentTimeString(): String = LocalTime.now().format(timeFormatter)

    fun dateTimeString(dateTime: LocalDateTime = LocalDateTime.now()): String =
        dateTime.format(displayDateTimeFormatter)

    fun parseTime(value: String): LocalTime = LocalTime.parse(value, timeFormatter)

    fun formatDateLabel(date: String): String = LocalDate.parse(date).format(displayDateFormatter)

    fun prayerEntries(prayerTime: PrayerTime): List<Pair<PrayerName, String>> = listOf(
        PrayerName.FAJR to prayerTime.fajr,
        PrayerName.DHUHR to prayerTime.dhuhr,
        PrayerName.ASR to prayerTime.asr,
        PrayerName.MAGHRIB to prayerTime.maghrib,
        PrayerName.ISHA to prayerTime.isha
    )

    fun nextPrayer(prayerTime: PrayerTime, now: LocalTime = LocalTime.now()): Pair<PrayerName, String> {
        val nextPrayer = nextPrayerDateTime(
            prayerTime = prayerTime,
            now = LocalDateTime.of(LocalDate.now(), now)
        )
        return nextPrayer.first to nextPrayer.second.toLocalTime().format(timeFormatter)
    }

    fun countdownToNextPrayer(prayerTime: PrayerTime, now: LocalTime = LocalTime.now()): String {
        val duration = durationUntilNextPrayer(prayerTime, now)

        val totalMinutes = duration.toMinutes().coerceAtLeast(0)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}j ${minutes}m" else "${minutes}m"
    }

    fun countdownClockToNextPrayer(prayerTime: PrayerTime, now: LocalTime = LocalTime.now()): String {
        val duration = durationUntilNextPrayer(prayerTime, now)

        val totalSeconds = duration.seconds.coerceAtLeast(0)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    private fun durationUntilNextPrayer(
        prayerTime: PrayerTime,
        now: LocalTime
    ): Duration {
        val currentDateTime = LocalDateTime.of(LocalDate.now(), now)
        return Duration.between(currentDateTime, nextPrayerDateTime(prayerTime, currentDateTime).second)
    }

    private fun nextPrayerDateTime(
        prayerTime: PrayerTime,
        now: LocalDateTime = LocalDateTime.now()
    ): Pair<PrayerName, LocalDateTime> {
        val today = now.toLocalDate()
        val todaysSchedule = prayerEntries(prayerTime).map { (prayerName, time) ->
            prayerName to LocalDateTime.of(today, parseTime(time))
        }

        return todaysSchedule.firstOrNull { (_, prayerDateTime) ->
            prayerDateTime.isAfter(now)
        } ?: (
            PrayerName.FAJR to LocalDateTime.of(
                today.plusDays(1),
                parseTime(prayerTime.fajr)
            )
            )
    }
}

object NumberUtils {
    private val arabicNumbers = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")

    fun formatArabicNumber(number: Int): String {
        return number.toString().map { arabicNumbers[it.digitToInt()] }.joinToString("")
    }
}
