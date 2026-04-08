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
import com.sajda.app.ui.viewmodel.SpiritualContentUiState
import com.sajda.app.util.AdhanSystemHelper
import com.sajda.app.util.DeviceLocationHelper
import com.sajda.app.util.DeviceLocationResult
import com.sajda.app.util.LocationConstants
import com.sajda.app.util.SpiritualContent
import com.sajda.app.util.displayLabel
import com.sajda.app.util.displayName
import com.sajda.app.util.isEnglish
import com.sajda.app.util.localizedDescription
import com.sajda.app.util.localizedPrayerName
import com.sajda.app.util.pick
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
        title = settings.pick("Doa Harian", "Daily Dua"),
        subtitle = if (spiritualState.isRemote) {
            settings.pick("Koleksi spiritual online", "Online spiritual collections")
        } else {
            settings.pick("Koleksi spiritual offline", "Offline spiritual collections")
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
                    text = if (settings.appLanguage == AppLanguage.ENGLISH) "Refresh content" else "Segarkan konten",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onRefresh)
                )
            }
        }
        if (spiritualState.sourceLabel.isNotBlank()) {
            Text(
                text = settings.pick("SUMBER", "SOURCE") + " | " + spiritualState.sourceLabel.uppercase(),
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
                text = if (settings.appLanguage == AppLanguage.ENGLISH) "HADITH CATEGORIES" else "KATEGORI HADITS",
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
            text = language.pick("HADITS HARI INI", "HADITH OF THE DAY"),
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
        title = appLanguage.pick("Tafsir Al-Qur'an", "Qur'an Tafsir"),
        subtitle = "${surah.transliteration} - ${appLanguage.pick("Ayat", "Verse")} ${ayat.ayatNumber}",
        onBack = onBack
    ) {
        SanctuaryCard {
            ArabicVerseText(text = ayat.textArabic)
            Text(
                text = appLanguage.pick(ayat.translation, ayat.englishTranslation.ifBlank { ayat.translation }),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (tafsirEntry == null) {
            SanctuaryCard {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = appLanguage.pick("Sedang memuat tafsir penuh...", "Loading full tafsir..."),
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
        title = settings.pick("Reminder Pintar", "Smart Reminder"),
        subtitle = settings.pick("Qur'an dan dzikir", "Qur'an and dhikr"),
        onBack = onBack
    ) {
        ReminderCard(
            timeLabel = settings.pick("Jam", "Time"),
            title = settings.pick("Baca Qur'an", "Qur'an reading"),
            subtitle = settings.pick("Ingatkan untuk membaca Al-Qur'an setiap hari.", "Remind me to read the Qur'an every day."),
            checked = settings.quranReminderEnabled,
            time = settings.quranReminderTime,
            onCheckedChange = { enabled: Boolean -> viewModel.setQuranReminder(enabled) },
            onTimePicked = { time: String -> viewModel.setQuranReminder(settings.quranReminderEnabled, time) }
        )
        ReminderCard(
            timeLabel = settings.pick("Jam", "Time"),
            title = settings.pick("Dzikir Pagi", "Morning dhikr"),
            subtitle = settings.pick("Pengingat wirid dan dzikir setelah Subuh.", "A reminder for morning dhikr after Fajr."),
            checked = settings.morningDzikirReminderEnabled,
            time = settings.morningDzikirReminderTime,
            onCheckedChange = { enabled: Boolean -> viewModel.setMorningDzikirReminder(enabled) },
            onTimePicked = { time: String -> viewModel.setMorningDzikirReminder(settings.morningDzikirReminderEnabled, time) }
        )
        ReminderCard(
            timeLabel = settings.pick("Jam", "Time"),
            title = settings.pick("Dzikir Sore", "Evening dhikr"),
            subtitle = settings.pick("Pengingat dzikir sore menjelang Maghrib.", "A reminder for evening dhikr before Maghrib."),
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
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        systemRefreshKey += 1
        systemMessage = if (granted) {
            settings.pick(
                "Izin notifikasi berhasil diberikan.",
                "Notification permission granted successfully."
            )
        } else {
            settings.pick(
                "Izin notifikasi ditolak. Notifikasi adzan bisa tidak muncul di Android 13+.",
                "Notification permission was denied. Adhan alerts may not appear on Android 13+."
            )
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

    val isEnglish = settings.appLanguage.isEnglish()
    val helpChecklist = remember(settings.appLanguage) {
        AdhanSystemHelper.helpChecklist().map { item ->
            if (isEnglish) {
                when (item) {
                    "Pastikan notifikasi aplikasi diizinkan." -> "Make sure app notifications are allowed."
                    "Pastikan izin exact alarm aktif." -> "Make sure exact alarm permission is enabled."
                    "Lepaskan pembatasan baterai untuk NurApp." -> "Remove battery restrictions for NurApp."
                    "Naikkan volume alarm perangkat di atas nol." -> "Keep the device alarm volume above zero."
                    "Aktifkan Override silent mode jika Anda ingin adzan tetap bersuara saat HP silent." -> "Enable Override silent mode if you want adhan to sound while the phone is silent."
                    "Buka aplikasi minimal sekali setelah reboot atau update bila vendor sangat agresif." -> "Open the app at least once after reboot or update if your device vendor is very aggressive."
                    else -> item
                }
            } else {
                item
            }
        }
    }
    val vendorTips = remember(systemRefreshKey, settings.appLanguage) { AdhanSystemHelper.vendorTips(context) }

    OverlayShell(
        title = settings.pick("Pengaturan Adzan", "Adhan Settings"),
        subtitle = settings.pick("Alarm sholat offline", "Offline prayer alarms"),
        onBack = onBack
    ) {
        SettingToggleCard(settings.pick("Adzan otomatis", "Automatic adhan"), settings.adzanEnabled) { viewModel.setAdzanEnabled(it) }
        SettingToggleCard(settings.pick("Override mode senyap", "Override silent mode"), settings.overrideSilentMode) { viewModel.setOverrideSilentMode(it) }
        SettingToggleCard(settings.pick("Getaran", "Vibration"), settings.vibrationEnabled) { viewModel.setVibrationEnabled(it) }

        SanctuaryCard {
            SectionHeader(
                eyebrow = settings.pick("Suara Adzan", "Adhan Audio"),
                title = settings.pick("Atur suara reguler dan Subuh secara terpisah", "Set regular and Fajr sounds separately")
            )
            Text(
                text = settings.pick("Adzan reguler", "Regular adhan"),
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
                text = settings.pick(
                    "Suara ini dipakai untuk Dzuhur, Ashar, Maghrib, dan Isya.",
                    "This sound is used for Dhuhr, Asr, Maghrib, and Isha."
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = settings.pick("Adzan Subuh", "Fajr adhan"),
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
                text = settings.pick(
                    "Suara ini khusus untuk Subuh agar bisa dibedakan dari jadwal lain.",
                    "This sound is dedicated to Fajr so it can feel different from the other prayers."
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SanctuaryCard {
            SectionHeader(
                eyebrow = settings.pick("Status Sistem Adzan", "Adhan System Status"),
                title = settings.pick("Alarm berikutnya dan riwayat", "Next alarm and history")
            )
            Text(
                text = if (settings.nextScheduledPrayer.isNotBlank() && settings.nextScheduledAt.isNotBlank()) {
                    settings.pick(
                        "Berikutnya: ${localizedPrayerName(settings.nextScheduledPrayer, settings.appLanguage)} | ${settings.nextScheduledAt}",
                        "Next: ${localizedPrayerName(settings.nextScheduledPrayer, settings.appLanguage)} | ${settings.nextScheduledAt}"
                    )
                } else {
                    settings.pick(
                        "Belum ada alarm berikutnya yang tersimpan. Coba jadwalkan ulang.",
                        "There is no saved upcoming alarm yet. Try rebuilding the schedule."
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (settings.lastAdhanPrayer.isNotBlank()) {
                    settings.pick(
                        "Terakhir: ${localizedPrayerName(settings.lastAdhanPrayer, settings.appLanguage)} | ${settings.lastAdhanStatus} | ${settings.lastAdhanAt}",
                        "Latest: ${localizedPrayerName(settings.lastAdhanPrayer, settings.appLanguage)} | ${settings.lastAdhanStatus} | ${settings.lastAdhanAt}"
                    )
                } else {
                    settings.pick(
                        "Belum ada riwayat adzan. Tes manual bisa dipakai untuk memastikan alurnya.",
                        "There is no adhan history yet. Use the manual test to verify the flow."
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = settings.pick("Snooze default", "Default snooze"),
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
                eyebrow = settings.pick("Kesiapan Sistem", "System Readiness"),
                title = settings.pick("Diagnosa adzan", "Adhan diagnostics")
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = settings.pick("Izin notifikasi", "Notification permission"),
                ready = readiness.notificationPermissionGranted && readiness.appNotificationsEnabled
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = settings.pick("Channel adzan aktif", "Adhan channel enabled"),
                ready = readiness.adhanChannelEnabled
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = settings.pick("Exact alarm", "Exact alarm"),
                ready = readiness.exactAlarmGranted
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = settings.pick("Optimasi baterai", "Battery optimization"),
                ready = readiness.batteryOptimizationIgnored
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = settings.pick("Mode senyap perangkat", "Device silent mode"),
                ready = !readiness.silentModeActive || settings.overrideSilentMode,
                message = if (readiness.silentModeActive && !settings.overrideSilentMode) {
                    settings.pick(
                        "HP sedang silent/vibrate dan Override silent mode masih mati.",
                        "The phone is in silent/vibrate mode and Override silent mode is still off."
                    )
                } else {
                    null
                }
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = settings.pick("Volume alarm", "Alarm volume"),
                ready = readiness.alarmVolumeLevel > 0,
                message = if (readiness.alarmVolumeLevel > 0) {
                    settings.pick(
                        "Level alarm saat ini: ${readiness.alarmVolumeLevel}",
                        "Current alarm level: ${readiness.alarmVolumeLevel}"
                    )
                } else {
                    settings.pick(
                        "Volume alarm sedang 0, jadi suara adzan tidak akan terdengar.",
                        "Alarm volume is 0, so the adhan sound will not be audible."
                    )
                }
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChoiceChip(
                    label = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !readiness.notificationPermissionGranted) {
                        settings.pick("Izinkan notifikasi", "Allow notifications")
                    } else {
                        settings.pick("Pengaturan notif", "Notification settings")
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
                    label = settings.pick("Exact alarm", "Exact alarm"),
                    selected = readiness.exactAlarmGranted,
                    onClick = { AdhanSystemHelper.openExactAlarmSettings(context) }
                )
                ChoiceChip(
                    label = settings.pick("Baterai", "Battery"),
                    selected = readiness.batteryOptimizationIgnored,
                    onClick = { AdhanSystemHelper.openBatteryOptimizationSettings(context) }
                )
                ChoiceChip(
                    label = settings.pick("Jadwalkan ulang", "Reschedule"),
                    selected = false,
                    onClick = {
                        viewModel.refreshPrayerSchedule()
                        systemRefreshKey += 1
                        systemMessage = settings.pick(
                            "Jadwal adzan dijadwalkan ulang dari pengaturan sekarang.",
                            "Adhan schedules were rebuilt from the current settings."
                        )
                    }
                )
            }
            Text(
                text = settings.pick(
                    "Agar adzan bekerja seperti aplikasi adzan pada umumnya, minimal aktifkan notifikasi, exact alarm, dan lepaskan pembatasan baterai.",
                    "To behave like a real adhan app, at minimum enable notifications, exact alarms, and remove battery restrictions."
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                eyebrow = settings.pick("Tes Manual", "Manual Test"),
                title = settings.pick("Coba semua waktu adzan", "Test every prayer adhan")
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PrayerName.entries.forEach { prayerName ->
                    ChoiceChip(
                        label = prayerName.displayName(settings.appLanguage),
                        selected = false,
                        onClick = { AdzanService.play(context, prayerName.label) }
                    )
                }
                ChoiceChip(
                    label = settings.pick("Stop", "Stop"),
                    selected = false,
                    onClick = { AdzanService.stop(context) }
                )
            }
            Text(
                text = settings.pick(
                    "Subuh memakai audio khusus Subuh. Dzuhur, Ashar, Maghrib, dan Isya memakai audio adzan reguler.",
                    "Fajr uses the dedicated Fajr adhan audio. Dhuhr, Asr, Maghrib, and Isha use the regular adhan audio."
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SanctuaryCard {
            SectionHeader(
                eyebrow = settings.pick("Bantuan cepat", "Quick help"),
                title = settings.pick("Kenapa adzan tidak berbunyi?", "Why is adhan not playing?")
            )
            helpChecklist.forEach { item ->
                Text(
                    text = "- $item",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        SanctuaryCard {
            SectionHeader(
                eyebrow = settings.pick("Tips perangkat", "Device tips"),
                title = settings.pick("Panduan per merek HP", "Vendor-specific guidance")
            )
            vendorTips.forEach { tip ->
                Text(
                    text = tip.vendor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEnglish) {
                        when (tip.title) {
                            "Nonaktifkan pembatasan MIUI" -> "Disable MIUI restrictions"
                            "Izinkan berjalan di background" -> "Allow background running"
                            "Aktifkan autostart dan baterai tanpa batas" -> "Enable auto-start and unrestricted battery"
                            "Keluarkan dari sleeping apps" -> "Remove from sleeping apps"
                            "Izinkan startup manager" -> "Allow startup manager"
                            else -> tip.title
                        }
                    } else {
                        tip.title
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                tip.steps.forEach { step ->
                    Text(
                        text = "- $step",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        SanctuaryCard {
            SectionHeader(
                eyebrow = settings.pick("Riwayat 7 hari", "7-day history"),
                title = settings.pick("Jejak adzan terbaru", "Recent adhan events")
            )
            if (settings.adhanHistory.isEmpty()) {
                Text(
                    text = settings.pick(
                        "Belum ada histori adzan. Jalankan tes manual atau tunggu waktu sholat berikutnya.",
                        "There is no adhan history yet. Run a manual test or wait for the next prayer time."
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                settings.adhanHistory.take(12).forEach { entry ->
                    Text(
                        text = "${localizedPrayerName(entry.prayerName, settings.appLanguage)} | ${entry.status}",
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
        SettingToggleCard(PrayerName.FAJR.displayName(settings.appLanguage), settings.fajrAdzanEnabled) { viewModel.setPrayerEnabled(PrayerName.FAJR, it) }
        SettingToggleCard(PrayerName.DHUHR.displayName(settings.appLanguage), settings.dhuhrAdzanEnabled) { viewModel.setPrayerEnabled(PrayerName.DHUHR, it) }
        SettingToggleCard(PrayerName.ASR.displayName(settings.appLanguage), settings.asrAdzanEnabled) { viewModel.setPrayerEnabled(PrayerName.ASR, it) }
        SettingToggleCard(PrayerName.MAGHRIB.displayName(settings.appLanguage), settings.maghribAdzanEnabled) { viewModel.setPrayerEnabled(PrayerName.MAGHRIB, it) }
        SettingToggleCard(PrayerName.ISHA.displayName(settings.appLanguage), settings.ishaAdzanEnabled) { viewModel.setPrayerEnabled(PrayerName.ISHA, it) }
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
            appLanguage.pick("SIAP | $title", "READY | $title")
        } else {
            appLanguage.pick("CEK | $title", "CHECK | $title")
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
fun AppearanceSettingsScreen(
    settings: UserSettings,
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    OverlayShell(
        title = settings.pick("Tampilan", "Appearance"),
        subtitle = settings.pick("Tampilan NurApp", "NurApp appearance"),
        onBack = onBack
    ) {
        SettingToggleCard(settings.pick("Mode gelap", "Dark mode"), settings.darkMode) { viewModel.setDarkMode(it) }
        SettingToggleCard(settings.pick("Mode malam", "Night mode"), settings.nightMode) { viewModel.setNightMode(it) }
        SettingToggleCard(settings.pick("Mode fokus bawaan", "Default focus mode"), settings.focusMode) { viewModel.setFocusMode(it) }
        SanctuaryCard {
            SectionHeader(
                eyebrow = settings.pick("Tata Letak Baca", "Reading layout"),
                title = settings.pick("Kenyamanan tilawah", "Reading comfort")
            )
            Text(
                text = settings.pick(
                    "Sesuaikan ukuran teks dan jarak antar ayat agar sesi baca lebih lembut di mata dan nyaman untuk waktu yang lama.",
                    "Adjust text sizes and verse spacing so long reading sessions stay calm and comfortable for your eyes."
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = settings.pick("Ukuran Arab", "Arabic size"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(26, 30, 34, 38).forEach { size ->
                    ChoiceChip(
                        label = "${size}sp",
                        selected = settings.arabicFontSize == size,
                        onClick = { viewModel.setArabicFontSize(size) }
                    )
                }
            }
            Text(
                text = settings.pick("Ukuran terjemahan", "Translation size"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(14, 16, 18, 20).forEach { size ->
                    ChoiceChip(
                        label = "${size}sp",
                        selected = settings.translationFontSize == size,
                        onClick = { viewModel.setTranslationFontSize(size) }
                    )
                }
            }
            Text(
                text = settings.pick("Jarak ayat", "Verse spacing"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(14, 18, 22, 26).forEach { spacing ->
                    ChoiceChip(
                        label = "${spacing}dp",
                        selected = settings.verseSpacing == spacing,
                        onClick = { viewModel.setVerseSpacing(spacing) }
                    )
                }
            }
            ArabicVerseText(
                text = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا",
                fontSize = settings.arabicFontSize
            )
            Text(
                text = settings.pick(
                    "Sesungguhnya bersama kesulitan ada kemudahan.",
                    "Indeed, with hardship comes ease."
                ),
                fontSize = settings.translationFontSize.sp,
                lineHeight = (settings.translationFontSize + 8).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
            Text(
                text = settings.pick(
                    "NurApp memakai palet emerald lembut dengan layered surfaces agar pengalaman ibadah terasa tenang, ringan, dan fokus.",
                    "NurApp uses a soft emerald palette with layered surfaces so worship feels calm, light, and focused."
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationSettingsScreen(
    settings: UserSettings,
    viewModel: SettingsViewModel,
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
                    locationStatus = settings.pick(
                        "Lokasi aktif diperbarui ke ${result.location.label}.",
                        "Active location was updated to ${result.location.label}."
                    )
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
            locationStatus = settings.pick(
                "Izin lokasi ditolak. Aktifkan izin GPS agar jadwal sholat menyesuaikan lokasi Anda.",
                "Location permission was denied. Enable GPS permission so prayer times can match your location."
            )
        }
    }

    OverlayShell(
        title = settings.pick("Lokasi", "Location"),
        subtitle = settings.locationName,
        onBack = onBack
    ) {
        SettingToggleCard(settings.pick("GPS otomatis", "Auto GPS"), settings.autoLocation) { enabled ->
            if (!enabled) {
                viewModel.setAutoLocation(false)
                locationStatus = settings.pick("Mode GPS otomatis dimatikan.", "Auto GPS mode was turned off.")
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
                title = settings.pick("Lokasi perangkat", "Device location")
            )
            ChoiceChip(
                label = if (isResolvingLocation) {
                    settings.pick("Mengambil lokasi...", "Getting location...")
                } else {
                    settings.pick("Gunakan lokasi saat ini", "Use current location")
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
                eyebrow = settings.pick("Manual", "Manual"),
                title = settings.pick("Cari wilayah", "Search region")
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(settings.pick("Cari provinsi, kota, atau kabupaten", "Search province, city, or regency")) }
            )
            filteredCities.take(if (normalizedQuery.isBlank()) 24 else 40).forEach { city ->
                LocationSelectionRow(
                    appLanguage = settings.appLanguage,
                    city = city,
                    isSelected = city.isSelectedLocation(),
                    isFavorite = city.isFavoriteLocation(),
                    onUse = {
                        viewModel.setLocation(city)
                        query = city.displayName
                        locationStatus = settings.pick(
                            "${city.displayName} dipakai untuk jadwal sholat.",
                            "${city.displayName} is now used for prayer times."
                        )
                    },
                    onToggleFavorite = { viewModel.toggleFavoriteLocation(city.displayName) }
                )
            }
            if (filteredCities.isEmpty()) {
                Text(
                    text = settings.pick(
                        "Belum ada hasil yang cocok. Coba pakai nama provinsi, kota, atau kabupaten yang lebih lengkap.",
                        "No matching results yet. Try a fuller province, city, or regency name."
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (favoriteCities.isNotEmpty()) {
            SanctuaryCard {
                SectionHeader(
                    eyebrow = settings.pick("Favorit", "Favorites"),
                    title = settings.pick("Lokasi favorit", "Favorite locations")
                )
                favoriteCities.forEach { city ->
                    LocationSelectionRow(
                        appLanguage = settings.appLanguage,
                        city = city,
                        isSelected = city.isSelectedLocation(),
                        isFavorite = true,
                        onUse = {
                            viewModel.setLocation(city)
                            query = city.displayName
                            locationStatus = settings.pick(
                                "${city.displayName} dipakai untuk jadwal sholat.",
                                "${city.displayName} is now used for prayer times."
                            )
                        },
                        onToggleFavorite = { viewModel.toggleFavoriteLocation(city.displayName) }
                    )
                }
            }
        }
        SanctuaryCard {
            SectionHeader(
                eyebrow = settings.pick("Perhitungan", "Calculation"),
                title = settings.pick("Metode waktu sholat", "Prayer time method")
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
                text = settings.prayerCalculationMethod.localizedDescription(settings.appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                com.sajda.app.domain.model.AsrMadhhab.entries.forEach { madhhab ->
                    ChoiceChip(
                        label = settings.pick("Asar ${madhhab.label}", "Asr ${madhhab.label}"),
                        selected = settings.asrMadhhab == madhhab,
                        onClick = { viewModel.setAsrMadhhab(madhhab) }
                    )
                }
            }
            Text(
                text = settings.asrMadhhab.localizedDescription(settings.appLanguage),
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
    val isEnglish = settings.appLanguage.isEnglish()
    OverlayShell(
        title = settings.pick("Bahasa", "Language"),
        subtitle = settings.pick("Aplikasi dan mode baca Qur'an", "App and Qur'an reading"),
        onBack = onBack
    ) {
        SanctuaryCard {
            SectionHeader(
                eyebrow = settings.pick("Bahasa Aplikasi", "App Language"),
                title = settings.pick("Pilih bahasa utama", "Choose the primary language")
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { language ->
                    ChoiceChip(
                        label = language.displayName(),
                        selected = settings.appLanguage == language,
                        onClick = { viewModel.setAppLanguage(language) }
                    )
                }
            }
        }
        SanctuaryCard {
            SectionHeader(
                eyebrow = settings.pick("Mode Baca Qur'an", "Qur'an reading mode"),
                title = settings.pick("Atur tampilan ayat", "Control verse layout")
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuranReadingMode.entries.forEach { mode ->
                    ChoiceChip(
                        label = mode.displayLabel(settings.appLanguage),
                        selected = settings.quranReadingMode == mode,
                        onClick = { viewModel.setQuranReadingMode(mode) }
                    )
                }
            }
        }
        SettingToggleCard(
            settings.pick("Tampilkan transliterasi", "Show transliteration"),
            settings.showTransliteration
        ) { viewModel.setShowTransliteration(it) }
        SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
            Text(
                text = settings.pick("Pratinjau", "Preview"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ArabicVerseText(text = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا", fontSize = 24)
            Text(
                text = settings.pick(
                    "Sesungguhnya bersama kesulitan ada kemudahan.",
                    "Indeed, with hardship comes ease."
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            if (settings.showTransliteration && settings.quranReadingMode != QuranReadingMode.ARABIC_ONLY) {
                Text(
                    text = "Fa inna ma'al 'usri yusra",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = if (isEnglish) {
                    "The app language controls the main interface. Qur'an reading mode controls which translation appears in the mushaf view."
                } else {
                    "Bahasa aplikasi mengatur teks utama antarmuka. Mode baca Qur'an mengatur terjemahan yang tampil di layar mushaf."
                },
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
                            appLanguage.pick("Hapus favorit", "Remove favorite")
                        } else {
                            appLanguage.pick("Simpan favorit", "Save favorite")
                        },
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ChoiceChip(
                    label = if (isSelected) appLanguage.pick("Dipakai", "Active") else appLanguage.pick("Gunakan", "Use"),
                    selected = isSelected,
                    onClick = onUse
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingExperience(
    settings: UserSettings,
    viewModel: SettingsViewModel,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isEnglish = settings.appLanguage.isEnglish()
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var isResolvingLocation by remember { mutableStateOf(false) }
    val readiness = remember(refreshKey, settings.overrideSilentMode) {
        AdhanSystemHelper.buildReadiness(context)
    }

    fun refreshGps() {
        scope.launch {
            isResolvingLocation = true
            when (val result = DeviceLocationHelper.getCurrentLocation(context)) {
                is DeviceLocationResult.Success -> {
                    viewModel.setCurrentLocation(
                        locationName = result.location.label,
                        latitude = result.location.latitude,
                        longitude = result.location.longitude,
                        automatic = true
                    )
                    statusMessage = settings.pick(
                        "Lokasi aktif disetel ke ${result.location.label}.",
                        "Active location was set to ${result.location.label}."
                    )
                }

                is DeviceLocationResult.Error -> {
                    statusMessage = result.message
                }
            }
            isResolvingLocation = false
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshKey += 1
        statusMessage = if (it) {
            settings.pick("Izin notifikasi aktif.", "Notification permission is active.")
        } else {
            settings.pick("Izin notifikasi ditolak.", "Notification permission was denied.")
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.any { it.value }) {
            refreshGps()
        } else {
            statusMessage = settings.pick("Izin lokasi belum diberikan.", "Location permission has not been granted.")
        }
    }

    OverlayShell(
        title = settings.pick("Persiapan NurApp", "NurApp setup"),
        subtitle = settings.pick("Aktifkan fitur penting sebelum mulai", "Enable key features before you begin"),
        onBack = {
            viewModel.completeOnboarding()
            onFinish()
        }
    ) {
        SanctuaryCard {
            SectionHeader(
                eyebrow = if (isEnglish) "Step 1" else "Langkah 1",
                title = settings.pick("Siapkan sistem adzan", "Prepare the adhan system")
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = settings.pick("Notifikasi", "Notifications"),
                ready = readiness.notificationPermissionGranted && readiness.appNotificationsEnabled
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = "Exact alarm",
                ready = readiness.exactAlarmGranted
            )
            SystemStatusLine(
                appLanguage = settings.appLanguage,
                title = settings.pick("Optimasi baterai", "Battery optimization"),
                ready = readiness.batteryOptimizationIgnored
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChoiceChip(
                    label = settings.pick("Notifikasi", "Notifications"),
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
                    label = "Exact alarm",
                    selected = readiness.exactAlarmGranted,
                    onClick = { AdhanSystemHelper.openExactAlarmSettings(context) }
                )
                ChoiceChip(
                    label = settings.pick("Baterai", "Battery"),
                    selected = readiness.batteryOptimizationIgnored,
                    onClick = { AdhanSystemHelper.openBatteryOptimizationSettings(context) }
                )
            }
        }

        SanctuaryCard {
            SectionHeader(
                eyebrow = if (isEnglish) "Step 2" else "Langkah 2",
                title = settings.pick("Lokasi dan hisab", "Location and calculation")
            )
            Text(
                text = settings.pick("Lokasi aktif: ${settings.locationName}", "Active location: ${settings.locationName}"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = settings.pick(
                    "Metode ${settings.prayerCalculationMethod.label} - Asar ${settings.asrMadhhab.label}",
                    "Method ${settings.prayerCalculationMethod.label} - Asr ${settings.asrMadhhab.label}"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChoiceChip(
                    label = if (isResolvingLocation) {
                        settings.pick("Mengambil GPS...", "Getting GPS...")
                    } else {
                        settings.pick("Gunakan GPS", "Use GPS")
                    },
                    selected = settings.autoLocation,
                    onClick = {
                        if (!isResolvingLocation) {
                            if (DeviceLocationHelper.hasLocationPermission(context)) {
                                refreshGps()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    }
                )
                com.sajda.app.domain.model.PrayerCalculationMethod.entries.forEach { method ->
                    ChoiceChip(
                        label = method.label,
                        selected = settings.prayerCalculationMethod == method,
                        onClick = { viewModel.setPrayerCalculationMethod(method) }
                    )
                }
            }
            if (isResolvingLocation) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        SanctuaryCard {
            SectionHeader(
                eyebrow = if (isEnglish) "Step 3" else "Langkah 3",
                title = settings.pick("Tes suara adzan", "Test adhan audio")
            )
            Text(
                text = settings.pick(
                    "Tes cepat ini membantu memastikan notifikasi dan audio berjalan sebelum Anda mulai memakai aplikasi setiap hari.",
                    "This quick test helps confirm that notifications and audio work before you start using the app every day."
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChoiceChip(
                    label = settings.pick("Tes Subuh", "Test Fajr"),
                    selected = false,
                    onClick = { AdzanService.play(context, PrayerName.FAJR.label) }
                )
                ChoiceChip(
                    label = settings.pick("Tes Reguler", "Test Regular"),
                    selected = false,
                    onClick = { AdzanService.play(context, PrayerName.DHUHR.label) }
                )
                ChoiceChip(
                    label = "Stop",
                    selected = false,
                    onClick = { AdzanService.stop(context) }
                )
            }
        }

        SanctuaryCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.completeOnboarding()
                        onFinish()
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = settings.pick("Masuk ke NurApp", "Enter NurApp"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = settings.pick(
                            "Anda bisa mengubah semuanya lagi dari Settings kapan saja.",
                            "You can change everything again from Settings at any time."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = settings.pick("Selesai", "Done"),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            statusMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
