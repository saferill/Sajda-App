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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.Surah
import com.sajda.app.ui.component.ArabicVerseText
import com.sajda.app.ui.component.HeroCard
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.theme.surfaceContainerLow
import com.sajda.app.ui.viewmodel.HomeViewModel
import com.sajda.app.util.DateTimeUtils
import com.sajda.app.util.currentGregorianSummary
import com.sajda.app.util.currentHijriSummary
import com.sajda.app.util.localizedPrayerName
import java.time.LocalDate

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToQuran: () -> Unit,
    onNavigateToPrayer: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHadith: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenRamadan: () -> Unit,
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

    val today = LocalDate.now()
    val prayerEntries = state.todayPrayerTime?.let(DateTimeUtils::prayerEntries).orEmpty()
    val nextPrayerLabel = localizedPrayerName(state.nextPrayerLabel, state.appLanguage)
    val dailyAyat = state.dailyAyat

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 150.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "NURAPP",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isEnglish) "Assalamu'alaikum" else "Assalamu'alaikum",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${currentHijriSummary(state.appLanguage, today)}  •  ${currentGregorianSummary(state.appLanguage, today)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            HeroCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isEnglish) "NOW" else "SEKARANG",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Text(
                            text = nextPrayerLabel,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = state.countdown,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = state.locationName.ifBlank { "Jakarta, Indonesia" }.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Text(
                            text = if (isEnglish) "Next: $nextPrayerLabel ${state.nextPrayerTime}" else "Berikutnya: $nextPrayerLabel ${state.nextPrayerTime}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        item {
            SectionTitle(
                title = if (isEnglish) "Prayer Schedule" else "Jadwal Sholat",
                action = if (isEnglish) "Full Schedule" else "Selengkapnya",
                onAction = onNavigateToPrayer
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(prayerEntries, key = { it.first.name }) { entry ->
                    val active = entry.first.label == state.nextPrayerLabel
                    Column(
                        modifier = Modifier
                            .width(104.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(horizontal = 14.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = localizedPrayerName(entry.first.label, state.appLanguage).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (active) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = entry.second,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            FeatureRows(
                isEnglish = isEnglish,
                onNavigateToQuran = onNavigateToQuran,
                onOpenQibla = onOpenQibla,
                onOpenHadith = onOpenHadith,
                onOpenCalendar = onOpenCalendar,
                onOpenRamadan = onOpenRamadan,
                onOpenBookmarks = onOpenBookmarks,
                onNavigateToPrayer = onNavigateToPrayer
            )
        }

        item {
            SanctuaryCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = if (isEnglish) "LAST READ" else "TERAKHIR DIBACA",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = state.lastReadSurah?.let { "${it.transliteration}: ${state.lastReadAyat?.ayatNumber ?: 1}" }
                                    ?: if (isEnglish) "Start reading" else "Mulai membaca",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Text(
                        text = if (isEnglish) "Continue" else "Lanjutkan",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(onClick = onNavigateToQuran)
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }
        }

        item {
            HeroCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isEnglish) "VERSE OF THE DAY" else "AYAT HARI INI",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (dailyAyat != null) {
                        ArabicVerseText(
                            text = dailyAyat.textArabic,
                            textAlign = TextAlign.Center,
                            fontSize = 28
                        )
                        Text(
                            text = "\"${if (isEnglish) dailyAyat.englishTranslation.ifBlank { dailyAyat.translation } else dailyAyat.translation}\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = if (isEnglish) "Daily verse is loading." else "Ayat harian sedang dimuat.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    action: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text(
            text = action.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onAction)
        )
    }
}

@Composable
private fun FeatureRows(
    isEnglish: Boolean,
    onNavigateToQuran: () -> Unit,
    onOpenQibla: () -> Unit,
    onOpenHadith: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenRamadan: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onNavigateToPrayer: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            FeatureCard("Al-Qur'an", Icons.Rounded.MenuBook, Modifier.weight(1f), onNavigateToQuran)
            FeatureCard(if (isEnglish) "Qibla" else "Kiblat", Icons.Rounded.Explore, Modifier.weight(1f), onOpenQibla)
            FeatureCard(if (isEnglish) "Hadith" else "Hadist", Icons.Rounded.HistoryEdu, Modifier.weight(1f), onOpenHadith)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            FeatureCard(if (isEnglish) "Hijri" else "Hijriah", Icons.Rounded.CalendarMonth, Modifier.weight(1f), onOpenCalendar)
            FeatureCard(if (isEnglish) "Bookmarks" else "Bookmark", Icons.Rounded.Bookmark, Modifier.weight(1f), onOpenBookmarks)
            FeatureCard(if (isEnglish) "Ramadan" else "Ramadhan", Icons.Rounded.Mosque, Modifier.weight(1f), onOpenRamadan)
            FeatureCard(if (isEnglish) "Adhan" else "Adzan", Icons.Rounded.NotificationsActive, Modifier.weight(1f), onNavigateToPrayer)
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    SanctuaryCard(
        modifier = modifier.clickable(onClick = onClick),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}
