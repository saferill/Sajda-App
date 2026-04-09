package com.sajda.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.CalendarDisplayMode
import com.sajda.app.domain.model.HadithEntry
import com.sajda.app.domain.model.PrayerTime
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.ui.component.HeroCard
import com.sajda.app.ui.component.MetadataChip
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.theme.surfaceContainerLow
import com.sajda.app.ui.viewmodel.AppUpdateUiState
import com.sajda.app.ui.viewmodel.SpiritualContentUiState
import com.sajda.app.util.HijriCalendarPage
import com.sajda.app.util.buildHijriCalendarCells
import com.sajda.app.util.buildGregorianCalendarCells
import com.sajda.app.util.currentHijriSummary
import com.sajda.app.util.currentHijriPage
import com.sajda.app.util.daysUntil
import com.sajda.app.util.gregorianMonthLabel
import com.sajda.app.util.hijriRangeLabel
import com.sajda.app.util.hijriWeekdayHeaders
import com.sajda.app.util.isEnglish
import com.sajda.app.util.nextRamadanStart
import com.sajda.app.util.pick
import com.sajda.app.util.ramadanProgress
import com.sajda.app.util.shiftHijriPage
import com.sajda.app.util.upcomingIslamicEvents
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoreHubScreen(
    settings: UserSettings,
    updateState: AppUpdateUiState,
    onOpenHadith: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenRamadhan: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPermissionSetup: () -> Unit
) {
    val isEnglish = settings.appLanguage == AppLanguage.ENGLISH

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            HeroCard {
                Text(
                    text = if (isEnglish) "Explore NurApp" else "Jelajahi NurApp",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${currentHijriSummary(settings.appLanguage)} • ${settings.locationName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f)
                )
                if (updateState.hasUpdate) {
                    Text(
                        text = if (isEnglish) "Version ${updateState.latestVersionName} is ready." else "Versi ${updateState.latestVersionName} siap dipakai.",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HubCard(if (isEnglish) "Hadith" else "Hadist", Icons.Rounded.HistoryEdu, onOpenHadith)
                HubCard(if (isEnglish) "Calendar" else "Kalender", Icons.Rounded.CalendarMonth, onOpenCalendar)
                HubCard(if (isEnglish) "Ramadan" else "Ramadhan", Icons.Rounded.Mosque, onOpenRamadhan)
                HubCard(if (isEnglish) "Bookmarks" else "Bookmark", Icons.Rounded.Bookmark, onOpenBookmarks)
                HubCard(if (isEnglish) "Settings" else "Pengaturan", Icons.Rounded.Settings, onOpenSettings)
                HubCard(if (isEnglish) "Permissions" else "Akses", Icons.Rounded.NotificationsActive, onOpenPermissionSetup)
            }
        }
    }
}

@Composable
private fun HubCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    SanctuaryCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun HadithLibraryScreen(
    settings: UserSettings,
    spiritualState: SpiritualContentUiState,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    val isEnglish = settings.appLanguage == AppLanguage.ENGLISH
    val categories = spiritualState.hadithCategories.entries.toList()
    val latestItems = categories.flatMap { it.value }.take(6)

    OverlayShell(
        title = if (isEnglish) "Daily Hadith" else "Hadist Harian",
        subtitle = spiritualState.sourceLabel.ifBlank { if (isEnglish) "Spiritual collection" else "Koleksi spiritual" },
        onBack = onBack
    ) {
        spiritualState.hadithOfDay?.let { hadith ->
            HeroCard {
                Text(
                    text = if (isEnglish) "FEATURED HADITH" else "HADITS PILIHAN",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
                Text(
                    text = hadith.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = listOf(hadith.collection, hadith.reference, hadith.narrator)
                        .filter { it.isNotBlank() }
                        .joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                )
            }
        }

        if (categories.isEmpty()) {
            EmptyStateCard(
                title = if (isEnglish) "Hadith is not ready yet" else "Hadist belum siap",
                message = if (isEnglish) "Refresh to try loading the latest collection." else "Segarkan untuk mencoba memuat koleksi terbaru."
            )
            Text(
                text = if (isEnglish) "Refresh content" else "Segarkan konten",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onRefresh)
            )
        } else {
            categories.take(4).forEach { entry ->
                SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Text(text = entry.key, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text(
                        text = "${entry.value.size} ${if (isEnglish) "hadiths" else "hadits"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = if (isEnglish) "LATEST HADITH" else "HADIST TERKINI",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            latestItems.forEachIndexed { index, hadith ->
                HadithCard(index + 1, hadith)
            }
        }
    }
}

@Composable
private fun HadithCard(index: Int, hadith: HadithEntry) {
    SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Text(
            text = "#$index  ${hadith.category.uppercase()}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = hadith.text, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = listOf(hadith.collection, hadith.reference).filter { it.isNotBlank() }.joinToString(" • "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun IslamicCalendarScreen(
    appLanguage: AppLanguage,
    displayMode: CalendarDisplayMode,
    onDisplayModeChange: (CalendarDisplayMode) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val isEnglish = appLanguage.isEnglish()
    var gregorianOffset by rememberSaveable { mutableIntStateOf(0) }
    val initialHijriPage = remember { currentHijriPage() }
    var hijriYear by rememberSaveable { mutableIntStateOf(initialHijriPage.year) }
    var hijriMonthValue by rememberSaveable { mutableIntStateOf(initialHijriPage.monthValue) }
    val hijriPage = remember(hijriYear, hijriMonthValue) { HijriCalendarPage(hijriYear, hijriMonthValue) }
    val gregorianMonth = remember(gregorianOffset) { YearMonth.now().plusMonths(gregorianOffset.toLong()) }
    val cells = remember(displayMode, gregorianMonth, hijriPage) {
        if (displayMode == CalendarDisplayMode.HIJRI) {
            buildHijriCalendarCells(hijriPage)
        } else {
            buildGregorianCalendarCells(gregorianMonth)
        }
    }
    val events = remember(appLanguage) { upcomingIslamicEvents(appLanguage) }
    val monthLabel = if (displayMode == CalendarDisplayMode.HIJRI) {
        hijriRangeLabel(appLanguage, hijriPage)
    } else {
        gregorianMonthLabel(appLanguage, gregorianMonth)
    }

    OverlayShell(
        title = if (displayMode == CalendarDisplayMode.HIJRI) {
            if (isEnglish) "Hijri Calendar" else "Kalender Hijriah"
        } else {
            if (isEnglish) "Gregorian Calendar" else "Kalender Masehi"
        },
        subtitle = monthLabel,
        onBack = onBack
    ) {
        SanctuaryCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChoiceChip(
                    label = if (isEnglish) "Hijri" else "Hijriah",
                    selected = displayMode == CalendarDisplayMode.HIJRI,
                    onClick = { onDisplayModeChange(CalendarDisplayMode.HIJRI) }
                )
                ChoiceChip(
                    label = if (isEnglish) "Gregorian" else "Masehi",
                    selected = displayMode == CalendarDisplayMode.GREGORIAN,
                    onClick = { onDisplayModeChange(CalendarDisplayMode.GREGORIAN) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "<",
                    modifier = Modifier.clickable {
                        if (displayMode == CalendarDisplayMode.HIJRI) {
                            shiftHijriPage(hijriPage, -1).also {
                                hijriYear = it.year
                                hijriMonthValue = it.monthValue
                            }
                        } else {
                            gregorianOffset -= 1
                        }
                    }
                )
                Text(
                    text = monthLabel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    ">",
                    modifier = Modifier.clickable {
                        if (displayMode == CalendarDisplayMode.HIJRI) {
                            shiftHijriPage(hijriPage, 1).also {
                                hijriYear = it.year
                                hijriMonthValue = it.monthValue
                            }
                        } else {
                            gregorianOffset += 1
                        }
                    }
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                hijriWeekdayHeaders(appLanguage).forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            repeat(6) { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    repeat(7) { col ->
                        val cell = cells[row * 7 + col]
                        CalendarCell(cell, appLanguage, displayMode)
                    }
                }
            }
        }

        Text(
            text = if (isEnglish) "UPCOMING EVENTS" else "ACARA MENDATANG",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        events.forEach { event ->
            SanctuaryCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        Text(text = event.hijriLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = if (daysUntil(event.gregorianDate) == 0L) {
                            if (isEnglish) "Today" else "Hari Ini"
                        } else {
                            if (isEnglish) "${daysUntil(event.gregorianDate)} days" else "${daysUntil(event.gregorianDate)} hari"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.CalendarCell(
    cell: com.sajda.app.util.HijriCalendarCell,
    appLanguage: AppLanguage,
    displayMode: CalendarDisplayMode
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    cell.isToday -> MaterialTheme.colorScheme.secondaryContainer
                    cell.isRamadan && cell.isCurrentMonth -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    else -> androidx.compose.ui.graphics.Color.Transparent
                }
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (displayMode == CalendarDisplayMode.HIJRI) {
                cell.hijriDay.toString()
            } else {
                cell.gregorianDay.toString()
            },
            style = MaterialTheme.typography.titleMedium,
            color = if (cell.isCurrentMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = if (displayMode == CalendarDisplayMode.HIJRI) {
                cell.gregorianDay.toString()
            } else {
                cell.hijriDay.toString()
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (cell.isCurrentMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RamadanModeScreen(
    settings: UserSettings,
    prayerTime: PrayerTime?,
    onOpenPrayer: () -> Unit,
    onOpenQuran: () -> Unit,
    onOpenPractices: () -> Unit,
    onOpenRamadanDua: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val isEnglish = settings.appLanguage.isEnglish()
    val today = LocalDate.now()
    val progress = ramadanProgress(today)

    OverlayShell(
        title = if (isEnglish) "Ramadan Mode" else "Mode Ramadhan",
        subtitle = currentHijriSummary(settings.appLanguage),
        onBack = onBack
    ) {
        HeroCard {
            if (progress != null) {
                Text(
                    text = if (isEnglish) "RAMADAN KAREEM" else "RAMADHAN KAREEM",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
                Text(
                    text = if (isEnglish) "Day ${progress.first} of ${progress.second}" else "Hari ke-${progress.first} dari ${progress.second}",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            } else {
                val nextRamadan = nextRamadanStart(today)
                Text(
                    text = if (isEnglish) "RAMADAN COUNTDOWN" else "HITUNG MUNDUR RAMADHAN",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
                Text(
                    text = if (isEnglish) "${daysUntil(nextRamadan)} days left" else "${daysUntil(nextRamadan)} hari lagi",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SanctuaryCard(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Text(text = if (isEnglish) "Imsak" else "Imsak", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = prayerTime?.fajr ?: "--:--", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            }
            SanctuaryCard(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.34f)
            ) {
                Text(text = if (isEnglish) "Iftar" else "Iftar", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = prayerTime?.maghrib ?: "--:--", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            }
        }

        SanctuaryCard(
            modifier = Modifier.clickable(onClick = onOpenPrayer),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            RamadanActionRow(
                icon = Icons.Rounded.CalendarMonth,
                title = if (isEnglish) "Suhur and iftar schedule" else "Jadwal sahur dan buka",
                subtitle = if (isEnglish) "Open today's prayer times" else "Buka jadwal sholat hari ini"
            )
        }

        SanctuaryCard(
            modifier = Modifier.clickable(onClick = onOpenQuran),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            RamadanActionRow(
                icon = Icons.Rounded.MenuBook,
                title = if (isEnglish) "Khatam Qur'an" else "Khatam Qur'an",
                subtitle = if (isEnglish) "Continue your tilawah plan" else "Lanjutkan target tilawah harian"
            )
        }

        SanctuaryCard(
            modifier = Modifier.clickable(onClick = onOpenPractices),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            RamadanActionRow(
                icon = Icons.Rounded.Mosque,
                title = if (isEnglish) "Ramadan practices" else "Amalan Ramadhan",
                subtitle = if (isEnglish) "Daily worship checklist" else "Panduan amalan harian Ramadhan"
            )
        }

        SanctuaryCard(
            modifier = Modifier.clickable(onClick = onOpenRamadanDua),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            RamadanActionRow(
                icon = Icons.Rounded.Bookmark,
                title = if (isEnglish) "Iftar and suhur duas" else "Doa berbuka dan sahur",
                subtitle = if (isEnglish) "Read and save important duas" else "Baca doa penting untuk sahur dan berbuka"
            )
        }
    }
}

@Composable
private fun RamadanActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RamadanPracticesScreen(
    appLanguage: AppLanguage,
    onBack: () -> Unit
) {
    OverlayShell(
        title = appLanguage.pick("Amalan Ramadhan", "Ramadan Practices"),
        subtitle = appLanguage.pick("Panduan singkat ibadah harian", "A concise daily worship guide"),
        onBack = onBack
    ) {
        RAMADAN_PRACTICES.forEachIndexed { index, practice ->
            SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                Text(
                    text = "%02d".format(index + 1),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = appLanguage.pick(practice.titleId, practice.titleEn),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = appLanguage.pick(practice.descriptionId, practice.descriptionEn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RamadanDuaScreen(
    appLanguage: AppLanguage,
    onBack: () -> Unit
) {
    OverlayShell(
        title = appLanguage.pick("Doa Berbuka & Sahur", "Iftar and Suhur Duas"),
        subtitle = appLanguage.pick("Doa yang sering dipakai saat Ramadhan", "Common duas used in Ramadan"),
        onBack = onBack
    ) {
        RAMADAN_DUAS.forEach { dua ->
            SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                Text(
                    text = appLanguage.pick(dua.titleId, dua.titleEn),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = dua.arabic,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                if (dua.transliteration.isNotBlank()) {
                    Text(
                        text = dua.transliteration,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = appLanguage.pick(dua.translationId, dua.translationEn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private data class RamadanPracticeContent(
    val titleId: String,
    val titleEn: String,
    val descriptionId: String,
    val descriptionEn: String
)

private data class RamadanDuaContent(
    val titleId: String,
    val titleEn: String,
    val arabic: String,
    val transliteration: String,
    val translationId: String,
    val translationEn: String
)

private val RAMADAN_PRACTICES = listOf(
    RamadanPracticeContent(
        titleId = "Niat dan sahur tepat waktu",
        titleEn = "Renew intention and take suhur on time",
        descriptionId = "Awali malam dengan niat puasa, lalu usahakan sahur mendekati waktu Subuh agar tubuh tetap kuat.",
        descriptionEn = "Begin the night with the intention to fast, then take suhur close to Fajr so your body stays strong."
    ),
    RamadanPracticeContent(
        titleId = "Jaga sholat fardhu berjamaah",
        titleEn = "Protect the five daily prayers",
        descriptionId = "Jadikan setiap sholat wajib sebagai poros aktivitas Ramadhan, lalu tambah witir dan tarawih semampunya.",
        descriptionEn = "Make each obligatory prayer the anchor of your Ramadan, then add tarawih and witr as much as you can."
    ),
    RamadanPracticeContent(
        titleId = "Tilawah harian yang realistis",
        titleEn = "Keep a realistic daily tilawah plan",
        descriptionId = "Bagi target bacaan menjadi beberapa sesi pendek setelah sholat agar lebih mudah konsisten sampai akhir bulan.",
        descriptionEn = "Split your recitation target into shorter sessions after prayers so it stays consistent through the month."
    ),
    RamadanPracticeContent(
        titleId = "Perbanyak sedekah dan doa",
        titleEn = "Increase charity and dua",
        descriptionId = "Gunakan waktu menjelang berbuka untuk berdoa, dan sisihkan sedekah rutin walau nilainya kecil.",
        descriptionEn = "Use the minutes before iftar for dua, and keep a steady charity habit even if the amount is small."
    ),
    RamadanPracticeContent(
        titleId = "Qiyam dan evaluasi harian",
        titleEn = "Night prayer and daily reflection",
        descriptionId = "Sempatkan qiyamul lail walau singkat, lalu evaluasi ibadah harian supaya Ramadhan tidak lewat tanpa arah.",
        descriptionEn = "Set aside some time for night prayer, then review each day so Ramadan does not pass without direction."
    )
)

private val RAMADAN_DUAS = listOf(
    RamadanDuaContent(
        titleId = "Doa berbuka puasa",
        titleEn = "Dua for breaking the fast",
        arabic = "اللَّهُمَّ إِنِّي لَكَ صُمْتُ وَبِكَ آمَنْتُ وَعَلَيْكَ تَوَكَّلْتُ وَعَلَى رِزْقِكَ أَفْطَرْتُ",
        transliteration = "Allahumma inni laka sumtu wa bika amantu wa 'alayka tawakkaltu wa 'ala rizqika aftartu",
        translationId = "Ya Allah, aku berpuasa untuk-Mu, aku beriman kepada-Mu, aku bertawakal kepada-Mu, dan dengan rezeki-Mu aku berbuka.",
        translationEn = "O Allah, I fasted for You, I believed in You, I relied upon You, and with Your provision I break my fast."
    ),
    RamadanDuaContent(
        titleId = "Dzikir setelah berbuka",
        titleEn = "Dhikr after iftar",
        arabic = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ",
        transliteration = "Dhahaba az-zama'u wabtallatil-'uruqu wa thabatal-ajru in sha' Allah",
        translationId = "Telah hilang dahaga, urat-urat telah basah, dan pahala telah tetap insya Allah.",
        translationEn = "The thirst is gone, the veins are moistened, and the reward is confirmed, if Allah wills."
    ),
    RamadanDuaContent(
        titleId = "Doa sebelum sahur",
        titleEn = "Dua before suhur",
        arabic = "وَبِصَوْمِ غَدٍ نَوَيْتُ مِنْ شَهْرِ رَمَضَانَ",
        transliteration = "Wa bisawmi ghadin nawaitu min shahri Ramadan",
        translationId = "Aku berniat puasa esok hari untuk menunaikan kewajiban Ramadhan.",
        translationEn = "I intend to fast tomorrow in fulfillment of the Ramadan obligation."
    )
)
