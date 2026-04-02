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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.Ayat
import com.sajda.app.domain.model.CityPreset
import com.sajda.app.domain.model.DailyDua
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.Surah
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.ui.component.ArabicVerseText
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.component.SectionHeader
import com.sajda.app.service.AdzanService
import com.sajda.app.ui.viewmodel.SettingsViewModel
import com.sajda.app.util.AdhanSystemHelper
import com.sajda.app.util.DeviceLocationHelper
import com.sajda.app.util.DeviceLocationResult
import com.sajda.app.util.LocationConstants
import com.sajda.app.util.SpiritualContent
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun DailyDuaScreen(
    bookmarkedIds: Set<String>,
    onBack: () -> Unit,
    onToggleBookmark: (String) -> Unit
) {
    OverlayShell(
        title = "Daily Dua",
        subtitle = "Morning, evening, daily activities",
        onBack = onBack
    ) {
        SpiritualContent.dailyDuas
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
    }
}

@Composable
fun TafsirScreen(
    surah: Surah,
    ayat: Ayat,
    onBack: () -> Unit
) {
    val tafsirItems = remember(surah.number, ayat.id) {
        SpiritualContent.buildTafsir(ayat, surah.transliteration)
    }

    OverlayShell(
        title = "Tafsir Ringkas",
        subtitle = "${surah.transliteration} • Ayat ${ayat.ayatNumber}",
        onBack = onBack
    ) {
        SanctuaryCard {
            ArabicVerseText(text = ayat.textArabic)
            Text(
                text = ayat.translation,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        tafsirItems.forEach { paragraph ->
            SanctuaryCard {
                Text(text = paragraph, style = MaterialTheme.typography.bodyLarge)
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
        title = "Smart Reminder",
        subtitle = "Qur'an dan dzikir",
        onBack = onBack
    ) {
        ReminderCard(
            title = "Qur'an Reading",
            subtitle = "Ingatkan untuk membaca Al-Qur'an setiap hari.",
            checked = settings.quranReminderEnabled,
            time = settings.quranReminderTime,
            onCheckedChange = { enabled: Boolean -> viewModel.setQuranReminder(enabled) },
            onTimePicked = { time: String -> viewModel.setQuranReminder(settings.quranReminderEnabled, time) }
        )
        ReminderCard(
            title = "Dzikir Pagi",
            subtitle = "Pengingat wirid dan dzikir setelah Subuh.",
            checked = settings.morningDzikirReminderEnabled,
            time = settings.morningDzikirReminderTime,
            onCheckedChange = { enabled: Boolean -> viewModel.setMorningDzikirReminder(enabled) },
            onTimePicked = { time: String -> viewModel.setMorningDzikirReminder(settings.morningDzikirReminderEnabled, time) }
        )
        ReminderCard(
            title = "Dzikir Sore",
            subtitle = "Pengingat dzikir sore menjelang Maghrib.",
            checked = settings.eveningDzikirReminderEnabled,
            time = settings.eveningDzikirReminderTime,
            onCheckedChange = { enabled: Boolean -> viewModel.setEveningDzikirReminder(enabled) },
            onTimePicked = { time: String -> viewModel.setEveningDzikirReminder(settings.eveningDzikirReminderEnabled, time) }
        )
    }
}

@Composable
private fun ReminderCard(
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
            text = "Jam $time",
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
            "Izin notifikasi berhasil diberikan."
        } else {
            "Izin notifikasi ditolak. Notifikasi adzan bisa tidak muncul di Android 13+."
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
        title = "Adhan Settings",
        subtitle = "Alarm sholat offline",
        onBack = onBack
    ) {
        SettingToggleCard("Adzan otomatis", settings.adzanEnabled) { viewModel.setAdzanEnabled(it) }
        SettingToggleCard("Override silent mode", settings.overrideSilentMode) { viewModel.setOverrideSilentMode(it) }
        SettingToggleCard("Vibration", settings.vibrationEnabled) { viewModel.setVibrationEnabled(it) }
        SanctuaryCard {
            SectionHeader(eyebrow = "Status Sistem Adzan", title = "Alarm berikutnya dan riwayat")
            Text(
                text = if (settings.nextScheduledPrayer.isNotBlank() && settings.nextScheduledAt.isNotBlank()) {
                    "Berikutnya: ${settings.nextScheduledPrayer} • ${settings.nextScheduledAt}"
                } else {
                    "Belum ada alarm berikutnya yang tersimpan. Coba jadwalkan ulang."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (settings.lastAdhanPrayer.isNotBlank()) {
                    "Terakhir: ${settings.lastAdhanPrayer} • ${settings.lastAdhanStatus} • ${settings.lastAdhanAt}"
                } else {
                    "Belum ada riwayat adzan. Tes manual bisa dipakai untuk memastikan alurnya."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Snooze default",
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
            SectionHeader(eyebrow = "Kesiapan Sistem", title = "Diagnosa adzan")
            SystemStatusLine(
                title = "Izin notifikasi",
                ready = readiness.notificationPermissionGranted && readiness.appNotificationsEnabled
            )
            SystemStatusLine(
                title = "Channel adzan aktif",
                ready = readiness.adhanChannelEnabled
            )
            SystemStatusLine(
                title = "Exact alarm",
                ready = readiness.exactAlarmGranted
            )
            SystemStatusLine(
                title = "Optimasi baterai",
                ready = readiness.batteryOptimizationIgnored
            )
            SystemStatusLine(
                title = "Mode senyap perangkat",
                ready = !readiness.silentModeActive || settings.overrideSilentMode,
                message = if (readiness.silentModeActive && !settings.overrideSilentMode) {
                    "HP sedang silent/vibrate dan Override silent mode masih mati."
                } else {
                    null
                }
            )
            SystemStatusLine(
                title = "Volume alarm",
                ready = readiness.alarmVolumeLevel > 0,
                message = if (readiness.alarmVolumeLevel > 0) {
                    "Level alarm saat ini: ${readiness.alarmVolumeLevel}"
                } else {
                    "Volume alarm sedang 0, jadi suara adzan tidak akan terdengar."
                }
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChoiceChip(
                    label = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !readiness.notificationPermissionGranted) {
                        "Izinkan notifikasi"
                    } else {
                        "Pengaturan notif"
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
                    label = "Exact alarm",
                    selected = readiness.exactAlarmGranted,
                    onClick = { AdhanSystemHelper.openExactAlarmSettings(context) }
                )
                ChoiceChip(
                    label = "Baterai",
                    selected = readiness.batteryOptimizationIgnored,
                    onClick = { AdhanSystemHelper.openBatteryOptimizationSettings(context) }
                )
                ChoiceChip(
                    label = "Jadwalkan ulang",
                    selected = false,
                    onClick = {
                        viewModel.refreshPrayerSchedule()
                        systemRefreshKey += 1
                        systemMessage = "Jadwal adzan dijadwalkan ulang dari pengaturan sekarang."
                    }
                )
            }
            Text(
                text = "Agar adzan bekerja seperti aplikasi adzan pada umumnya, minimal aktifkan notifikasi, exact alarm, dan lepaskan pembatasan baterai.",
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
            SectionHeader(eyebrow = "Manual Test", title = "Coba suara adzan")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip(
                    label = "Tes Subuh",
                    selected = false,
                    onClick = { AdzanService.play(context, PrayerName.FAJR.label) }
                )
                ChoiceChip(
                    label = "Tes Reguler",
                    selected = false,
                    onClick = { AdzanService.play(context, PrayerName.DHUHR.label) }
                )
                ChoiceChip(
                    label = "Stop",
                    selected = false,
                    onClick = { AdzanService.stop(context) }
                )
            }
            Text(
                text = "Tes Subuh memakai audio khusus Subuh. Tes Reguler memakai audio untuk Dzuhur, Ashar, Maghrib, dan Isya.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SettingToggleCard("Subuh", settings.fajrAdzanEnabled) { viewModel.setPrayerEnabled(PrayerName.FAJR, it) }
        SettingToggleCard("Dzuhur", settings.dhuhrAdzanEnabled) { viewModel.setPrayerEnabled(PrayerName.DHUHR, it) }
        SettingToggleCard("Ashar", settings.asrAdzanEnabled) { viewModel.setPrayerEnabled(PrayerName.ASR, it) }
        SettingToggleCard("Maghrib", settings.maghribAdzanEnabled) { viewModel.setPrayerEnabled(PrayerName.MAGHRIB, it) }
        SettingToggleCard("Isya", settings.ishaAdzanEnabled) { viewModel.setPrayerEnabled(PrayerName.ISHA, it) }
    }
}

@Composable
private fun SystemStatusLine(
    title: String,
    ready: Boolean,
    message: String? = null
) {
    Text(
        text = if (ready) "Siap: $title" else "Perlu dicek: $title",
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
        title = "Appearance",
        subtitle = "Tampilan Sajda",
        onBack = onBack
    ) {
        SettingToggleCard("Dark mode", settings.darkMode) { viewModel.setDarkMode(it) }
        SettingToggleCard("Night mode", settings.nightMode) { viewModel.setNightMode(it) }
        SettingToggleCard("Focus mode default", settings.focusMode) { viewModel.setFocusMode(it) }
        SanctuaryCard {
            SectionHeader(eyebrow = "Reading Layout", title = "Kenyamanan tilawah")
            Text(
                text = "Sesuaikan ukuran teks dan jarak antarayat agar sesi baca lebih lembut di mata dan nyaman untuk waktu yang lama.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Ukuran Arab",
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
                text = "Ukuran terjemahan",
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
                text = "Jarak ayat",
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
                text = "Sesungguhnya bersama kesulitan ada kemudahan.",
                fontSize = settings.translationFontSize.sp,
                lineHeight = (settings.translationFontSize + 8).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
            Text(
                text = "Sajda memakai palet emerald lembut dengan layered surfaces agar pengalaman ibadah terasa tenang, ringan, dan fokus.",
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
                    locationStatus = "Lokasi aktif diperbarui ke ${result.location.label}."
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
            locationStatus = "Izin lokasi ditolak. Aktifkan izin GPS agar jadwal sholat menyesuaikan lokasi Anda."
        }
    }

    OverlayShell(
        title = "Location",
        subtitle = settings.locationName,
        onBack = onBack
    ) {
        SettingToggleCard("Auto GPS", settings.autoLocation) { enabled ->
            if (!enabled) {
                viewModel.setAutoLocation(false)
                locationStatus = "Mode GPS otomatis dimatikan."
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
            SectionHeader(eyebrow = "GPS Device", title = "Lokasi perangkat")
            Text(
                text = "Gunakan GPS untuk mengambil lokasi ponsel saat ini dan menghitung jadwal sholat yang lebih akurat.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ChoiceChip(
                label = if (isResolvingLocation) "Mengambil lokasi..." else "Gunakan lokasi saat ini",
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
                text = "Koordinat aktif: ${"%.4f".format(Locale.US, settings.latitude)}, ${"%.4f".format(Locale.US, settings.longitude)}",
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
            SectionHeader(eyebrow = "Manual", title = "Cari wilayah Indonesia")
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Cari provinsi, kota, atau kabupaten") }
            )
            Text(
                text = "Ketik nama wilayah, lalu pilih hasil yang paling sesuai. Pencarian mendukung provinsi, kota, dan kabupaten.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            filteredCities.take(if (normalizedQuery.isBlank()) 24 else 40).forEach { city ->
                LocationSelectionRow(
                    city = city,
                    isSelected = city.isSelectedLocation(),
                    isFavorite = city.isFavoriteLocation(),
                    onUse = {
                        viewModel.setLocation(city)
                        query = city.displayName
                        locationStatus = "${city.displayName} dipakai untuk jadwal sholat."
                    },
                    onToggleFavorite = { viewModel.toggleFavoriteLocation(city.displayName) }
                )
            }
            if (filteredCities.isEmpty()) {
                Text(
                    text = "Belum ada hasil yang cocok. Coba pakai nama provinsi, kota, atau kabupaten yang lebih lengkap.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (favoriteCities.isNotEmpty()) {
            SanctuaryCard {
                SectionHeader(eyebrow = "Favorites", title = "Lokasi favorit")
                favoriteCities.forEach { city ->
                    LocationSelectionRow(
                        city = city,
                        isSelected = city.isSelectedLocation(),
                        isFavorite = true,
                        onUse = {
                            viewModel.setLocation(city)
                            query = city.displayName
                            locationStatus = "${city.displayName} dipakai untuk jadwal sholat."
                        },
                        onToggleFavorite = { viewModel.toggleFavoriteLocation(city.displayName) }
                    )
                }
            }
        }
        SanctuaryCard {
            SectionHeader(eyebrow = "Calculation", title = "Metode waktu sholat")
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
                text = settings.prayerCalculationMethod.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                com.sajda.app.domain.model.AsrMadhhab.entries.forEach { madhhab ->
                    ChoiceChip(
                        label = "Asar ${madhhab.label}",
                        selected = settings.asrMadhhab == madhhab,
                        onClick = { viewModel.setAsrMadhhab(madhhab) }
                    )
                }
            }
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
        title = "Language",
        subtitle = "App and Qur'an reading",
        onBack = onBack
    ) {
        SanctuaryCard {
            SectionHeader(eyebrow = "App Language", title = "Pilih bahasa")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { language ->
                    ChoiceChip(
                        label = language.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = settings.appLanguage == language,
                        onClick = { viewModel.setAppLanguage(language) }
                    )
                }
            }
        }
        SettingToggleCard("Show translation", settings.showTranslation) { viewModel.setShowTranslation(it) }
        SettingToggleCard("Arabic only mode", settings.arabicOnly) { viewModel.setArabicOnly(it) }
        SettingToggleCard("Show transliteration", settings.showTransliteration) { viewModel.setShowTransliteration(it) }
        SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
            Text(text = "Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ArabicVerseText(text = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا", fontSize = 24)
            Text(
                text = when (settings.appLanguage) {
                    AppLanguage.INDONESIAN -> "Sesungguhnya bersama kesulitan ada kemudahan."
                    AppLanguage.ENGLISH -> "Indeed, with hardship comes ease."
                    AppLanguage.ARABIC -> "إِنَّ مَعَ الْعُسْرِ يُسْرًا"
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LocationSelectionRow(
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
                Text(
                    text = "${"%.3f".format(Locale.US, city.latitude)}, ${"%.3f".format(Locale.US, city.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChoiceChip(
                    label = if (isFavorite) "Favorit" else "Simpan",
                    selected = isFavorite,
                    onClick = onToggleFavorite
                )
                ChoiceChip(
                    label = if (isSelected) "Dipakai" else "Gunakan",
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
                    statusMessage = "Lokasi aktif disetel ke ${result.location.label}."
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
        statusMessage = if (it) "Izin notifikasi aktif." else "Izin notifikasi ditolak."
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.any { it.value }) {
            refreshGps()
        } else {
            statusMessage = "Izin lokasi belum diberikan."
        }
    }

    OverlayShell(
        title = "Persiapan Sajda",
        subtitle = "Aktifkan fitur penting sebelum mulai",
        onBack = {
            viewModel.completeOnboarding()
            onFinish()
        }
    ) {
        SanctuaryCard {
            SectionHeader(eyebrow = "Step 1", title = "Siapkan sistem adzan")
            SystemStatusLine(
                title = "Notifikasi",
                ready = readiness.notificationPermissionGranted && readiness.appNotificationsEnabled
            )
            SystemStatusLine(
                title = "Exact alarm",
                ready = readiness.exactAlarmGranted
            )
            SystemStatusLine(
                title = "Optimasi baterai",
                ready = readiness.batteryOptimizationIgnored
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChoiceChip(
                    label = "Notifikasi",
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
                    label = "Baterai",
                    selected = readiness.batteryOptimizationIgnored,
                    onClick = { AdhanSystemHelper.openBatteryOptimizationSettings(context) }
                )
            }
        }

        SanctuaryCard {
            SectionHeader(eyebrow = "Step 2", title = "Lokasi dan hisab")
            Text(
                text = "Lokasi aktif: ${settings.locationName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Metode ${settings.prayerCalculationMethod.label} • Asar ${settings.asrMadhhab.label}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChoiceChip(
                    label = if (isResolvingLocation) "Mengambil GPS..." else "Gunakan GPS",
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
            SectionHeader(eyebrow = "Step 3", title = "Tes suara adzan")
            Text(
                text = "Tes cepat ini membantu memastikan notifikasi dan audio berjalan sebelum Anda mulai memakai aplikasi setiap hari.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChoiceChip(
                    label = "Tes Subuh",
                    selected = false,
                    onClick = { AdzanService.play(context, PrayerName.FAJR.label) }
                )
                ChoiceChip(
                    label = "Tes Reguler",
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
                        text = "Masuk ke Sajda App",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Anda bisa mengubah semuanya lagi dari Settings kapan saja.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Selesai",
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
