package com.sajda.app.ui.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.sajda.app.data.repository.TafsirRepository
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.Ayat
import com.sajda.app.domain.model.CityPreset
import com.sajda.app.domain.model.DailyDua
import com.sajda.app.domain.model.HadithEntry
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.QuranReadingMode
import com.sajda.app.domain.model.Surah
import com.sajda.app.domain.model.TafsirEntry
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.ui.component.ArabicVerseText
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.component.SectionHeader
import com.sajda.app.ui.theme.surfaceContainerLow
import com.sajda.app.service.AdzanService
import com.sajda.app.ui.viewmodel.SettingsViewModel
import com.sajda.app.ui.viewmodel.LocationManagerViewModel
import com.sajda.app.ui.viewmodel.SpiritualContentUiState
import com.sajda.app.util.AppTranslations
import com.sajda.app.util.AdhanSystemHelper
import com.sajda.app.util.DeviceLocationHelper
import com.sajda.app.util.DeviceLocationResult
import com.sajda.app.util.LocationConstants
import com.sajda.app.util.SpiritualContent
import com.sajda.app.util.displayLabelRes
import com.sajda.app.util.displayNameRes
import com.sajda.app.util.localizedDescriptionRes
import com.sajda.app.util.localizedPrayerNameRes
import kotlinx.coroutines.launch

@Composable
fun DailyDuaScreen(
    settings: UserSettings,
    spiritualState: SpiritualContentUiState,
    onRefresh: () -> Unit,
    bookmarkedIds: Set<String>,
    onBack: () -> Unit,
    onToggleBookmark: (String) -> Unit
) {
    OverlayShell(
        title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.daily_dua),
        subtitle = if (spiritualState.isRemote) {
            androidx.compose.ui.res.stringResource(com.sajda.app.R.string.online_spiritual_collections)
        } else {
            androidx.compose.ui.res.stringResource(com.sajda.app.R.string.offline_spiritual_collections)
        },
        onBack = onBack
    ) {
        spiritualState.hadithOfDay?.let { hadith ->
            HadithCard(hadith = hadith, language = settings.appLanguage)
        }
        if (spiritualState.errorMessage != null) {
            SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                Text(
                    text = spiritualState.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.refresh_content),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onRefresh)
                )
            }
        }
        if (spiritualState.sourceLabel.isNotBlank()) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.source) + " | " + spiritualState.sourceLabel.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        spiritualState.duas
            .groupBy { it.category }
            .entries
            .forEach { entry ->
                val category = entry.key
                val duas = entry.value
                Text(
                    text = category.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                duas.forEach { dua ->
                    DuaCard(
                        dua = dua,
                        isBookmarked = bookmarkedIds.contains(dua.id),
                        onToggleBookmark = { onToggleBookmark(dua.id) }
                    )
                }
            }
        if (spiritualState.hadithCategories.isNotEmpty()) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.hadith_categories),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            spiritualState.hadithCategories.forEach { (category, items) ->
                SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    items.take(3).forEach { hadith ->
                        Text(
                            text = "- ${hadith.text}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = listOf(hadith.collection, hadith.reference, hadith.sourceLabel)
                                .filter { it.isNotBlank() }
                                .joinToString(" | "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HadithCard(hadith: HadithEntry, language: AppLanguage) {
    SanctuaryCard {
        Text(
            text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.hadith_of_the_day),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = hadith.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = hadith.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = listOf(
                hadith.category,
                hadith.collection,
                hadith.reference,
                hadith.narrator,
                hadith.sourceLabel
            ).filter { it.isNotBlank() }.joinToString(" | "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DuaCard(
    dua: DailyDua,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit
) {
    SanctuaryCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dua.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onToggleBookmark) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = "Bookmark"
                )
            }
        }
        ArabicVerseText(text = dua.arabic, fontSize = 24)
        Text(
            text = dua.transliteration,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = dua.translation,
            style = MaterialTheme.typography.bodyMedium
        )
        if (dua.sourceLabel.isNotBlank()) {
            Text(
                text = dua.sourceLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TafsirScreen(
    surah: Surah,
    ayat: Ayat,
    appLanguage: AppLanguage,
    tafsirRepository: TafsirRepository,
    onBack: () -> Unit
) {
    val tafsirEntry by produceState(
        initialValue = null as TafsirEntry?,
        surah.number,
        ayat.id,
        appLanguage
    ) {
        value = tafsirRepository.getTafsirForAyat(
            surahNumber = surah.number,
            ayat = ayat,
            appLanguage = appLanguage
        )
    }

    OverlayShell(
        title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.qur_an_tafsir),
        subtitle = "${surah.transliteration} - ${androidx.compose.ui.res.stringResource(com.sajda.app.R.string.verse)} ${ayat.ayatNumber}",
        onBack = onBack
    ) {
        SanctuaryCard {
            ArabicVerseText(text = ayat.textArabic)
            Text(
                text = resolveAyatTranslation(
                    appLanguage = appLanguage,
                    indonesian = ayat.translation,
                    english = ayat.englishTranslation
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (tafsirEntry == null) {
            SanctuaryCard {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.loading_full_tafsir),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                Text(
                    text = tafsirEntry?.sourceName.orEmpty(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!tafsirEntry?.sourceDescription.isNullOrBlank()) {
                    Text(
                        text = tafsirEntry?.sourceDescription.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            SanctuaryCard {
                Text(
                    text = tafsirEntry?.text.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun SmartReminderScreen(
    settings: UserSettings,
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    OverlayShell(
        title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.smart_reminder),
        subtitle = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.qur_an_and_dhikr),
        onBack = onBack
    ) {
        ReminderCard(
            timeLabel = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.time),
            title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.qur_an_reading),
            subtitle = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.remind_me_to_read_the_qur_an_every_day),
            checked = settings.quranReminderEnabled,
            time = settings.quranReminderTime,
            onCheckedChange = { enabled: Boolean -> viewModel.setQuranReminder(enabled) },
            onTimePicked = { time: String -> viewModel.setQuranReminder(settings.quranReminderEnabled, time) }
        )
        ReminderCard(
            timeLabel = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.time),
            title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.morning_dhikr),
            subtitle = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.a_reminder_for_morning_dhikr_after_fajr),
            checked = settings.morningDzikirReminderEnabled,
            time = settings.morningDzikirReminderTime,
            onCheckedChange = { enabled: Boolean -> viewModel.setMorningDzikirReminder(enabled) },
            onTimePicked = { time: String -> viewModel.setMorningDzikirReminder(settings.morningDzikirReminderEnabled, time) }
        )
        ReminderCard(
            timeLabel = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.time),
            title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.evening_dhikr),
            subtitle = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.a_reminder_for_evening_dhikr_before_magh),
            checked = settings.eveningDzikirReminderEnabled,
            time = settings.eveningDzikirReminderTime,
            onCheckedChange = { enabled: Boolean -> viewModel.setEveningDzikirReminder(enabled) },
            onTimePicked = { time: String -> viewModel.setEveningDzikirReminder(settings.eveningDzikirReminderEnabled, time) }
        )
    }
}

@Composable
private fun ReminderCard(
    timeLabel: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    time: String,
    onCheckedChange: (Boolean) -> Unit,
    onTimePicked: (String) -> Unit
) {
    val context = LocalContext.current
    SanctuaryCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            androidx.compose.material3.Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
        Text(
            text = "$timeLabel $time",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { showTimePicker(context, time, onTimePicked) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdhanSettingsScreen(
    settings: UserSettings,
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var systemMessage by remember { mutableStateOf<String?>(null) }
    var systemRefreshKey by remember { mutableIntStateOf(0) }
    val readiness = remember(systemRefreshKey, settings.overrideSilentMode) {
        AdhanSystemHelper.buildReadiness(context)
    }
    val msgNotifSuccess = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.notification_permission_granted_successf)
    val msgNotifDenied = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.notification_permission_was_denied_adhan_2)
    val msgSchedulesRebuilt = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.adhan_schedules_were_rebuilt_from_the_cu)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        systemRefreshKey += 1
        systemMessage = if (granted) {
            msgNotifSuccess
        } else {
            msgNotifDenied
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                systemRefreshKey += 1
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    OverlayShell(
        title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.adhan_diagnostics),
        subtitle = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.offline_prayer_alarms),
        onBack = onBack
    ) {
        SettingToggleCard(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.automatic_adhan_2), settings.adzanEnabled) { viewModel.setAdzanEnabled(it) }
        SettingToggleCard(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.override_silent_mode_2), settings.overrideSilentMode) { viewModel.setOverrideSilentMode(it) }
        SettingToggleCard(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.vibration), settings.vibrationEnabled) { viewModel.setVibrationEnabled(it) }

        SanctuaryCard {
            SectionHeader(
                eyebrow = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.adhan_audio),
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.set_regular_and_fajr_sounds_separately)
            )
            Text(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.regular_adhan),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.sajda.app.domain.model.AdhanStyle.entries.forEach { style ->
                    ChoiceChip(
                        label = style.title,
                        selected = settings.adzanSound == style,
                        onClick = { viewModel.setAdzanSound(style) }
                    )
                }
            }
            Text(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.this_sound_is_used_for_dhuhr_asr_maghrib),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.fajr_adhan),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.sajda.app.domain.model.AdhanStyle.entries.forEach { style ->
                    ChoiceChip(
                        label = style.title,
                        selected = settings.fajrAdzanSound == style,
                        onClick = { viewModel.setFajrAdzanSound(style) }
                    )
                }
            }
            Text(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.this_sound_is_dedicated_to_fajr_so_it_ca),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SanctuaryCard {
            SectionHeader(
                eyebrow = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.adhan_system_status),
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.next_alarm_and_history)
            )
            Text(
                text = if (settings.nextScheduledPrayer.isNotBlank() && settings.nextScheduledAt.isNotBlank()) {
                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.next_localizedprayername_settings_nextsc)
                } else {
                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.there_is_no_saved_upcoming_alarm_yet_try)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (settings.lastAdhanPrayer.isNotBlank()) {
                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.latest_localizedprayername_settings_last)
                } else {
                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.there_is_no_adhan_history_yet_use_the_ma)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.default_snooze),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 10, 15, 20).forEach { minutes ->
                    ChoiceChip(
                        label = "${minutes}m",
                        selected = settings.adhanSnoozeMinutes == minutes,
                        onClick = { viewModel.setAdhanSnoozeMinutes(minutes) }
                    )
                }
            }
        }
        SanctuaryCard {
            SectionHeader(
                eyebrow = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.system_readiness),
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.adhan_diagnostics_2)
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.notification_permission),
                ready = readiness.notificationPermissionGranted && readiness.appNotificationsEnabled
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.adhan_channel_enabled),
                ready = readiness.adhanChannelEnabled
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.exact_alarm),
                ready = readiness.exactAlarmGranted
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.battery_optimization_2),
                ready = readiness.batteryOptimizationIgnored
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.location_permission),
                ready = readiness.locationPermissionGranted,
                message = if (readiness.locationPermissionGranted) null else {
                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.location_permission_is_required_for_auto)
                }
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.device_silent_mode),
                ready = !readiness.silentModeActive || settings.overrideSilentMode,
                message = if (readiness.silentModeActive && !settings.overrideSilentMode) {
                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.the_phone_is_in_silent_vibrate_mode_and)
                } else {
                    null
                }
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.alarm_volume),
                ready = readiness.alarmVolumeLevel > 0,
                message = if (readiness.alarmVolumeLevel > 0) {
                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.current_alarm_level_readiness_alarmvolum)
                } else {
                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.alarm_volume_is_0_so_the_adhan_sound_wil)
                }
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChoiceChip(
                    label = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !readiness.notificationPermissionGranted) {
                        androidx.compose.ui.res.stringResource(com.sajda.app.R.string.allow_notifications)
                    } else {
                        androidx.compose.ui.res.stringResource(com.sajda.app.R.string.notification_settings)
                    },
                    selected = readiness.notificationPermissionGranted && readiness.appNotificationsEnabled,
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !readiness.notificationPermissionGranted) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            AdhanSystemHelper.openNotificationSettings(context)
                        }
                    }
                )
                ChoiceChip(
                    label = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.exact_alarm),
                    selected = readiness.exactAlarmGranted,
                    onClick = { AdhanSystemHelper.openExactAlarmSettings(context) }
                )
                ChoiceChip(
                    label = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.battery),
                    selected = readiness.batteryOptimizationIgnored,
                    onClick = { AdhanSystemHelper.openBatteryOptimizationSettings(context) }
                )
                ChoiceChip(
                    label = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.reschedule),
                    selected = false,
                    onClick = {
                        viewModel.refreshPrayerSchedule()
                        systemRefreshKey += 1
                        systemMessage = msgSchedulesRebuilt
                    }
                )
            }
            systemMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        SanctuaryCard {
            SectionHeader(
                eyebrow = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.manual_test),
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.test_every_prayer_adhan)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PrayerName.entries.forEach { prayerName ->
                    ChoiceChip(
                        label = androidx.compose.ui.res.stringResource(prayerName.displayNameRes()),
                        selected = false,
                        onClick = { AdzanService.play(context, prayerName.label) }
                    )
                }
                ChoiceChip(
                    label = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.stop),
                    selected = false,
                    onClick = { AdzanService.stop(context) }
                )
            }
            Text(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.fajr_uses_the_dedicated_fajr_adhan_audio),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SanctuaryCard {
            SectionHeader(
                eyebrow = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.str_7_day_history),
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.recent_adhan_events)
            )
            if (settings.adhanHistory.isEmpty()) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.there_is_no_adhan_history_yet_run_a_manu),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                settings.adhanHistory.take(12).forEach { entry ->
                    Text(
                        text = "${localizedPrayerNameRes(entry.prayerName)?.let { androidx.compose.ui.res.stringResource(it) } ?: entry.prayerName} | ${entry.status}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = listOf(entry.occurredAt, entry.details)
                            .filter { it.isNotBlank() }
                            .joinToString(" | "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        SettingToggleCard(androidx.compose.ui.res.stringResource(PrayerName.FAJR.displayNameRes()), settings.fajrAdzanEnabled) { viewModel.setPrayerEnabled(PrayerName.FAJR, it) }
        SettingToggleCard(androidx.compose.ui.res.stringResource(PrayerName.DHUHR.displayNameRes()), settings.dhuhrAdzanEnabled) { viewModel.setPrayerEnabled(PrayerName.DHUHR, it) }
        SettingToggleCard(androidx.compose.ui.res.stringResource(PrayerName.ASR.displayNameRes()), settings.asrAdzanEnabled) { viewModel.setPrayerEnabled(PrayerName.ASR, it) }
        SettingToggleCard(androidx.compose.ui.res.stringResource(PrayerName.MAGHRIB.displayNameRes()), settings.maghribAdzanEnabled) { viewModel.setPrayerEnabled(PrayerName.MAGHRIB, it) }
        SettingToggleCard(androidx.compose.ui.res.stringResource(PrayerName.ISHA.displayNameRes()), settings.ishaAdzanEnabled) { viewModel.setPrayerEnabled(PrayerName.ISHA, it) }
    }
}

@Composable
private fun SystemStatusLine(
    appLanguage: AppLanguage,
    title: String,
    ready: Boolean,
    message: String? = null
) {
    Text(
        text = if (ready) {
            androidx.compose.ui.res.stringResource(com.sajda.app.R.string.ready_title)
        } else {
            androidx.compose.ui.res.stringResource(com.sajda.app.R.string.check_title)
        },
        style = MaterialTheme.typography.bodyMedium,
        color = if (ready) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    )
    if (message != null) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationSettingsScreen(
    settings: UserSettings,
    viewModel: LocationManagerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isResolvingLocation by remember { mutableStateOf(false) }
    var locationStatus by remember { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val normalizedQuery = query.trim()
    val filteredCities = remember(query) {
        LocationConstants.cityPresets
            .filter { city -> city.matches(normalizedQuery) }
            .sortedWith(compareBy<CityPreset> { it.matchScore(normalizedQuery) }.thenBy { it.displayName })
    }
    val favoriteCities = remember(settings.favoriteLocationNames) {
        LocationConstants.cityPresets.filter { city ->
            city.displayName in settings.favoriteLocationNames || city.name in settings.favoriteLocationNames
        }.distinctBy { it.displayName }
    }

    fun CityPreset.isSelectedLocation(): Boolean =
        settings.locationName == displayName || settings.locationName == name

    fun CityPreset.isFavoriteLocation(): Boolean =
        displayName in settings.favoriteLocationNames || name in settings.favoriteLocationNames

    val msgLocationUpdated = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.active_location_was_updated_to_result_lo)
    val msgLocationDenied = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.location_permission_was_denied_enable_gp)
    val msgAutoGpsOff = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.auto_gps_mode_was_turned_off)

    fun refreshFromDevice(markAuto: Boolean) {
        scope.launch {
            isResolvingLocation = true
            when (val result = DeviceLocationHelper.getCurrentLocation(context)) {
                is DeviceLocationResult.Success -> {
                    viewModel.setCurrentLocation(
                        locationName = result.location.label,
                        latitude = result.location.latitude,
                        longitude = result.location.longitude,
                        automatic = markAuto
                    )
                    locationStatus = msgLocationUpdated
                }

                is DeviceLocationResult.Error -> {
                    if (markAuto) {
                        viewModel.setAutoLocation(false)
                    }
                    locationStatus = result.message
                }
            }
            isResolvingLocation = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.any { it.value }) {
            refreshFromDevice(markAuto = true)
        } else {
            scope.launch { viewModel.setAutoLocation(false) }
            locationStatus = msgLocationDenied
        }
    }

    OverlayShell(
        title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.location_2),
        subtitle = settings.locationName,
        onBack = onBack
    ) {
        SettingToggleCard(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.auto_gps), settings.autoLocation) { enabled ->
            if (!enabled) {
                viewModel.setAutoLocation(false)
                locationStatus = msgAutoGpsOff
            } else if (DeviceLocationHelper.hasLocationPermission(context)) {
                refreshFromDevice(markAuto = true)
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
        SanctuaryCard {
            SectionHeader(
                eyebrow = "GPS",
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.device_location)
            )
            ChoiceChip(
                label = if (isResolvingLocation) {
                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.getting_location)
                } else {
                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.use_current_location)
                },
                selected = settings.autoLocation,
                onClick = {
                    if (!isResolvingLocation) {
                        if (DeviceLocationHelper.hasLocationPermission(context)) {
                            refreshFromDevice(markAuto = true)
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                }
            )
            if (isResolvingLocation) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Text(
                text = settings.locationName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            locationStatus?.let { status ->
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        SanctuaryCard {
            SectionHeader(
                eyebrow = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.manual),
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.search_region)
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.search_province_city_or_regency)) }
            )
            val locationSavedMsg = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.city_displayname_is_now_used_for_prayer)
            filteredCities.take(if (normalizedQuery.isBlank()) 24 else 40).forEach { city ->
                LocationSelectionRow(
                    appLanguage = settings.appLanguage,
                    city = city,
                    isSelected = city.isSelectedLocation(),
                    isFavorite = city.isFavoriteLocation(),
                    onUse = {
                        viewModel.setLocation(city)
                        query = city.displayName
                        locationStatus = locationSavedMsg
                    },
                    onToggleFavorite = { viewModel.toggleFavoriteLocation(city.displayName) }
                )
            }
            if (filteredCities.isEmpty()) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.no_matching_results_yet_try_a_fuller_pro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (favoriteCities.isNotEmpty()) {
            SanctuaryCard {
                SectionHeader(
                    eyebrow = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.favorites),
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.favorite_locations)
                )
                favoriteCities.forEach { city ->
                    val locationSavedMsgFav = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.city_displayname_is_now_used_for_prayer)
                    LocationSelectionRow(
                        appLanguage = settings.appLanguage,
                        city = city,
                        isSelected = city.isSelectedLocation(),
                        isFavorite = true,
                        onUse = {
                            viewModel.setLocation(city)
                            query = city.displayName
                            locationStatus = locationSavedMsgFav
                        },
                        onToggleFavorite = { viewModel.toggleFavoriteLocation(city.displayName) }
                    )
                }
            }
        }
        SanctuaryCard {
            SectionHeader(
                eyebrow = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.calculation),
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.prayer_time_method)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                com.sajda.app.domain.model.PrayerCalculationMethod.entries.forEach { method ->
                    ChoiceChip(
                        label = method.label,
                        selected = settings.prayerCalculationMethod == method,
                        onClick = { viewModel.setPrayerCalculationMethod(method) }
                    )
                }
            }
            Text(
                text = androidx.compose.ui.res.stringResource(settings.prayerCalculationMethod.localizedDescriptionRes()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                com.sajda.app.domain.model.AsrMadhhab.entries.forEach { madhhab ->
                    ChoiceChip(
                        label = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.asr_madhhab_label),
                        selected = settings.asrMadhhab == madhhab,
                        onClick = { viewModel.setAsrMadhhab(madhhab) }
                    )
                }
            }
            Text(
                text = androidx.compose.ui.res.stringResource(settings.asrMadhhab.localizedDescriptionRes()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguageSettingsScreen(
    settings: UserSettings,
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    OverlayShell(
        title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.language),
        subtitle = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.app_and_qur_an_reading),
        onBack = onBack
    ) {
        SanctuaryCard {
            SectionHeader(
                eyebrow = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.app_language),
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.choose_the_primary_language)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { language ->
                    ChoiceChip(
                        label = androidx.compose.ui.res.stringResource(language.displayNameRes()),
                        selected = settings.appLanguage == language,
                        onClick = { viewModel.setAppLanguage(language) }
                    )
                }
            }
        }
        SanctuaryCard {
            SectionHeader(
                eyebrow = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.qur_an_reading_mode),
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.control_verse_layout)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuranReadingMode.entries.forEach { mode ->
                    ChoiceChip(
                        label = androidx.compose.ui.res.stringResource(mode.displayLabelRes()),
                        selected = settings.quranReadingMode == mode,
                        onClick = { viewModel.setQuranReadingMode(mode) }
                    )
                }
            }
        }
        SettingToggleCard(
            androidx.compose.ui.res.stringResource(com.sajda.app.R.string.show_transliteration),
            settings.showTransliteration
        ) { viewModel.setShowTransliteration(it) }
        SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.preview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ArabicVerseText(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.preview_ayah_arabic_fixed),
                fontSize = 24
            )
            Text(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.indeed_with_hardship_comes_ease),
                style = MaterialTheme.typography.bodyLarge
            )
            if (settings.showTransliteration && settings.quranReadingMode != QuranReadingMode.ARABIC_ONLY) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.preview_ayah_transliteration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.language_preview_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LocationSelectionRow(
    appLanguage: AppLanguage,
    city: CityPreset,
    isSelected: Boolean,
    isFavorite: Boolean,
    onUse: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = city.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = city.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = if (isFavorite) {
                            androidx.compose.ui.res.stringResource(com.sajda.app.R.string.remove_favorite)
                        } else {
                            androidx.compose.ui.res.stringResource(com.sajda.app.R.string.save_favorite)
                        },
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ChoiceChip(
                    label = if (isSelected) androidx.compose.ui.res.stringResource(com.sajda.app.R.string.active) else androidx.compose.ui.res.stringResource(com.sajda.app.R.string.use),
                    selected = isSelected,
                    onClick = onUse
                )
            }
        }
    }
}

private fun resolveAyatTranslation(
    appLanguage: com.sajda.app.domain.model.AppLanguage,
    indonesian: String,
    english: String
): String {
    val fallbackEnglish = english.ifBlank { indonesian }
    return when (appLanguage) {
        com.sajda.app.domain.model.AppLanguage.INDONESIAN -> indonesian
        com.sajda.app.domain.model.AppLanguage.ENGLISH -> fallbackEnglish
        else -> AppTranslations.translate(fallbackEnglish, appLanguage)
    }
}
