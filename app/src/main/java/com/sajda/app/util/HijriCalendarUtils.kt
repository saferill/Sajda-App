package com.sajda.app.util

import com.sajda.app.domain.model.AppLanguage
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class HijriCalendarCell(
    val date: LocalDate,
    val gregorianDay: Int,
    val hijriDay: Int,
    val hijriMonthValue: Int,
    val hijriYear: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isRamadan: Boolean
)

data class IslamicEventSummary(
    val title: String,
    val hijriLabel: String,
    val gregorianDate: LocalDate
)

private val indonesianLocale = Locale("id", "ID")
private val englishLocale = Locale.ENGLISH

private fun AppLanguage.locale(): Locale = if (this == AppLanguage.ENGLISH) englishLocale else indonesianLocale

private fun LocalDate.toHijriDate(): HijrahDate = HijrahDate.from(this)

fun hijriMonthName(monthValue: Int, appLanguage: AppLanguage): String {
    val names = if (appLanguage == AppLanguage.ENGLISH) {
        listOf(
            "Muharram",
            "Safar",
            "Rabi' al-Awwal",
            "Rabi' al-Thani",
            "Jumada al-Awwal",
            "Jumada al-Thani",
            "Rajab",
            "Sha'ban",
            "Ramadan",
            "Shawwal",
            "Dhu al-Qi'dah",
            "Dhu al-Hijjah"
        )
    } else {
        listOf(
            "Muharram",
            "Safar",
            "Rabiul Awal",
            "Rabiul Akhir",
            "Jumadil Awal",
            "Jumadil Akhir",
            "Rajab",
            "Sya'ban",
            "Ramadhan",
            "Syawal",
            "Dzulqa'dah",
            "Dzulhijjah"
        )
    }
    return names.getOrElse(monthValue - 1) { monthValue.toString() }
}

fun currentHijriSummary(
    appLanguage: AppLanguage,
    date: LocalDate = LocalDate.now()
): String {
    val hijri = date.toHijriDate()
    val month = hijriMonthName(hijri.get(java.time.temporal.ChronoField.MONTH_OF_YEAR), appLanguage)
    val year = hijri.get(java.time.temporal.ChronoField.YEAR)
    val day = hijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
    return "$day $month $year H"
}

fun currentGregorianSummary(
    appLanguage: AppLanguage,
    date: LocalDate = LocalDate.now()
): String {
    val formatter = if (appLanguage == AppLanguage.ENGLISH) {
        DateTimeFormatter.ofPattern("d MMMM yyyy", englishLocale)
    } else {
        DateTimeFormatter.ofPattern("d MMMM yyyy", indonesianLocale)
    }
    return date.format(formatter)
}

fun currentDayName(
    appLanguage: AppLanguage,
    date: LocalDate = LocalDate.now()
): String {
    return date.dayOfWeek.getDisplayName(TextStyle.FULL, appLanguage.locale())
        .replaceFirstChar { it.titlecase(appLanguage.locale()) }
}

fun buildHijriCalendarCells(
    month: YearMonth = YearMonth.now(),
    today: LocalDate = LocalDate.now()
): List<HijriCalendarCell> {
    val firstOfMonth = month.atDay(1)
    val gridStart = firstOfMonth.minusDays(((firstOfMonth.dayOfWeek.value % 7).toLong()))
    return (0 until 42).map { offset ->
        val date = gridStart.plusDays(offset.toLong())
        val hijri = date.toHijriDate()
        val hijriMonth = hijri.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
        HijriCalendarCell(
            date = date,
            gregorianDay = date.dayOfMonth,
            hijriDay = hijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH),
            hijriMonthValue = hijriMonth,
            hijriYear = hijri.get(java.time.temporal.ChronoField.YEAR),
            isCurrentMonth = date.month == month.month,
            isToday = date == today,
            isRamadan = hijriMonth == 9
        )
    }
}

fun hijriRangeLabel(
    appLanguage: AppLanguage,
    month: YearMonth = YearMonth.now()
): String {
    val cells = buildHijriCalendarCells(month).filter { it.isCurrentMonth }
    val first = cells.firstOrNull() ?: return currentHijriSummary(appLanguage)
    val last = cells.lastOrNull() ?: return currentHijriSummary(appLanguage)
    val firstMonth = hijriMonthName(first.hijriMonthValue, appLanguage)
    val lastMonth = hijriMonthName(last.hijriMonthValue, appLanguage)
    return if (first.hijriMonthValue == last.hijriMonthValue) {
        "$firstMonth ${first.hijriYear} H"
    } else {
        "$firstMonth - $lastMonth ${last.hijriYear} H"
    }
}

fun hijriWeekdayHeaders(appLanguage: AppLanguage): List<String> {
    return if (appLanguage == AppLanguage.ENGLISH) {
        listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    } else {
        listOf("MIN", "SEN", "SEL", "RAB", "KAM", "JUM", "SAB")
    }
}

fun nextRamadanStart(date: LocalDate = LocalDate.now()): LocalDate {
    val hijriNow = date.toHijriDate()
    val currentHijriYear = hijriNow.get(java.time.temporal.ChronoField.YEAR)
    val thisYearRamadan = LocalDate.from(HijrahDate.of(currentHijriYear, 9, 1))
    return if (!date.isAfter(thisYearRamadan)) {
        thisYearRamadan
    } else {
        LocalDate.from(HijrahDate.of(currentHijriYear + 1, 9, 1))
    }
}

fun ramadanProgress(date: LocalDate = LocalDate.now()): Pair<Int, Int>? {
    val hijri = date.toHijriDate()
    val month = hijri.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
    if (month != 9) return null
    val day = hijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
    val monthLength = hijri.lengthOfMonth()
    return day to monthLength
}

fun upcomingIslamicEvents(
    appLanguage: AppLanguage,
    date: LocalDate = LocalDate.now()
): List<IslamicEventSummary> {
    val hijriYear = date.toHijriDate().get(java.time.temporal.ChronoField.YEAR)
    val candidates = listOf(
        IslamicEventSummary(
            title = if (appLanguage == AppLanguage.ENGLISH) "Start of Ramadan" else "Awal Ramadhan",
            hijriLabel = "1 ${hijriMonthName(9, appLanguage)} $hijriYear H",
            gregorianDate = LocalDate.from(HijrahDate.of(hijriYear, 9, 1))
        ),
        IslamicEventSummary(
            title = if (appLanguage == AppLanguage.ENGLISH) "Nuzulul Qur'an" else "Nuzulul Qur'an",
            hijriLabel = "17 ${hijriMonthName(9, appLanguage)} $hijriYear H",
            gregorianDate = LocalDate.from(HijrahDate.of(hijriYear, 9, 17))
        ),
        IslamicEventSummary(
            title = if (appLanguage == AppLanguage.ENGLISH) "Eid al-Fitr" else "Hari Raya Idul Fitri",
            hijriLabel = "1 ${hijriMonthName(10, appLanguage)} $hijriYear H",
            gregorianDate = LocalDate.from(HijrahDate.of(hijriYear, 10, 1))
        )
    )

    val nextYear = hijriYear + 1
    val nextYearCandidates = listOf(
        IslamicEventSummary(
            title = if (appLanguage == AppLanguage.ENGLISH) "Start of Ramadan" else "Awal Ramadhan",
            hijriLabel = "1 ${hijriMonthName(9, appLanguage)} $nextYear H",
            gregorianDate = LocalDate.from(HijrahDate.of(nextYear, 9, 1))
        ),
        IslamicEventSummary(
            title = if (appLanguage == AppLanguage.ENGLISH) "Nuzulul Qur'an" else "Nuzulul Qur'an",
            hijriLabel = "17 ${hijriMonthName(9, appLanguage)} $nextYear H",
            gregorianDate = LocalDate.from(HijrahDate.of(nextYear, 9, 17))
        ),
        IslamicEventSummary(
            title = if (appLanguage == AppLanguage.ENGLISH) "Eid al-Fitr" else "Hari Raya Idul Fitri",
            hijriLabel = "1 ${hijriMonthName(10, appLanguage)} $nextYear H",
            gregorianDate = LocalDate.from(HijrahDate.of(nextYear, 10, 1))
        )
    )

    return (candidates + nextYearCandidates)
        .filter { !it.gregorianDate.isBefore(date) }
        .sortedBy { it.gregorianDate }
        .take(3)
}

fun daysUntil(targetDate: LocalDate, from: LocalDate = LocalDate.now()): Long {
    return java.time.temporal.ChronoUnit.DAYS.between(from, targetDate)
}

fun isFriday(date: LocalDate = LocalDate.now()): Boolean = date.dayOfWeek == DayOfWeek.FRIDAY
