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
import com.sajda.app.util.buildHijriCalendarCells
import com.sajda.app.util.currentHijriSummary
import com.sajda.app.util.daysUntil
import com.sajda.app.util.hijriRangeLabel
import com.sajda.app.util.hijriWeekdayHeaders
import com.sajda.app.util.isEnglish
import com.sajda.app.util.nextRamadanStart
import com.sajda.app.util.ramadanProgress
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
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }
    val month = remember(monthOffset) { YearMonth.now().plusMonths(monthOffset.toLong()) }
    val cells = remember(month) { buildHijriCalendarCells(month) }
    val events = remember(month, appLanguage) { upcomingIslamicEvents(appLanguage) }

    OverlayShell(
        title = if (displayMode == CalendarDisplayMode.HIJRI) {
            if (isEnglish) "Hijri Calendar" else "Kalender Hijriah"
        } else {
            if (isEnglish) "Gregorian Calendar" else "Kalender Masehi"
        },
        subtitle = hijriRangeLabel(appLanguage, month),
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
                Text("<", modifier = Modifier.clickable { monthOffset -= 1 })
                Text(
                    text = "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(">", modifier = Modifier.clickable { monthOffset += 1 })
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

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HubCard(if (isEnglish) "Prayer Schedule" else "Jadwal Sahur & Buka", Icons.Rounded.CalendarMonth, onOpenPrayer)
            HubCard(if (isEnglish) "Khatam Qur'an" else "Khatam Qur'an", Icons.Rounded.Bookmark, onOpenQuran)
            HubCard(if (isEnglish) "Ramadan Deeds" else "Amalan Ramadhan", Icons.Rounded.Mosque, {})
            HubCard(if (isEnglish) "Dua & Suhoor" else "Doa Berbuka & Sahur", Icons.Rounded.HistoryEdu, {})
        }

        SanctuaryCard(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)) {
            Text(
                text = "\"Whoever fasts Ramadan out of faith and hope for reward will be forgiven.\"",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Sahih Bukhari",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
