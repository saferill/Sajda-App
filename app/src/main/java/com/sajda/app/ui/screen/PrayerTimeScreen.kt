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
import java.time.LocalDate

@Composable
fun PrayerTimeScreen(
    viewModel: PrayerTimeViewModel,
    onOpenWeeklySchedule: () -> Unit,
    onOpenQibla: () -> Unit,
    onOpenLocationSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
                        text = "Prayer Times",
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
                    SajdaTopAction(Icons.Rounded.LocationOn, "Lokasi", onOpenLocationSettings)
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
                eyebrow = "Prayer Controls",
                title = "Adzan per waktu",
                actionLabel = "Mingguan",
                onAction = onOpenWeeklySchedule
            )
        }

        item {
            PrayerToggleCard(
                title = "Subuh",
                subtitle = state.todayPrayerTime?.fajr ?: "--:--",
                checked = state.settings.fajrAdzanEnabled,
                onCheckedChange = { viewModel.togglePrayer(PrayerName.FAJR, it) }
            )
        }

        item {
            PrayerToggleCard(
                title = "Dzuhur",
                subtitle = state.todayPrayerTime?.dhuhr ?: "--:--",
                checked = state.settings.dhuhrAdzanEnabled,
                onCheckedChange = { viewModel.togglePrayer(PrayerName.DHUHR, it) }
            )
        }

        item {
            PrayerToggleCard(
                title = "Ashar",
                subtitle = state.todayPrayerTime?.asr ?: "--:--",
                checked = state.settings.asrAdzanEnabled,
                onCheckedChange = { viewModel.togglePrayer(PrayerName.ASR, it) }
            )
        }

        item {
            PrayerToggleCard(
                title = "Maghrib",
                subtitle = state.todayPrayerTime?.maghrib ?: "--:--",
                checked = state.settings.maghribAdzanEnabled,
                onCheckedChange = { viewModel.togglePrayer(PrayerName.MAGHRIB, it) }
            )
        }

        item {
            PrayerToggleCard(
                title = "Isya",
                subtitle = state.todayPrayerTime?.isha ?: "--:--",
                checked = state.settings.ishaAdzanEnabled,
                onCheckedChange = { viewModel.togglePrayer(PrayerName.ISHA, it) }
            )
        }

        item {
            SectionHeader(eyebrow = "7 Day View", title = "Jadwal satu minggu")
        }

        items(state.weeklyPrayerTimes, key = { it.date }) { prayerTime ->
            WeeklyPrayerCard(prayerTime = prayerTime)
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
                title = "NEXT PRAYER",
                primary = nextPrayer?.first?.label ?: "Subuh",
                secondary = nextPrayer?.second ?: "--:--"
            )
            PrayerHeroMetric(
                modifier = Modifier.weight(1f),
                title = "REMAINING",
                primary = countdown,
                secondary = "Menuju ${nextPrayer?.first?.label ?: "Subuh"}"
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
                    MetadataChip(text = "Terbit ${details.sunrise}")
                }
                prayerEntries.forEach { entry ->
                    val prayer = entry.first
                    val time = entry.second
                    MetadataChip(text = "${prayer.label} $time", active = prayer == nextPrayer?.first)
                }
            }
        }

        Text(
            text = "${settings.prayerCalculationMethod.label} | Asar ${settings.asrMadhhab.label}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Adzan otomatis",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Switch(checked = adzanEnabled, onCheckedChange = onToggleAdzan)
        }

        Text(
            text = "Lihat jadwal mingguan",
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
private fun WeeklyPrayerCard(prayerTime: PrayerTime) {
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
                    text = "Arah kiblat ${prayerTime.qiblaDirection.toInt()} derajat",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MetadataChip(text = "Next ${nextPrayer.first.label}", active = true)
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            listOf(
                "Subuh" to prayerTime.fajr,
                "Dzuhur" to prayerTime.dhuhr,
                "Ashar" to prayerTime.asr,
                "Maghrib" to prayerTime.maghrib,
                "Isya" to prayerTime.isha
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
