package com.sajda.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
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
import com.sajda.app.domain.model.AudioDownloadMode
import com.sajda.app.domain.model.CalendarDisplayMode
import com.sajda.app.ui.component.SajdaTopAction
import com.sajda.app.ui.component.SajdaTopBar
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.viewmodel.AppUpdateUiState
import com.sajda.app.ui.viewmodel.BackupUiState
import com.sajda.app.ui.viewmodel.SettingsViewModel
import com.sajda.app.util.displayLabel
import com.sajda.app.util.displayName
import com.sajda.app.util.pick

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    updateState: AppUpdateUiState,
    onOpenAdhanSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenLanguageSettings: () -> Unit,
    onOpenUpdateCenter: () -> Unit,
    onOpenAudioManagement: () -> Unit,
    onOpenSmartReminders: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val backupState by viewModel.backupState.collectAsStateWithLifecycle()
    val isEnglish = settings.appLanguage == com.sajda.app.domain.model.AppLanguage.ENGLISH

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 150.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SajdaTopBar(
                title = settings.pick("Pengaturan", "Settings"),
                leading = onBack?.let { backAction ->
                    {
                        SajdaTopAction(
                            icon = Icons.Rounded.ArrowBack,
                            label = settings.pick("Kembali", "Back"),
                            onClick = backAction
                        )
                    }
                }
            )
        }

        item {
            SanctuaryCard {
                SettingsCardTitle(settings.pick("Adzan", "Adhan"))
                ToggleRow(
                    title = settings.pick("Adzan otomatis", "Automatic adhan"),
                    subtitle = if (settings.adzanEnabled) settings.pick("Aktif", "On") else settings.pick("Nonaktif", "Off"),
                    checked = settings.adzanEnabled,
                    onCheckedChange = viewModel::setAdzanEnabled
                )
                ActionRow(
                    title = settings.pick("Diagnosa adzan", "Adhan diagnostics"),
                    value = settings.pick("Tes, izin, battery, exact alarm", "Tests, permissions, battery, exact alarm"),
                    onClick = onOpenAdhanSettings
                )
                ActionRow(
                    title = settings.pick("Suara reguler", "Regular sound"),
                    value = settings.adzanSound.title,
                    onClick = onOpenAdhanSettings
                )
                ActionRow(
                    title = settings.pick("Suara Subuh", "Fajr sound"),
                    value = settings.fajrAdzanSound.title,
                    onClick = onOpenAdhanSettings
                )
            }
        }

        item {
            SanctuaryCard {
                SettingsCardTitle("Al-Qur'an")
                ActionRow(
                    title = settings.pick("Qari aktif", "Active reciter"),
                    value = settings.selectedQuranReciter.title,
                    onClick = onOpenAudioManagement
                )
                ActionRow(
                    title = settings.pick("Audio offline", "Offline audio"),
                    value = settings.pick("Kelola unduhan", "Manage downloads"),
                    onClick = onOpenAudioManagement
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AudioDownloadMode.entries.forEach { mode ->
                        ChoiceChip(
                            label = when (mode) {
                                AudioDownloadMode.SELECTED_RECITER_ONLY -> settings.pick("Qari aktif saja", "Selected reciter only")
                                AudioDownloadMode.ALL_RECITERS -> settings.pick("Semua qari", "All reciters")
                            },
                            selected = settings.audioDownloadMode == mode,
                            onClick = { viewModel.setAudioDownloadMode(mode) }
                        )
                    }
                }
                ToggleRow(
                    title = settings.pick("Unduh hanya lewat Wi-Fi", "Download on Wi-Fi only"),
                    subtitle = settings.pick("Berlaku untuk audio Qur'an offline", "Applies to offline Qur'an audio"),
                    checked = settings.wifiOnlyAudioDownloads,
                    onCheckedChange = viewModel::setWifiOnlyAudioDownloads
                )
            }
        }

        item {
            SanctuaryCard {
                SettingsCardTitle(settings.pick("Bahasa & Tampilan", "Language & Appearance"))
                ActionRow(
                    title = settings.pick("Bahasa aplikasi", "App language"),
                    value = settings.appLanguage.displayName(),
                    onClick = onOpenLanguageSettings
                )
                ToggleRow(
                    title = settings.pick("Mode gelap", "Dark mode"),
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
                    valueLabel = settings.arabicFontSize.toString(),
                    onValueChange = { viewModel.setArabicFontSize(it.toInt()) }
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
                SettingsCardTitle(settings.pick("Lokasi & Jadwal", "Location & Schedule"))
                ToggleRow(
                    title = settings.pick("Lokasi otomatis", "Auto location"),
                    subtitle = settings.locationName.ifBlank {
                        settings.pick("Belum ada lokasi aktif", "No active location yet")
                    },
                    checked = settings.autoLocation,
                    onCheckedChange = viewModel::setAutoLocation
                )
                ActionRow(
                    title = settings.pick("Lokasi aktif", "Active location"),
                    value = settings.locationName.ifBlank {
                        settings.pick("Pilih lokasi", "Choose location")
                    },
                    onClick = onOpenLocationSettings
                )
                ActionRow(
                    title = settings.pick("Metode hisab", "Calculation method"),
                    value = "${settings.prayerCalculationMethod.label} | ${settings.asrMadhhab.label}",
                    onClick = onOpenLocationSettings
                )
                ActionRow(
                    title = settings.pick("Reminder ibadah", "Worship reminders"),
                    value = settings.pick("Atur jadwal harian", "Manage daily reminders"),
                    onClick = onOpenSmartReminders
                )
            }
        }

        item {
            SanctuaryCard {
                SettingsCardTitle(settings.pick("Data & Update", "Data & Updates"))
                ActionRow(
                    title = settings.pick("Pembaruan aplikasi", "App updates"),
                    value = if (updateState.hasUpdate) {
                        settings.pick("Versi ${updateState.latestVersionName}", "Version ${updateState.latestVersionName}")
                    } else {
                        settings.pick("Sudah terbaru", "Up to date")
                    },
                    onClick = onOpenUpdateCenter
                )
                ActionRow(
                    title = settings.pick("Backup data lokal", "Backup local data"),
                    value = settings.lastBackupAt.ifBlank {
                        settings.pick("Belum pernah backup", "No backup yet")
                    },
                    onClick = viewModel::exportBackup
                )
                ActionRow(
                    title = settings.pick("Restore data lokal", "Restore local data"),
                    value = settings.lastRestoreAt.ifBlank {
                        settings.pick("Belum pernah restore", "No restore yet")
                    },
                    onClick = viewModel::restoreBackup
                )
                BackupStatusCard(
                    backupState = backupState,
                    emptyMessage = settings.pick(
                        "Bookmark, terakhir dibaca, qari, bahasa, dan setting adzan ikut dibackup.",
                        "Bookmarks, last read, reciter, language, and adhan settings are included in the backup."
                    )
                )
            }
        }

        item {
            Text(
                text = if (isEnglish) "NurApp ${BuildConfig.VERSION_NAME}" else "NurApp ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SettingsCardTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SliderRow(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
    }
}

@Composable
private fun ActionRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun BackupStatusCard(
    backupState: BackupUiState,
    emptyMessage: String
) {
    Text(
        text = backupState.message ?: emptyMessage,
        style = MaterialTheme.typography.bodySmall,
        color = if (backupState.message == null) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.primary
        }
    )
}
