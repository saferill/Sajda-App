package com.sajda.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.ui.component.HeroCard
import com.sajda.app.ui.component.MetadataChip
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.theme.surfaceContainerLow
import com.sajda.app.ui.viewmodel.PrayerTimeViewModel
import com.sajda.app.util.DateTimeUtils
import com.sajda.app.util.currentDayName
import com.sajda.app.util.currentGregorianSummary
import com.sajda.app.util.currentHijriSummary
import com.sajda.app.util.displayName
import com.sajda.app.util.localizedPrayerName
import java.time.LocalDate

@Composable
fun PrayerTimeScreen(
    viewModel: PrayerTimeViewModel,
    onOpenWeeklySchedule: () -> Unit,
    onOpenQibla: () -> Unit,
    onOpenLocationSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = state.settings
    val isEnglish = settings.appLanguage == AppLanguage.ENGLISH
    val today = LocalDate.now()
    val prayerTime = state.todayPrayerTime
    val nextPrayer = prayerTime?.let(DateTimeUtils::nextPrayer)
    val countdown = prayerTime?.let(DateTimeUtils::countdownClockToNextPrayer) ?: "--:--:--"
    val detailed = prayerTime?.let {
        com.sajda.app.util.PrayerTimeCalculator.calculateDetailedPrayerTimes(
            date = LocalDate.parse(it.date),
            latitude = it.latitude,
            longitude = it.longitude,
            locationName = it.locationName,
            calculationMethod = settings.prayerCalculationMethod,
            asrMadhhab = settings.asrMadhhab
        )
    }

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
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "NurApp",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = settings.locationName.ifBlank { "Jakarta, Indonesia" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${currentHijriSummary(settings.appLanguage, today)}  •  ${currentDayName(settings.appLanguage, today)}, ${currentGregorianSummary(settings.appLanguage, today)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (isEnglish) "Change" else "Ubah Lokasi",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.28f))
                        .clickable(onClick = onOpenLocationSettings)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }

        item {
            HeroCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEnglish) "NEXT PRAYER" else "WAKTU SHOLAT BERIKUTNYA",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.80f)
                        )
                        Text(
                            text = nextPrayer?.first?.displayName(settings.appLanguage)
                                ?: localizedPrayerName(PrayerName.ASR.label, settings.appLanguage),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = if (isEnglish) "In $countdown" else "Dalam $countdown",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = nextPrayer?.second ?: "--:--",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "WIB",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                        )
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.weeklyPrayerTimes.take(5), key = { it.date }) { item ->
                    val localDate = LocalDate.parse(item.date)
                    val active = item.date == prayerTime?.date
                    Column(
                        modifier = Modifier
                            .width(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (active) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentDayName(settings.appLanguage, localDate).take(3).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = localDate.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        item {
            SanctuaryCard {
                detailed?.let {
                    PrayerRow(
                        label = if (isEnglish) "Imsak" else "Imsak",
                        value = it.imsak,
                        enabled = false,
                        onToggle = null
                    )
                }
                PrayerRow(
                    localizedPrayerName(PrayerName.FAJR.label, settings.appLanguage),
                    prayerTime?.fajr ?: "--:--",
                    settings.fajrAdzanEnabled
                ) { viewModel.togglePrayer(PrayerName.FAJR, it) }
                detailed?.let {
                    PrayerRow(
                        label = if (isEnglish) "Sunrise" else "Terbit",
                        value = it.sunrise,
                        enabled = false,
                        onToggle = null
                    )
                }
                PrayerRow(
                    localizedPrayerName(PrayerName.DHUHR.label, settings.appLanguage),
                    prayerTime?.dhuhr ?: "--:--",
                    settings.dhuhrAdzanEnabled
                ) { viewModel.togglePrayer(PrayerName.DHUHR, it) }
                PrayerRow(
                    localizedPrayerName(PrayerName.ASR.label, settings.appLanguage),
                    prayerTime?.asr ?: "--:--",
                    settings.asrAdzanEnabled,
                    highlighted = true
                ) { viewModel.togglePrayer(PrayerName.ASR, it) }
                PrayerRow(
                    localizedPrayerName(PrayerName.MAGHRIB.label, settings.appLanguage),
                    prayerTime?.maghrib ?: "--:--",
                    settings.maghribAdzanEnabled
                ) { viewModel.togglePrayer(PrayerName.MAGHRIB, it) }
                PrayerRow(
                    localizedPrayerName(PrayerName.ISHA.label, settings.appLanguage),
                    prayerTime?.isha ?: "--:--",
                    settings.ishaAdzanEnabled
                ) { viewModel.togglePrayer(PrayerName.ISHA, it) }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetadataChip(text = "${settings.prayerCalculationMethod.label} | ${settings.asrMadhhab.label}", active = true)
                Text(
                    text = if (isEnglish) "Qibla" else "Kiblat",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .clickable(onClick = onOpenQibla)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
                Text(
                    text = if (isEnglish) "Weekly" else "Mingguan",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .clickable(onClick = onOpenWeeklySchedule)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun PrayerRow(
    label: String,
    value: String,
    enabled: Boolean,
    highlighted: Boolean = false,
    onToggle: ((Boolean) -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(if (highlighted) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f) else Color.Transparent)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (highlighted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = if (onToggle != null) Icons.Rounded.NotificationsActive else Icons.Rounded.Mosque,
                    contentDescription = null,
                    tint = if (highlighted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(text = label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            if (onToggle != null) {
                Switch(checked = enabled, onCheckedChange = onToggle)
            }
        }
    }
}
