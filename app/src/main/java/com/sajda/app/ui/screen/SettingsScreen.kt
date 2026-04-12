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
import com.sajda.app.util.displayLabelRes
import com.sajda.app.util.displayNameRes

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
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.tab_settings),
                onBack = onBack
            )
        }

        item {
            SettingsSection(title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.adhan_notifications)) {
                SettingsActionRow(
                    icon = Icons.Rounded.Mosque,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.adhan_sound),
                    value = settings.adzanSound.title,
                    iconTint = MaterialTheme.colorScheme.primary,
                    iconBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    onClick = onOpenAdhanSettings
                )
                SettingsActionRow(
                    icon = Icons.Rounded.WbTwilight,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.fajr_adhan_sound),
                    value = settings.fajrAdzanSound.title,
                    iconTint = MaterialTheme.colorScheme.primary,
                    iconBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    onClick = onOpenAdhanSettings
                )
                SettingsToggleRow(
                    icon = Icons.Rounded.NotificationsActive,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.automatic_adhan),
                    subtitle = if (settings.adzanEnabled) {
                        androidx.compose.ui.res.stringResource(com.sajda.app.R.string.active_for_selected_prayer_times)
                    } else {
                        androidx.compose.ui.res.stringResource(com.sajda.app.R.string.all_adhan_alarms_are_disabled)
                    },
                    checked = settings.adzanEnabled,
                    iconTint = MaterialTheme.colorScheme.primary,
                    iconBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    onCheckedChange = viewModel::setAdzanEnabled
                )
                SettingsActionRow(
                    icon = Icons.Rounded.Tune,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.adhan_diagnostics),
                    value = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.test_exact_alarm_battery_permissions),
                    iconTint = MaterialTheme.colorScheme.primary,
                    iconBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    onClick = onOpenAdhanSettings
                )
            }
        }

        item {
            SettingsSection(title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.appearance)) {
                SettingsToggleRow(
                    icon = Icons.Rounded.DarkMode,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.dark_mode),
                    subtitle = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.use_the_night_look_across_the_app),
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
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.qur_an_font_size),
                    value = settings.arabicFontSize.toFloat(),
                    valueLabel = settings.arabicFontSize.toString(),
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconBackground = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f),
                    onValueChange = { viewModel.setArabicFontSize(it.toInt()) }
                )
                SettingsActionRow(
                    icon = Icons.Rounded.Translate,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.language),
                    value = androidx.compose.ui.res.stringResource(settings.appLanguage.displayNameRes()),
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconBackground = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f),
                    onClick = onOpenLanguageSettings
                )
                SettingsToggleRow(
                    icon = Icons.Rounded.CalendarMonth,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.hijri_calendar),
                    subtitle = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.turn_off_for_gregorian_mode),
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
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.translation_mode),
                    value = androidx.compose.ui.res.stringResource(settings.quranReadingMode.displayLabelRes()),
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    iconBackground = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    onClick = onOpenLanguageSettings
                )
                SettingsActionRow(
                    icon = Icons.Rounded.RecordVoiceOver,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.reciter),
                    value = settings.selectedQuranReciter.title,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    iconBackground = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    onClick = onOpenAudioManagement
                )
                SettingsActionRow(
                    icon = Icons.Rounded.DownloadForOffline,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.offline_audio),
                    value = when (settings.audioDownloadMode) {
                        AudioDownloadMode.SELECTED_RECITER_ONLY -> androidx.compose.ui.res.stringResource(com.sajda.app.R.string.selected_reciter_only)
                        AudioDownloadMode.ALL_RECITERS -> androidx.compose.ui.res.stringResource(com.sajda.app.R.string.all_reciters)
                    },
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    iconBackground = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    onClick = onOpenAudioManagement
                )
                SettingsToggleRow(
                    icon = Icons.Rounded.Wifi,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.wi_fi_only_downloads),
                    subtitle = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.applies_to_all_qur_an_audio_downloads),
                    checked = settings.wifiOnlyAudioDownloads,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    iconBackground = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    onCheckedChange = viewModel::setWifiOnlyAudioDownloads
                )
            }
        }

        item {
            SettingsSection(title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.more)) {
                SettingsActionRow(
                    icon = Icons.Rounded.LocationOn,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.active_location),
                    value = settings.locationName.ifBlank {
                        androidx.compose.ui.res.stringResource(com.sajda.app.R.string.no_active_location_yet)
                    },
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onOpenLocationSettings
                )
                SettingsActionRow(
                    icon = Icons.Rounded.Alarm,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.worship_reminders),
                    value = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.manage_daily_reminders),
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onOpenSmartReminders
                )
                SettingsActionRow(
                    icon = Icons.Rounded.SystemUpdateAlt,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.app_updates),
                    value = updateLabel(settings = settings, updateState = updateState),
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onOpenUpdateCenter
                )
                SettingsActionRow(
                    icon = Icons.Rounded.Backup,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.backup_local_data),
                    value = settings.lastBackupAt.ifBlank {
                        androidx.compose.ui.res.stringResource(com.sajda.app.R.string.no_backup_yet)
                    },
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = viewModel::exportBackup
                )
                SettingsActionRow(
                    icon = Icons.Rounded.Restore,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.restore_local_data),
                    value = settings.lastRestoreAt.ifBlank {
                        androidx.compose.ui.res.stringResource(com.sajda.app.R.string.no_restore_yet)
                    },
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = viewModel::restoreBackup
                )
                SettingsStaticRow(
                    icon = Icons.Rounded.Info,
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.about_nurapp),
                    value = "v${BuildConfig.VERSION_NAME}",
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        item {
            BackupStatusCard(
                backupState = backupState,
                emptyMessage = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.bookmarks_last_read_reciter_language_and)
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

@Composable
private fun updateLabel(
    settings: com.sajda.app.domain.model.UserSettings,
    updateState: AppUpdateUiState
): String {
    return when {
        updateState.hasUpdate -> androidx.compose.ui.res.stringResource(com.sajda.app.R.string.version_updatestate_latestversionname)
        updateState.lastCheckedAt.isBlank() -> androidx.compose.ui.res.stringResource(com.sajda.app.R.string.manual_check)
        else -> androidx.compose.ui.res.stringResource(com.sajda.app.R.string.up_to_date)
    }
}
