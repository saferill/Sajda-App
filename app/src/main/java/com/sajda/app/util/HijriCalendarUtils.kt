package com.sajda.app.util

import com.sajda.app.domain.model.AppLanguage
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
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

data class HijriCalendarPage(
    val year: Int,
    val monthValue: Int
)

data class IslamicEventSummary(
    val title: String,
    val hijriLabel: String,
    val gregorianDate: LocalDate
)

private val languageLocales = mapOf(
    AppLanguage.INDONESIAN to Locale("id", "ID"),
    AppLanguage.ENGLISH to Locale.ENGLISH,
    AppLanguage.ARABIC to Locale("ar"),
    AppLanguage.TURKISH to Locale("tr", "TR"),
    AppLanguage.URDU to Locale("ur", "PK"),
    AppLanguage.FRENCH to Locale.FRENCH,
    AppLanguage.MALAY to Locale("ms", "MY"),
    AppLanguage.HINDI to Locale("hi", "IN")
)

private fun AppLanguage.locale(): Locale = languageLocales[this] ?: Locale.ENGLISH

private fun LocalDate.toHijriDate(): HijrahDate = HijrahDate.from(this)

private fun HijrahDate.page(): HijriCalendarPage {
    return HijriCalendarPage(
        year = get(ChronoField.YEAR),
        monthValue = get(ChronoField.MONTH_OF_YEAR)
    )
}

fun hijriMonthName(monthValue: Int, appLanguage: AppLanguage): String {
    val names = when (appLanguage) {
        AppLanguage.ARABIC -> listOf(
            "محرم",
            "صفر",
            "ربيع الأول",
            "ربيع الآخر",
            "جمادى الأولى",
            "جمادى الآخرة",
            "رجب",
            "شعبان",
            "رمضان",
            "شوال",
            "ذو القعدة",
            "ذو الحجة"
        )
        AppLanguage.TURKISH -> listOf(
            "Muharrem",
            "Safer",
            "Rebiülevvel",
            "Rebiülahir",
            "Cemaziyelevvel",
            "Cemaziyelahir",
            "Recep",
            "Şaban",
            "Ramazan",
            "Şevval",
            "Zilkade",
            "Zilhicce"
        )
        AppLanguage.URDU -> listOf(
            "محرم",
            "صفر",
            "ربیع الاول",
            "ربیع الثانی",
            "جمادی الاول",
            "جمادی الثانی",
            "رجب",
            "شعبان",
            "رمضان",
            "شوال",
            "ذوالقعدہ",
            "ذوالحجہ"
        )
        AppLanguage.FRENCH -> listOf(
            "Mouharram",
            "Safar",
            "Rabi al-awwal",
            "Rabi ath-thani",
            "Joumada al-oula",
            "Joumada ath-thania",
            "Rajab",
            "Chaabane",
            "Ramadan",
            "Chawwal",
            "Dhou al-qi`da",
            "Dhou al-hijja"
        )
        AppLanguage.MALAY -> listOf(
            "Muharam",
            "Safar",
            "Rabiulawal",
            "Rabiulakhir",
            "Jamadilawal",
            "Jamadilakhir",
            "Rejab",
            "Syaaban",
            "Ramadan",
            "Syawal",
            "Zulkaedah",
            "Zulhijjah"
        )
        AppLanguage.HINDI -> listOf(
            "मुहर्रम",
            "सफ़र",
            "रबी अल-अव्वल",
            "रबी अल-आख़िर",
            "जुमादा अल-अव्वल",
            "जुमादा अल-आख़िर",
            "रजब",
            "शाबान",
            "रमज़ान",
            "शव्वाल",
            "ज़ुल-क़ादा",
            "ज़ुल-हिज्जा"
        )
        AppLanguage.ENGLISH -> listOf(
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
        AppLanguage.INDONESIAN -> listOf(
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

fun currentHijriPage(date: LocalDate = LocalDate.now()): HijriCalendarPage = date.toHijriDate().page()

fun shiftHijriPage(
    page: HijriCalendarPage,
    monthDelta: Int
): HijriCalendarPage {
    val shifted = HijrahDate.of(page.year, page.monthValue, 1)
        .plus(monthDelta.toLong(), ChronoUnit.MONTHS)
    return HijriCalendarPage(
        year = shifted.get(ChronoField.YEAR),
        monthValue = shifted.get(ChronoField.MONTH_OF_YEAR)
    )
}

fun currentHijriSummary(
    appLanguage: AppLanguage,
    date: LocalDate = LocalDate.now()
): String {
    val hijri = date.toHijriDate()
    val month = hijriMonthName(hijri.get(ChronoField.MONTH_OF_YEAR), appLanguage)
    val year = hijri.get(ChronoField.YEAR)
    val day = hijri.get(ChronoField.DAY_OF_MONTH)
    return "$day $month $year H"
}

fun currentGregorianSummary(
    appLanguage: AppLanguage,
    date: LocalDate = LocalDate.now()
): String {
    return date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", appLanguage.locale()))
}

fun currentDayName(
    appLanguage: AppLanguage,
    date: LocalDate = LocalDate.now()
): String {
    val locale = appLanguage.locale()
    val raw = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
    return if (appLanguage == AppLanguage.ARABIC) raw else raw.replaceFirstChar { it.titlecase(locale) }
}

fun gregorianMonthLabel(
    appLanguage: AppLanguage,
    month: YearMonth = YearMonth.now()
): String {
    return month.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy", appLanguage.locale()))
}

fun buildHijriCalendarCells(
    page: HijriCalendarPage = currentHijriPage(),
    today: LocalDate = LocalDate.now()
): List<HijriCalendarCell> {
    val firstOfMonth = LocalDate.from(HijrahDate.of(page.year, page.monthValue, 1))
    val gridStart = firstOfMonth.minusDays((firstOfMonth.dayOfWeek.value % 7).toLong())
    return buildCalendarCells(
        gridStart = gridStart,
        today = today
    ) { hijriMonth, hijriYear ->
        hijriMonth == page.monthValue && hijriYear == page.year
    }
}

fun buildGregorianCalendarCells(
    month: YearMonth = YearMonth.now(),
    today: LocalDate = LocalDate.now()
): List<HijriCalendarCell> {
    val firstOfMonth = month.atDay(1)
    val gridStart = firstOfMonth.minusDays((firstOfMonth.dayOfWeek.value % 7).toLong())
    return buildCalendarCells(
        gridStart = gridStart,
        today = today
    ) { _, _ ->
        false
    }.map { cell ->
        cell.copy(isCurrentMonth = YearMonth.from(cell.date) == month)
    }
}

private fun buildCalendarCells(
    gridStart: LocalDate,
    today: LocalDate,
    currentMonthMatcher: (Int, Int) -> Boolean
): List<HijriCalendarCell> {
    return (0 until 42).map { offset ->
        val date = gridStart.plusDays(offset.toLong())
        val hijri = date.toHijriDate()
        val hijriMonth = hijri.get(ChronoField.MONTH_OF_YEAR)
        val hijriYear = hijri.get(ChronoField.YEAR)
        HijriCalendarCell(
            date = date,
            gregorianDay = date.dayOfMonth,
            hijriDay = hijri.get(ChronoField.DAY_OF_MONTH),
            hijriMonthValue = hijriMonth,
            hijriYear = hijriYear,
            isCurrentMonth = currentMonthMatcher(hijriMonth, hijriYear),
            isToday = date == today,
            isRamadan = hijriMonth == 9
        )
    }
}

fun hijriRangeLabel(
    appLanguage: AppLanguage,
    page: HijriCalendarPage = currentHijriPage()
): String {
    return "${hijriMonthName(page.monthValue, appLanguage)} ${page.year} H"
}

fun hijriWeekdayHeaders(appLanguage: AppLanguage): List<String> {
    val locale = appLanguage.locale()
    val baseSunday = LocalDate.of(2026, 1, 4)
    return (0..6).map { offset ->
        val raw = baseSunday.plusDays(offset.toLong())
            .dayOfWeek
            .getDisplayName(TextStyle.SHORT, locale)
            .replace(".", "")
        if (appLanguage == AppLanguage.ARABIC) raw else raw.uppercase(locale)
    }
}

fun nextRamadanStart(date: LocalDate = LocalDate.now()): LocalDate {
    val hijriNow = date.toHijriDate()
    val currentHijriYear = hijriNow.get(ChronoField.YEAR)
    val thisYearRamadan = LocalDate.from(HijrahDate.of(currentHijriYear, 9, 1))
    return if (!date.isAfter(thisYearRamadan)) {
        thisYearRamadan
    } else {
        LocalDate.from(HijrahDate.of(currentHijriYear + 1, 9, 1))
    }
}

fun ramadanProgress(date: LocalDate = LocalDate.now()): Pair<Int, Int>? {
    val hijri = date.toHijriDate()
    val month = hijri.get(ChronoField.MONTH_OF_YEAR)
    if (month != 9) return null
    val day = hijri.get(ChronoField.DAY_OF_MONTH)
    val monthLength = hijri.lengthOfMonth()
    return day to monthLength
}

fun upcomingIslamicEvents(
    appLanguage: AppLanguage,
    date: LocalDate = LocalDate.now()
): List<IslamicEventSummary> {
    val hijriYear = date.toHijriDate().get(ChronoField.YEAR)
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
    return ChronoUnit.DAYS.between(from, targetDate)
}

fun isFriday(date: LocalDate = LocalDate.now()): Boolean = date.dayOfWeek == DayOfWeek.FRIDAY
