package com.sajda.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajda.app.BuildConfig
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.CalendarDisplayMode
import com.sajda.app.domain.model.QuranReciter
import com.sajda.app.ui.component.SajdaTopAction
import com.sajda.app.ui.component.SajdaTopBar
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.component.SectionHeader
import com.sajda.app.ui.viewmodel.AppUpdateUiState
import com.sajda.app.ui.viewmodel.SettingsViewModel
import com.sajda.app.util.displayName
import com.sajda.app.util.displayLabel
import com.sajda.app.util.isEnglish
import com.sajda.app.util.pick

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    updateState: AppUpdateUiState,
    onOpenAdhanSettings: () -> Unit,
    onOpenAppearanceSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenLanguageSettings: () -> Unit,
    onOpenUpdateCenter: () -> Unit,
    onOpenAudioManagement: () -> Unit,
    onOpenWorshipProgress: () -> Unit,
    onOpenSmartReminders: () -> Unit,
    onOpenBackgroundAudioInfo: () -> Unit,
    onOpenWidgetPreview: () -> Unit,
    onOpenEmptyState: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isEnglish = settings.appLanguage.isEnglish()

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 150.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            SajdaTopBar(
                title = if (isEnglish) "Settings" else "Pengaturan",
                subtitle = "NurApp",
                leading = onBack?.let { backAction ->
                    {
                        SajdaTopAction(
                            icon = Icons.Rounded.ArrowBack,
                            label = settings.pick("Kembali", "Back"),
                            onClick = backAction
                        )
                    }
                },
                trailing = {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }

        item {
            SanctuaryCard {
                SectionHeader(
                    eyebrow = settings.pick("Adzan", "Adhan"),
                    title = settings.pick("Suara, jadwal, dan perilaku alarm", "Sound, schedule, and alarm behavior")
                )
                ToggleRow(
                    title = settings.pick("Adzan otomatis", "Automatic adhan"),
                    subtitle = settings.pick("Aktifkan alarm adzan offline", "Enable offline adhan alarms"),
                    checked = settings.adzanEnabled,
                    onCheckedChange = viewModel::setAdzanEnabled
                )
                ActionRow(
                    title = settings.pick("Suara adzan reguler", "Regular adhan sound"),
                    subtitle = settings.adzanSound.title,
                    onClick = onOpenAdhanSettings
                )
                ActionRow(
                    title = settings.pick("Suara adzan Subuh", "Fajr adhan sound"),
                    subtitle = settings.fajrAdzanSound.title,
                    onClick = onOpenAdhanSettings
                )
                ActionRow(
                    title = settings.pick("Pengaturan adzan lengkap", "Full adhan settings"),
                    subtitle = settings.pick("Per-prayer toggle, getaran, snooze, dan diagnosa", "Per-prayer toggles, vibration, snooze, and diagnostics"),
                    onClick = onOpenAdhanSettings
                )
            }
        }

        item {
            SanctuaryCard {
                SectionHeader(
                    eyebrow = "Al-Qur'an",
                    title = settings.pick("Bacaan, audio, dan qari", "Reading, audio, and reciters")
                )
                ActionRow(
                    title = settings.pick("Mode terjemahan", "Translation mode"),
                    subtitle = settings.quranReadingMode.displayLabel(settings.appLanguage),
                    onClick = onOpenLanguageSettings
                )
                Text(
                    text = settings.pick("Qari aktif", "Active reciter"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuranReciter.entries.forEach { reciter ->
                        ChoiceChip(
                            label = reciter.title,
                            selected = settings.selectedQuranReciter == reciter,
                            onClick = { viewModel.setSelectedQuranReciter(reciter) }
                        )
                    }
                }
                ActionRow(
                    title = settings.pick("Audio offline", "Offline audio"),
                    subtitle = settings.pick("Kelola hasil unduhan murattal", "Manage downloaded murattal files"),
                    onClick = onOpenAudioManagement
                )
            }
        }

        item {
            SanctuaryCard {
                SectionHeader(
                    eyebrow = settings.pick("Bahasa & Tampilan", "Language & Appearance"),
                    title = settings.pick("Rapikan pengalaman aplikasi", "Clean up the app experience")
                )
                Text(
                    text = settings.pick("Bahasa aplikasi", "App language"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.entries.forEach { language ->
                        ChoiceChip(
                            label = language.displayName(),
                            selected = settings.appLanguage == language,
                            onClick = { viewModel.setAppLanguage(language) }
                        )
                    }
                }
                ToggleRow(
                    title = settings.pick("Mode gelap", "Dark mode"),
                    subtitle = settings.pick("Gunakan tampilan malam untuk aplikasi", "Use the darker app appearance"),
                    checked = settings.darkMode || settings.nightMode,
                    onCheckedChange = {
                        viewModel.setDarkMode(it)
                        viewModel.setNightMode(it)
                    }
                )
                SliderRow(
                    title = settings.pick("Ukuran font Arab", "Arabic font size"),
                    value = settings.arabicFontSize.toFloat(),
                    valueRange = 24f..40f,
                    onValueChange = { viewModel.setArabicFontSize(it.toInt()) }
                )
                ActionRow(
                    title = settings.pick("Tampilan detail", "Appearance details"),
                    subtitle = settings.pick("Spacing, fokus, dan opsi baca", "Spacing, focus mode, and reading options"),
                    onClick = onOpenAppearanceSettings
                )
            }
        }

        item {
            SanctuaryCard {
                SectionHeader(
                    eyebrow = settings.pick("Lokasi & Kalender", "Location & Calendar"),
                    title = settings.pick("Waktu sholat dan tampilan tanggal", "Prayer times and calendar display")
                )
                ToggleRow(
                    title = settings.pick("Deteksi lokasi otomatis", "Auto detect location"),
                    subtitle = settings.locationName,
                    checked = settings.autoLocation,
                    onCheckedChange = viewModel::setAutoLocation
                )
                ActionRow(
                    title = settings.pick("Metode perhitungan", "Calculation method"),
                    subtitle = "${settings.prayerCalculationMethod.label} • ${settings.asrMadhhab.label}",
                    onClick = onOpenLocationSettings
                )
                Text(
                    text = settings.pick("Mode kalender", "Calendar mode"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChoiceChip(
                        label = settings.pick("Hijriah", "Hijri"),
                        selected = settings.calendarDisplayMode == CalendarDisplayMode.HIJRI,
                        onClick = { viewModel.setCalendarDisplayMode(CalendarDisplayMode.HIJRI) }
                    )
                    ChoiceChip(
                        label = settings.pick("Masehi", "Gregorian"),
                        selected = settings.calendarDisplayMode == CalendarDisplayMode.GREGORIAN,
                        onClick = { viewModel.setCalendarDisplayMode(CalendarDisplayMode.GREGORIAN) }
                    )
                }
            }
        }

        item {
            SanctuaryCard {
                SectionHeader(
                    eyebrow = settings.pick("Lainnya", "More"),
                    title = settings.pick("Tools, update, dan produktivitas", "Tools, updates, and productivity")
                )
                ActionRow(
                    title = settings.pick("Pembaruan aplikasi", "App updates"),
                    subtitle = if (updateState.hasUpdate) {
                        settings.pick("Versi ${updateState.latestVersionName} tersedia", "Version ${updateState.latestVersionName} is available")
                    } else {
                        settings.pick("Sudah di versi terbaru", "Already on the latest version")
                    },
                    onClick = onOpenUpdateCenter
                )
                ActionRow(
                    title = settings.pick("Reminder & target ibadah", "Reminder & worship goals"),
                    subtitle = settings.pick("Atur rutinitas Qur'an dan dzikir", "Set Qur'an and dhikr routines"),
                    onClick = onOpenSmartReminders
                )
                ActionRow(
                    title = settings.pick("Widget dan audio", "Widgets and audio"),
                    subtitle = settings.pick("Info audio latar dan preview widget", "Background audio info and widget preview"),
                    onClick = onOpenBackgroundAudioInfo
                )
            }
        }

        item {
            Text(
                text = "NurApp ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SliderRow(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
    }
}

@Composable
private fun ActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}
