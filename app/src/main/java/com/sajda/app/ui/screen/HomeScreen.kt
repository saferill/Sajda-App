package com.sajda.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Headphones
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
import com.sajda.app.domain.model.Surah
import com.sajda.app.ui.component.ArabicVerseText
import com.sajda.app.ui.component.HeroCard
import com.sajda.app.ui.component.MetadataChip
import com.sajda.app.ui.component.SajdaLogoTile
import com.sajda.app.ui.component.SajdaTopAction
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.component.SectionHeader
import com.sajda.app.ui.theme.surfaceContainerLow
import com.sajda.app.ui.viewmodel.HomeViewModel
import com.sajda.app.util.DateTimeUtils
import com.sajda.app.util.currentGregorianSummary
import com.sajda.app.util.currentHijriSummary
import com.sajda.app.util.isEnglish
import com.sajda.app.util.localizedPrayerName
import com.sajda.app.util.pick
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
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
    val isEnglish = state.appLanguage.isEnglish()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val today = LocalDate.now()
    val prayerEntries = state.todayPrayerTime?.let(DateTimeUtils::prayerEntries).orEmpty()
    val nextPrayerLabel = localizedPrayerName(state.nextPrayerLabel, state.appLanguage)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 150.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SajdaLogoTile()
                    androidx.compose.foundation.layout.Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isEnglish) "Assalamu'alaikum" else "Assalamu'alaikum",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = state.locationName.ifBlank { "Jakarta, Indonesia" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SajdaTopAction(
                        icon = Icons.Rounded.CalendarMonth,
                        label = if (isEnglish) "Calendar" else "Kalender",
                        onClick = onOpenCalendar
                    )
                    SajdaTopAction(
                        icon = Icons.Rounded.Explore,
                        label = if (isEnglish) "Qibla" else "Kiblat",
                        onClick = onOpenQibla
                    )
                }
            }
        }

        item {
            HeroCard {
                Text(
                    text = "${currentHijriSummary(state.appLanguage, today)}  •  ${currentGregorianSummary(state.appLanguage, today)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    androidx.compose.foundation.layout.Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isEnglish) "NEXT ADHAN" else "ADZAN BERIKUTNYA",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        )
                        Text(
                            text = nextPrayerLabel,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isEnglish) {
                                "In ${state.countdown} • ${state.nextPrayerTime}"
                            } else {
                                "Dalam ${state.countdown} • ${state.nextPrayerTime}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionPill(
                        label = if (isEnglish) "Prayer Schedule" else "Jadwal Adzan",
                        onClick = onNavigateToPrayer
                    )
                    ActionPill(
                        label = if (isEnglish) "Hadith" else "Hadist",
                        onClick = onOpenHadith
                    )
                }
            }
        }

        item {
            SectionHeader(
                eyebrow = if (isEnglish) "Quick Access" else "Akses Cepat",
                title = if (isEnglish) "Open what you need" else "Buka yang paling sering dipakai"
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAccessCard(
                        label = "Qur'an",
                        icon = Icons.Rounded.MenuBook,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToQuran
                    )
                    QuickAccessCard(
                        label = if (isEnglish) "Adhan" else "Adzan",
                        icon = Icons.Rounded.NotificationsActive,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToPrayer
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAccessCard(
                        label = if (isEnglish) "Hadith" else "Hadist",
                        icon = Icons.Rounded.HistoryEdu,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenHadith
                    )
                    QuickAccessCard(
                        label = if (isEnglish) "Ramadan" else "Ramadhan",
                        icon = Icons.Rounded.Mosque,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenRamadan
                    )
                }
            }
        }

        item {
            SectionHeader(
                eyebrow = if (isEnglish) "Today" else "Hari Ini",
                title = if (isEnglish) "Prayer timetable" else "Jadwal sholat",
                actionLabel = if (isEnglish) "Open Adhan" else "Buka Adzan",
                onAction = onNavigateToPrayer
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                prayerEntries.forEach { entry ->
                    MetadataChip(
                        text = "${localizedPrayerName(entry.first.label, state.appLanguage)} ${entry.second}",
                        active = entry.first.label == state.nextPrayerLabel
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SanctuaryCard(
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Text(
                        text = if (isEnglish) "LAST READ" else "TERAKHIR DIBACA",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.lastReadSurah?.let { "${it.transliteration}:${state.lastReadAyat?.ayatNumber ?: 1}" }
                            ?: if (isEnglish) "No reading history" else "Belum ada riwayat baca",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (isEnglish) "Continue reading" else "Lanjutkan baca",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onNavigateToQuran)
                    )
                }

                SanctuaryCard(
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Text(
                        text = if (isEnglish) "MURATTAL" else "MURATTAL",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.quickPlaySurah?.transliteration
                            ?: if (isEnglish) "No downloaded audio" else "Belum ada audio offline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (state.quickPlaySurah != null) {
                            if (isEnglish) "Play now" else "Putar sekarang"
                        } else {
                            if (isEnglish) "Open Qur'an library" else "Buka pustaka Qur'an"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            state.quickPlaySurah?.let(onPlayLastAudio) ?: onNavigateToQuran()
                        }
                    )
                }
            }
        }

        item {
            HeroCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnglish) "VERSE OF THE DAY" else "AYAT HARI INI",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                    Text(
                        text = if (isEnglish) "${state.dailyAyatRead} verses read" else "${state.dailyAyatRead} ayat dibaca",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
                    )
                }

                state.dailyAyat?.let { ayat ->
                    ArabicVerseText(
                        text = ayat.textArabic,
                        fontSize = 28,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "\"${state.appLanguage.pick(ayat.translation, ayat.englishTranslation.ifBlank { ayat.translation })}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f),
                        textAlign = TextAlign.Center
                    )
                } ?: Text(
                    text = if (isEnglish) "Daily verse is loading." else "Ayat harian sedang dimuat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        item {
            SanctuaryCard(
                modifier = Modifier.clickable(onClick = onOpenBookmarks)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        androidx.compose.foundation.layout.Column {
                            Text(
                                text = if (isEnglish) "Bookmarks & Notes" else "Bookmark & Catatan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isEnglish) "Open saved ayat, folders, and reflections" else "Buka ayat tersimpan, folder, dan refleksi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = if (isEnglish) "Open" else "Buka",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionPill(
    label: String,
    onClick: () -> Unit
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    )
}

@Composable
private fun QuickAccessCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SanctuaryCard(
        modifier = modifier.clickable(onClick = onClick),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
