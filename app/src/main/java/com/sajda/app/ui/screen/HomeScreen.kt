package com.sajda.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajda.app.domain.model.Ayat
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.PrayerTime
import com.sajda.app.domain.model.Surah
import com.sajda.app.ui.component.ArabicVerseText
import com.sajda.app.ui.component.HeroCard
import com.sajda.app.ui.component.SajdaLogoTile
import com.sajda.app.ui.component.SajdaTopAction
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.component.SectionHeader
import com.sajda.app.ui.component.ShortcutTile
import com.sajda.app.ui.theme.surfaceContainerHigh
import com.sajda.app.ui.theme.surfaceContainerLow
import com.sajda.app.ui.viewmodel.HomeViewModel
import com.sajda.app.util.DateTimeUtils
import com.sajda.app.util.localizedPrayerName

private data class HomeShortcut(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val action: () -> Unit
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToQuran: () -> Unit,
    onNavigateToPrayer: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenAudioManager: () -> Unit,
    onOpenDua: () -> Unit,
    onOpenQibla: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenProgress: () -> Unit,
    onPlayLastAudio: (Surah) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isEnglish = state.appLanguage == AppLanguage.ENGLISH

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val shortcuts = listOf(
        HomeShortcut(if (isEnglish) "Qibla" else "Qibla", Icons.Rounded.Explore, onOpenQibla),
        HomeShortcut(if (isEnglish) "Schedule" else "Jadwal", Icons.Rounded.Schedule, onNavigateToPrayer),
        HomeShortcut(if (isEnglish) "Audio" else "Audio", Icons.Rounded.Headphones, onOpenAudioManager),
        HomeShortcut(if (isEnglish) "Dua" else "Doa", Icons.Rounded.AutoAwesome, onOpenDua)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 172.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            HomeHeader(
                appLanguage = state.appLanguage,
                locationName = state.locationName.ifBlank { "Jakarta" },
                onOpenSearch = onOpenSearch,
                onOpenReminders = onOpenReminders
            )
        }

        item {
            PrayerHeroCard(
                appLanguage = state.appLanguage,
                prayerTime = state.todayPrayerTime,
                nextPrayerLabel = state.nextPrayerLabel,
                nextPrayerTime = state.nextPrayerTime,
                countdown = state.countdown,
                onOpenPrayer = onNavigateToPrayer
            )
        }

        item {
            SpiritualJourneyCard(
                appLanguage = state.appLanguage,
                ayatRead = state.dailyAyatRead,
                streakCount = state.streakCount,
                onOpenProgress = onOpenProgress
            )
        }

        item {
            LastReadCard(
                appLanguage = state.appLanguage,
                surah = state.lastReadSurah,
                ayat = state.lastReadAyat,
                onContinueReading = onNavigateToQuran,
                onOpenBookmarks = onOpenBookmarks
            )
        }

        item {
            QuickAudioCard(
                appLanguage = state.appLanguage,
                surah = state.quickPlaySurah,
                onPlay = onPlayLastAudio,
                onOpenAudioManager = onOpenAudioManager
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                shortcuts.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { shortcut ->
                            ShortcutTile(
                                icon = shortcut.icon,
                                label = shortcut.label,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(138.dp),
                                onClick = shortcut.action
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item {
            DailyAyatCard(appLanguage = state.appLanguage, ayat = state.dailyAyat)
        }
    }
}

@Composable
private fun HomeHeader(
    appLanguage: AppLanguage,
    locationName: String,
    onOpenSearch: () -> Unit,
    onOpenReminders: () -> Unit
) {
    val isEnglish = appLanguage == AppLanguage.ENGLISH
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SajdaLogoTile(size = 42)
            Column(
                modifier = Modifier.padding(start = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Sajda App",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = locationName.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SajdaTopAction(icon = Icons.Rounded.Search, label = if (isEnglish) "Search" else "Cari", onClick = onOpenSearch)
            SajdaTopAction(icon = Icons.Rounded.Notifications, label = if (isEnglish) "Reminders" else "Reminder", onClick = onOpenReminders)
        }
    }
}

@Composable
private fun PrayerHeroCard(
    appLanguage: AppLanguage,
    prayerTime: PrayerTime?,
    nextPrayerLabel: String,
    nextPrayerTime: String,
    countdown: String,
    onOpenPrayer: () -> Unit
) {
    val isEnglish = appLanguage == AppLanguage.ENGLISH
    HeroCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isEnglish) "NEXT PRAYER" else "SHOLAT BERIKUTNYA",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
                )
                Text(
                    text = localizedPrayerName(nextPrayerLabel, appLanguage),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = nextPrayerTime,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isEnglish) "REMAINING" else "SISA WAKTU",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
                )
                Text(
                    text = countdown,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = if (isEnglish) "Schedule" else "Jadwal",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.10f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clickable(onClick = onOpenPrayer)
                )
            }
        }

        if (prayerTime != null) {
            val prayerEntries: List<Pair<com.sajda.app.domain.model.PrayerName, String>> =
                DateTimeUtils.prayerEntries(prayerTime)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                prayerEntries.forEach { entry ->
                    val prayer = entry.first
                    val time = entry.second
                    val isNext = prayer.label == nextPrayerLabel
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                MaterialTheme.colorScheme.onPrimary.copy(
                                    alpha = if (isNext) 0.18f else 0.10f
                                )
                            )
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = localizedPrayerName(prayer.label, appLanguage).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
                        )
                        Text(
                            text = time,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpiritualJourneyCard(
    appLanguage: AppLanguage,
    ayatRead: Int,
    streakCount: Int,
    onOpenProgress: () -> Unit
) {
    val isEnglish = appLanguage == AppLanguage.ENGLISH
    val dailyGoal = 20
    val progress = (ayatRead / dailyGoal.toFloat()).coerceIn(0f, 1f)
    val level = (streakCount / 7) + 1

    SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isEnglish) "$streakCount Day Streak" else "$streakCount Hari Beruntun",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = if (isEnglish) "LEVEL $level" else "LEVEL $level",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = MaterialTheme.colorScheme.primaryContainer,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )

        Text(
            text = if (isEnglish) {
                "$ayatRead verses today out of the $dailyGoal verse goal."
            } else {
                "$ayatRead ayat hari ini dari target $dailyGoal ayat."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = if (isEnglish) "View full progress" else "Lihat progres lengkap",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onOpenProgress)
        )
    }
}

@Composable
private fun LastReadCard(
    appLanguage: AppLanguage,
    surah: Surah?,
    ayat: Ayat?,
    onContinueReading: () -> Unit,
    onOpenBookmarks: () -> Unit
) {
    val isEnglish = appLanguage == AppLanguage.ENGLISH
    SanctuaryCard {
        SectionHeader(
            eyebrow = if (isEnglish) "Last Read" else "Terakhir Dibaca",
            title = surah?.transliteration ?: if (isEnglish) "Start your Qur'an journey" else "Mulai perjalanan Qur'an",
            actionLabel = if (isEnglish) "Bookmarks" else "Bookmark",
            onAction = onOpenBookmarks
        )

        if (surah != null && ayat != null) {
            Text(
                text = if (isEnglish) "Verse ${ayat.ayatNumber}" else "Ayat ${ayat.ayatNumber}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isEnglish) ayat.englishTranslation.ifBlank { ayat.translation } else ayat.translation,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = if (isEnglish) "Resume reading" else "Lanjutkan membaca",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onContinueReading)
                )
            }
        } else {
            Text(
                text = if (isEnglish) "Open the Qur'an tab to start reading." else "Buka tab Qur'an untuk mulai membaca.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickAudioCard(
    appLanguage: AppLanguage,
    surah: Surah?,
    onPlay: (Surah) -> Unit,
    onOpenAudioManager: () -> Unit
) {
    val isEnglish = appLanguage == AppLanguage.ENGLISH
    SanctuaryCard {
        SectionHeader(
            eyebrow = if (isEnglish) "Quick Audio" else "Audio Cepat",
            title = surah?.transliteration ?: if (isEnglish) "Offline Murattal" else "Murattal Offline",
            actionLabel = if (isEnglish) "Manage" else "Kelola",
            onAction = onOpenAudioManager
        )

        if (surah?.localAudioPath != null) {
            Text(
                text = if (isEnglish) "The latest audio is ready to play offline." else "Audio terakhir siap diputar offline.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isEnglish) "Play now" else "Putar sekarang",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onPlay(surah) }
            )
        } else {
            Text(
                text = if (isEnglish) "No offline audio has been downloaded yet." else "Belum ada audio offline yang diunduh.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DailyAyatCard(appLanguage: AppLanguage, ayat: Ayat?) {
    val isEnglish = appLanguage == AppLanguage.ENGLISH
    SanctuaryCard {
        SectionHeader(
            eyebrow = if (isEnglish) "Daily Verse" else "Ayat Harian",
            title = if (isEnglish) "Today's verse" else "Ayat hari ini"
        )
        if (ayat != null) {
            ArabicVerseText(text = ayat.textArabic)
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "\"${if (isEnglish) ayat.englishTranslation.ifBlank { ayat.translation } else ayat.translation}\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isEnglish) "Surah ${ayat.surahNumber}:${ayat.ayatNumber}" else "Surah ${ayat.surahNumber}:${ayat.ayatNumber}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start
                )
            }
        } else {
            Text(
                text = if (isEnglish) {
                    "The daily verse will appear after the Qur'an data finishes loading."
                } else {
                    "Ayat harian akan tampil setelah data Qur'an selesai dimuat."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
