package com.sajda.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajda.app.BuildConfig
import com.sajda.app.domain.model.AudioDownloadMode
import com.sajda.app.domain.model.CalendarDisplayMode
import com.sajda.app.ui.viewmodel.AppUpdateUiState
import com.sajda.app.ui.viewmodel.BackupUiState
import com.sajda.app.ui.viewmodel.SettingsViewModel
import com.sajda.app.util.displayLabel
import com.sajda.app.util.displayName
import com.sajda.app.util.pick

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 132.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            SettingsHeader(
                title = settings.pick("Pengaturan", "Settings"),
                onBack = onBack
            )
        }

        item {
            SettingsSection(title = settings.pick("Adzan & Notifikasi", "Adhan & Notifications")) {
                SettingsActionRow(
                    icon = Icons.Rounded.Mosque,
                    title = settings.pick("Suara Adzan", "Adhan Sound"),
                    value = settings.adzanSound.title,
                    iconTint = MaterialTheme.colorScheme.primary,
                    iconBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    onClick = onOpenAdhanSettings
                )
                SettingsActionRow(
                    icon = Icons.Rounded.WbTwilight,
                    title = settings.pick("Suara Adzan Subuh", "Fajr Adhan Sound"),
                    value = settings.fajrAdzanSound.title,
                    iconTint = MaterialTheme.colorScheme.primary,
                    iconBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    onClick = onOpenAdhanSettings
                )
                SettingsToggleRow(
                    icon = Icons.Rounded.NotificationsActive,
                    title = settings.pick("Adzan Otomatis", "Automatic Adhan"),
                    subtitle = if (settings.adzanEnabled) {
                        settings.pick("Aktif untuk jadwal yang dipilih", "Active for selected prayer times")
                    } else {
                        settings.pick("Semua alarm adzan dimatikan", "All adhan alarms are disabled")
                    },
                    checked = settings.adzanEnabled,
                    iconTint = MaterialTheme.colorScheme.primary,
                    iconBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    onCheckedChange = viewModel::setAdzanEnabled
                )
                SettingsActionRow(
                    icon = Icons.Rounded.Tune,
                    title = settings.pick("Diagnosa Adzan", "Adhan Diagnostics"),
                    value = settings.pick("Tes, exact alarm, baterai, izin", "Test, exact alarm, battery, permissions"),
                    iconTint = MaterialTheme.colorScheme.primary,
                    iconBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    onClick = onOpenAdhanSettings
                )
            }
        }

        item {
            SettingsSection(title = settings.pick("Tampilan", "Appearance")) {
                SettingsToggleRow(
                    icon = Icons.Rounded.DarkMode,
                    title = settings.pick("Mode Gelap", "Dark Mode"),
                    subtitle = settings.pick("Gunakan tampilan malam untuk aplikasi", "Use the night look across the app"),
                    checked = settings.darkMode || settings.nightMode,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconBackground = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f),
                    onCheckedChange = {
                        viewModel.setDarkMode(it)
                        viewModel.setNightMode(it)
                    }
                )
                SettingsSliderBlock(
                    icon = Icons.Rounded.FormatSize,
                    title = settings.pick("Ukuran Font Al-Qur'an", "Qur'an Font Size"),
                    value = settings.arabicFontSize.toFloat(),
                    valueLabel = settings.arabicFontSize.toString(),
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconBackground = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f),
                    onValueChange = { viewModel.setArabicFontSize(it.toInt()) }
                )
                SettingsActionRow(
                    icon = Icons.Rounded.Translate,
                    title = settings.pick("Bahasa", "Language"),
                    value = settings.appLanguage.displayName(),
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconBackground = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f),
                    onClick = onOpenLanguageSettings
                )
                SettingsToggleRow(
                    icon = Icons.Rounded.CalendarMonth,
                    title = settings.pick("Kalender Hijriah", "Hijri Calendar"),
                    subtitle = settings.pick("Matikan jika ingin tampilan Masehi", "Turn off for Gregorian mode"),
                    checked = settings.calendarDisplayMode == CalendarDisplayMode.HIJRI,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconBackground = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f),
                    onCheckedChange = {
                        viewModel.setCalendarDisplayMode(
                            if (it) CalendarDisplayMode.HIJRI else CalendarDisplayMode.GREGORIAN
                        )
                    }
                )
            }
        }

        item {
            SettingsSection(title = "Al-Qur'an") {
                SettingsActionRow(
                    icon = Icons.Rounded.MenuBook,
                    title = settings.pick("Jenis Terjemahan", "Translation Mode"),
                    value = settings.quranReadingMode.displayLabel(settings.appLanguage),
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    iconBackground = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    onClick = onOpenLanguageSettings
                )
                SettingsActionRow(
                    icon = Icons.Rounded.RecordVoiceOver,
                    title = settings.pick("Qari (Murottal)", "Reciter"),
                    value = settings.selectedQuranReciter.title,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    iconBackground = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    onClick = onOpenAudioManagement
                )
                SettingsActionRow(
                    icon = Icons.Rounded.DownloadForOffline,
                    title = settings.pick("Audio Offline", "Offline Audio"),
                    value = when (settings.audioDownloadMode) {
                        AudioDownloadMode.SELECTED_RECITER_ONLY -> settings.pick("Qari aktif saja", "Selected reciter only")
                        AudioDownloadMode.ALL_RECITERS -> settings.pick("Semua qari", "All reciters")
                    },
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    iconBackground = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    onClick = onOpenAudioManagement
                )
                SettingsToggleRow(
                    icon = Icons.Rounded.Wifi,
                    title = settings.pick("Unduh Hanya via Wi-Fi", "Wi-Fi Only Downloads"),
                    subtitle = settings.pick("Berlaku untuk semua unduhan audio Al-Qur'an", "Applies to all Qur'an audio downloads"),
                    checked = settings.wifiOnlyAudioDownloads,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    iconBackground = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    onCheckedChange = viewModel::setWifiOnlyAudioDownloads
                )
            }
        }

        item {
            SettingsSection(title = settings.pick("Lainnya", "More")) {
                SettingsActionRow(
                    icon = Icons.Rounded.LocationOn,
                    title = settings.pick("Lokasi Aktif", "Active Location"),
                    value = settings.locationName.ifBlank {
                        settings.pick("Belum ada lokasi aktif", "No active location yet")
                    },
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onOpenLocationSettings
                )
                SettingsActionRow(
                    icon = Icons.Rounded.Alarm,
                    title = settings.pick("Reminder Ibadah", "Worship Reminders"),
                    value = settings.pick("Atur jadwal harian", "Manage daily reminders"),
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onOpenSmartReminders
                )
                SettingsActionRow(
                    icon = Icons.Rounded.SystemUpdateAlt,
                    title = settings.pick("Pembaruan Aplikasi", "App Updates"),
                    value = updateLabel(settings = settings, updateState = updateState),
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onOpenUpdateCenter
                )
                SettingsActionRow(
                    icon = Icons.Rounded.Backup,
                    title = settings.pick("Backup Data Lokal", "Backup Local Data"),
                    value = settings.lastBackupAt.ifBlank {
                        settings.pick("Belum pernah backup", "No backup yet")
                    },
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = viewModel::exportBackup
                )
                SettingsActionRow(
                    icon = Icons.Rounded.Restore,
                    title = settings.pick("Restore Data Lokal", "Restore Local Data"),
                    value = settings.lastRestoreAt.ifBlank {
                        settings.pick("Belum pernah restore", "No restore yet")
                    },
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = viewModel::restoreBackup
                )
                SettingsStaticRow(
                    icon = Icons.Rounded.Info,
                    title = settings.pick("Tentang NurApp", "About NurApp"),
                    value = "v${BuildConfig.VERSION_NAME}",
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        item {
            BackupStatusCard(
                backupState = backupState,
                emptyMessage = settings.pick(
                    "Bookmark, terakhir dibaca, qari, bahasa, dan setting adzan ikut dibackup.",
                    "Bookmarks, last read, reciter, language, and adhan settings are included in backups."
                )
            )
        }
    }
}

@Composable
private fun SettingsHeader(
    title: String,
    onBack: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.70f)
        )
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    value: String,
    iconTint: Color,
    iconBackground: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsRowLead(
            icon = icon,
            iconTint = iconTint,
            iconBackground = iconBackground,
            title = title,
            modifier = Modifier.weight(1f)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun SettingsStaticRow(
    icon: ImageVector,
    title: String,
    value: String,
    iconTint: Color,
    iconBackground: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsRowLead(
            icon = icon,
            iconTint = iconTint,
            iconBackground = iconBackground,
            title = title,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    iconTint: Color,
    iconBackground: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsRowLead(
            icon = icon,
            iconTint = iconTint,
            iconBackground = iconBackground,
            title = title,
            subtitle = subtitle,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsSliderBlock(
    icon: ImageVector,
    title: String,
    value: Float,
    valueLabel: String,
    iconTint: Color,
    iconBackground: Color,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsRowLead(
            icon = icon,
            iconTint = iconTint,
            iconBackground = iconBackground,
            title = title,
            trailingText = valueLabel
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 24f..40f
        )
    }
}

@Composable
private fun SettingsRowLead(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    subtitle: String? = null,
    trailingText: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!trailingText.isNullOrBlank()) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BackupStatusCard(
    backupState: BackupUiState,
    emptyMessage: String
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
    ) {
        Text(
            text = backupState.message ?: emptyMessage,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (backupState.message == null) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    }
}

private fun updateLabel(
    settings: com.sajda.app.domain.model.UserSettings,
    updateState: AppUpdateUiState
): String {
    return when {
        updateState.hasUpdate -> settings.pick(
            "Versi ${updateState.latestVersionName}",
            "Version ${updateState.latestVersionName}"
        )
        updateState.lastCheckedAt.isBlank() -> settings.pick(
            "Cek manual",
            "Manual check"
        )
        else -> settings.pick("Sudah terbaru", "Up to date")
    }
}
