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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.PrayerTime
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.ui.component.HeroCard
import com.sajda.app.ui.component.MetadataChip
import com.sajda.app.ui.component.SajdaTopAction
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.component.SectionHeader
import com.sajda.app.ui.viewmodel.PrayerTimeViewModel
import com.sajda.app.util.DateTimeUtils
import com.sajda.app.util.PrayerTimeCalculator
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
    val isEnglish = state.settings.appLanguage == AppLanguage.ENGLISH

    if (state.isRefreshing && state.weeklyPrayerTimes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 180.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isEnglish) "Prayer Times" else "Waktu Sholat",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = state.settings.locationName.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SajdaTopAction(Icons.Rounded.LocationOn, if (isEnglish) "Location" else "Lokasi", onOpenLocationSettings)
                    SajdaTopAction(Icons.Rounded.Explore, "Qibla", onOpenQibla)
                }
            }
        }

        item {
            DailyPrayerHero(
                prayerTime = state.todayPrayerTime,
                settings = state.settings,
                adzanEnabled = state.settings.adzanEnabled,
                onToggleAdzan = viewModel::toggleAdzan,
                onOpenWeeklySchedule = onOpenWeeklySchedule
            )
        }

        item {
            SectionHeader(
                eyebrow = if (isEnglish) "Prayer Controls" else "Kontrol Sholat",
                title = if (isEnglish) "Adhan by prayer" else "Adzan per waktu",
                actionLabel = if (isEnglish) "Weekly" else "Mingguan",
                onAction = onOpenWeeklySchedule
            )
        }

        item {
            PrayerToggleCard(
                title = PrayerName.FAJR.displayName(state.settings.appLanguage),
                subtitle = state.todayPrayerTime?.fajr ?: "--:--",
                checked = state.settings.fajrAdzanEnabled,
                onCheckedChange = { viewModel.togglePrayer(PrayerName.FAJR, it) }
            )
        }

        item {
            PrayerToggleCard(
                title = PrayerName.DHUHR.displayName(state.settings.appLanguage),
                subtitle = state.todayPrayerTime?.dhuhr ?: "--:--",
                checked = state.settings.dhuhrAdzanEnabled,
                onCheckedChange = { viewModel.togglePrayer(PrayerName.DHUHR, it) }
            )
        }

        item {
            PrayerToggleCard(
                title = PrayerName.ASR.displayName(state.settings.appLanguage),
                subtitle = state.todayPrayerTime?.asr ?: "--:--",
                checked = state.settings.asrAdzanEnabled,
                onCheckedChange = { viewModel.togglePrayer(PrayerName.ASR, it) }
            )
        }

        item {
            PrayerToggleCard(
                title = PrayerName.MAGHRIB.displayName(state.settings.appLanguage),
                subtitle = state.todayPrayerTime?.maghrib ?: "--:--",
                checked = state.settings.maghribAdzanEnabled,
                onCheckedChange = { viewModel.togglePrayer(PrayerName.MAGHRIB, it) }
            )
        }

        item {
            PrayerToggleCard(
                title = PrayerName.ISHA.displayName(state.settings.appLanguage),
                subtitle = state.todayPrayerTime?.isha ?: "--:--",
                checked = state.settings.ishaAdzanEnabled,
                onCheckedChange = { viewModel.togglePrayer(PrayerName.ISHA, it) }
            )
        }

        item {
            SectionHeader(
                eyebrow = if (isEnglish) "7 Day View" else "Tampilan 7 Hari",
                title = if (isEnglish) "One-week schedule" else "Jadwal satu minggu"
            )
        }

        items(state.weeklyPrayerTimes, key = { it.date }) { prayerTime ->
            WeeklyPrayerCard(prayerTime = prayerTime, appLanguage = state.settings.appLanguage)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyPrayerHero(
    prayerTime: PrayerTime?,
    settings: UserSettings,
    adzanEnabled: Boolean,
    onToggleAdzan: (Boolean) -> Unit,
    onOpenWeeklySchedule: () -> Unit
) {
    val isEnglish = settings.appLanguage == AppLanguage.ENGLISH
    val nextPrayer = prayerTime?.let(DateTimeUtils::nextPrayer)
    val countdown = prayerTime?.let(DateTimeUtils::countdownClockToNextPrayer) ?: "--:--:--"
    val detailedTimes = prayerTime?.let {
        PrayerTimeCalculator.calculateDetailedPrayerTimes(
            date = LocalDate.parse(it.date),
            latitude = it.latitude,
            longitude = it.longitude,
            locationName = it.locationName,
            calculationMethod = settings.prayerCalculationMethod,
            asrMadhhab = settings.asrMadhhab
        )
    }

    HeroCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            PrayerHeroMetric(
                modifier = Modifier.weight(1f),
                title = if (isEnglish) "NEXT PRAYER" else "SHOLAT BERIKUTNYA",
                primary = nextPrayer?.first?.displayName(settings.appLanguage) ?: PrayerName.FAJR.displayName(settings.appLanguage),
                secondary = nextPrayer?.second ?: "--:--"
            )
            PrayerHeroMetric(
                modifier = Modifier.weight(1f),
                title = if (isEnglish) "REMAINING" else "SISA WAKTU",
                primary = countdown,
                secondary = if (isEnglish) {
                    "Before ${nextPrayer?.first?.displayName(settings.appLanguage) ?: PrayerName.FAJR.displayName(settings.appLanguage)}"
                } else {
                    "Menuju ${nextPrayer?.first?.displayName(settings.appLanguage) ?: PrayerName.FAJR.displayName(settings.appLanguage)}"
                }
            )
        }

        prayerTime?.let {
            val prayerEntries: List<Pair<com.sajda.app.domain.model.PrayerName, String>> =
                DateTimeUtils.prayerEntries(it)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                detailedTimes?.let { details ->
                    MetadataChip(text = "Imsak ${details.imsak}")
                    MetadataChip(text = if (isEnglish) "Sunrise ${details.sunrise}" else "Terbit ${details.sunrise}")
                }
                prayerEntries.forEach { entry ->
                    val prayer = entry.first
                    val time = entry.second
                    MetadataChip(text = "${prayer.displayName(settings.appLanguage)} $time", active = prayer == nextPrayer?.first)
                }
            }
        }

        Text(
            text = if (isEnglish) {
                "${settings.prayerCalculationMethod.label} | Asr ${settings.asrMadhhab.label}"
            } else {
                "${settings.prayerCalculationMethod.label} | Asar ${settings.asrMadhhab.label}"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEnglish) "Automatic adhan" else "Adzan otomatis",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Switch(checked = adzanEnabled, onCheckedChange = onToggleAdzan)
        }

        Text(
            text = if (isEnglish) "View weekly schedule" else "Lihat jadwal mingguan",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.10f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable(onClick = onOpenWeeklySchedule)
        )
    }
}

@Composable
private fun PrayerHeroMetric(
    title: String,
    primary: String,
    secondary: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.10f))
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
        )
        Text(
            text = primary,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = secondary,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f)
        )
    }
}

@Composable
private fun PrayerToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SanctuaryCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeeklyPrayerCard(prayerTime: PrayerTime, appLanguage: AppLanguage) {
    val isEnglish = appLanguage == AppLanguage.ENGLISH
    val nextPrayer = DateTimeUtils.nextPrayer(prayerTime)

    SanctuaryCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = DateTimeUtils.formatDateLabel(prayerTime.date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEnglish) {
                        "Qibla direction ${prayerTime.qiblaDirection.toInt()}°"
                    } else {
                        "Arah kiblat ${prayerTime.qiblaDirection.toInt()} derajat"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MetadataChip(text = if (isEnglish) {
                "Next ${nextPrayer.first.displayName(appLanguage)}"
            } else {
                "Berikutnya ${nextPrayer.first.displayName(appLanguage)}"
            }, active = true)
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            listOf(
                PrayerName.FAJR.displayName(appLanguage) to prayerTime.fajr,
                PrayerName.DHUHR.displayName(appLanguage) to prayerTime.dhuhr,
                PrayerName.ASR.displayName(appLanguage) to prayerTime.asr,
                PrayerName.MAGHRIB.displayName(appLanguage) to prayerTime.maghrib,
                PrayerName.ISHA.displayName(appLanguage) to prayerTime.isha
            ).forEach { entry ->
                PrayerTimeColumn(entry.first, entry.second)
            }
        }
    }
}

@Composable
private fun PrayerTimeColumn(title: String, time: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = time,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
