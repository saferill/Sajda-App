package com.sajda.app.ui.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.service.AdzanManager
import com.sajda.app.ui.viewmodel.SettingsViewModel
import com.sajda.app.util.AdhanSystemHelper
import com.sajda.app.util.DeviceLocationHelper
import com.sajda.app.util.DeviceLocationResult
import com.sajda.app.util.isEnglish
import com.sajda.app.util.pick
import kotlinx.coroutines.launch

@Composable
fun PermissionSetupScreen(
    settings: UserSettings,
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var systemRefreshKey by remember { mutableIntStateOf(0) }
    var isResolvingLocation by remember { mutableStateOf(false) }
    val readiness = remember(systemRefreshKey, settings.overrideSilentMode, settings.autoLocation) {
        AdhanSystemHelper.buildReadiness(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) systemRefreshKey += 1
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun refreshLocationFromDevice() {
        scope.launch {
            isResolvingLocation = true
            viewModel.setLocationPermissionPrompted(true)
            viewModel.setAutoLocation(true, refreshSchedule = false)
            val updatedByManager = runCatching {
                AdzanManager(context).checkAndUpdateLocation()
            }.onFailure { error ->
                android.util.Log.e("PermissionSetupScreen", "Gagal update lokasi dari GPS", error)
            }.getOrDefault(false)

            if (updatedByManager) {
                statusMessage = settings.pick(
                    "Lokasi aktif berhasil diperbarui dari GPS.",
                    "The active location was updated from GPS."
                )
            } else {
                when (val result = DeviceLocationHelper.getCurrentLocation(context)) {
                    is DeviceLocationResult.Success -> {
                        viewModel.setCurrentLocation(
                            locationName = result.location.label,
                            latitude = result.location.latitude,
                            longitude = result.location.longitude,
                            automatic = true
                        )
                        statusMessage = settings.pick(
                            "Lokasi aktif berhasil diperbarui dari GPS.",
                            "The active location was updated from GPS."
                        )
                    }
                    is DeviceLocationResult.Error -> statusMessage = result.message
                }
            }
            isResolvingLocation = false
            systemRefreshKey += 1
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setAdzanEnabled(true)
            statusMessage = settings.pick(
                "Izin notifikasi aktif. Pengingat adzan siap dipakai.",
                "Notification permission is active. Adhan reminders are ready."
            )
        } else {
            viewModel.setAdzanEnabled(false)
            statusMessage = settings.pick(
                "Izin notifikasi ditolak. Notifikasi adzan bisa tidak muncul.",
                "Notification permission was denied. Adhan reminders may not appear."
            )
        }
        systemRefreshKey += 1
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.setLocationPermissionPrompted(true)
        if (permissions.any { it.value }) {
            refreshLocationFromDevice()
        } else {
            viewModel.setAutoLocation(false)
            statusMessage = settings.pick(
                "Izin lokasi belum diberikan.",
                "Location permission has not been granted yet."
            )
            systemRefreshKey += 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PermissionPatternBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = settings.pick("Kembali", "Back"),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = settings.pick("Permissions", "Permissions"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = settings.pick("Izinkan Akses Aplikasi", "Allow App Access"),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = settings.pick(
                                "Agar pengalaman ibadahmu lebih sempurna",
                                "So your worship experience feels complete"
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    PermissionTile(
                        icon = Icons.Rounded.LocationOn,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        title = settings.pick("Izin Lokasi", "Location Access"),
                        subtitle = settings.pick(
                            "Untuk menentukan waktu sholat dan arah kiblat",
                            "For prayer times and qibla direction"
                        ),
                        checked = readiness.locationPermissionGranted && settings.autoLocation,
                        onToggle = { enabled ->
                            if (enabled) {
                                if (DeviceLocationHelper.hasLocationPermission(context)) {
                                    refreshLocationFromDevice()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            } else {
                                viewModel.setAutoLocation(false)
                                statusMessage = settings.pick(
                                    "Lokasi otomatis dimatikan. Kamu masih bisa pilih lokasi manual di pengaturan.",
                                    "Auto location is off. You can still choose a manual location in settings."
                                )
                            }
                        }
                    ) {
                        Text(
                            text = settings.locationName.ifBlank {
                                settings.pick("Belum ada lokasi aktif", "No active location yet")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isResolvingLocation) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
                item {
                    PermissionTile(
                        icon = Icons.Rounded.NotificationsActive,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        iconBackground = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.32f),
                        title = settings.pick("Notifikasi Adzan", "Adhan Notifications"),
                        subtitle = settings.pick(
                            "Dapatkan pengingat tepat waktu",
                            "Get timely prayer reminders"
                        ),
                        checked = settings.adzanEnabled &&
                            readiness.notificationPermissionGranted &&
                            readiness.appNotificationsEnabled,
                        onToggle = { enabled ->
                            if (!enabled) {
                                viewModel.setAdzanEnabled(false)
                                statusMessage = settings.pick(
                                    "Pengingat adzan dimatikan dari aplikasi.",
                                    "Adhan reminders were disabled in the app."
                                )
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !readiness.notificationPermissionGranted
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.setAdzanEnabled(true)
                                AdhanSystemHelper.openNotificationSettings(context)
                                statusMessage = settings.pick(
                                    "Cek juga apakah notifikasi aplikasi dan channel adzan sudah aktif.",
                                    "Also verify that app notifications and the adhan channel are enabled."
                                )
                            }
                        }
                    )
                }
                item {
                    PermissionTile(
                        icon = Icons.Rounded.VolumeUp,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        iconBackground = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                        title = settings.pick("Abaikan Mode Senyap", "Override Silent Mode"),
                        subtitle = settings.pick(
                            "Tetap bunyikan adzan meski ponsel dalam mode senyap",
                            "Keep the adhan audible even when the phone is muted"
                        ),
                        checked = settings.overrideSilentMode,
                        onToggle = {
                            viewModel.setOverrideSilentMode(it)
                            statusMessage = if (it) {
                                settings.pick(
                                    "NurApp akan mencoba tetap memutar adzan saat ponsel senyap.",
                                    "NurApp will try to keep playing the adhan while the phone is muted."
                                )
                            } else {
                                settings.pick(
                                    "Mode senyap perangkat akan dihormati kembali.",
                                    "The phone's silent mode will be respected again."
                                )
                            }
                        }
                    ) {
                        Text(
                            text = settings.pick(
                                "Volume adzan tetap mengikuti volume alarm HP kamu.",
                                "The adhan volume still follows your phone's alarm volume."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    PermissionTile(
                        icon = Icons.Rounded.BatteryChargingFull,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        title = settings.pick("Optimasi Baterai", "Battery Optimization"),
                        subtitle = settings.pick(
                            "Izinkan aplikasi berjalan di latar belakang untuk akurasi pengingat",
                            "Allow background work so reminders stay accurate"
                        ),
                        checked = readiness.batteryOptimizationIgnored,
                        onToggle = {
                            AdhanSystemHelper.openBatteryOptimizationSettings(context)
                            statusMessage = settings.pick(
                                "Buka pengaturan baterai lalu pilih NurApp agar tidak dibatasi.",
                                "Open battery settings and mark NurApp as unrestricted."
                            )
                        }
                    ) {
                        Text(
                            text = if (readiness.batteryOptimizationIgnored) {
                                settings.pick(
                                    "NurApp sudah dibebaskan dari optimasi baterai agresif.",
                                    "NurApp is already exempt from aggressive battery optimization."
                                )
                            } else {
                                settings.pick(
                                    "Masih perlu dibebaskan supaya notifikasi adzan lebih stabil.",
                                    "This still needs to be unrestricted for more stable adhan reminders."
                                )
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    statusMessage?.let { message ->
                        Text(
                            text = message,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PrimarySetupButton(
                            text = settings.pick("Lanjutkan ke Dashboard", "Continue to Dashboard"),
                            onClick = onContinue
                        )
                        Text(
                            text = settings.pick(
                                "Anda dapat mengubah pengaturan ini kapan saja di menu Pengaturan",
                                "You can change these settings anytime from Settings"
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
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
fun NurAppOnboardingScreen(
    settings: UserSettings,
    viewModel: SettingsViewModel,
    onFinish: () -> Unit
) {
    val isEnglish = settings.appLanguage.isEnglish()
    var page by rememberSaveable { mutableIntStateOf(0) }
    val pages = listOf(
        OnboardingPage(
            title = if (isEnglish) "Accurate Prayer Times" else "Jadwal Sholat Akurat",
            description = if (isEnglish) {
                "Get precise adhan times based on your location in real time."
            } else {
                "Dapatkan waktu adzan yang tepat berdasarkan lokasi kamu secara real-time."
            }
        ),
        OnboardingPage(
            title = if (isEnglish) "Complete Qur'an & Tafsir" else "Al-Qur'an Lengkap & Tafsir",
            description = if (isEnglish) {
                "Read the full Qur'an with translation, tafsir per ayah, and audio murottal."
            } else {
                "Baca Al-Qur'an 30 juz lengkap dengan terjemahan, tafsir per ayat, dan audio murottal."
            }
        ),
        OnboardingPage(
            title = if (isEnglish) "Qibla Compass & Islamic Tools" else "Kompas Kiblat & Fitur Lengkap",
            description = if (isEnglish) {
                "Find the qibla, daily hadith, Ramadan schedule, and Hijri calendar in one app."
            } else {
                "Temukan arah kiblat, hadist harian, jadwal Ramadhan, dan kalender Hijriah dalam satu aplikasi."
            }
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PermissionPatternBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = if (isEnglish) "Skip" else "Lewati",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            viewModel.completeOnboarding()
                            onFinish()
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = page,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "nurappOnboardingPage"
            ) { currentPage ->
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    when (currentPage) {
                        0 -> PrayerOnboardingArt()
                        1 -> QuranOnboardingArt()
                        else -> QiblaOnboardingArt()
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = pages[currentPage].title,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = pages[currentPage].description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (page == index) 28.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (page == index) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (page) {
                            0 -> Icons.Rounded.LocationOn
                            1 -> Icons.Rounded.AutoAwesome
                            else -> Icons.Rounded.Explore
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (page) {
                            0 -> settings.pick("Otomatis terdeteksi", "Auto detected")
                            1 -> settings.pick("Tafsir dan audio", "Tafsir and audio")
                            else -> settings.pick("Kalender dan kiblat", "Calendar and qibla")
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                PrimarySetupButton(
                    text = if (page == pages.lastIndex) {
                        if (isEnglish) "Start Now" else "Mulai Sekarang"
                    } else {
                        if (isEnglish) "Next" else "Selanjutnya"
                    },
                    compact = true,
                    onClick = {
                        if (page == pages.lastIndex) {
                            viewModel.completeOnboarding()
                            onFinish()
                        } else {
                            page += 1
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PrayerOnboardingArt() {
    EditorialHeroCard(
        brush = Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.primary,
                Color(0xFF0D1B36)
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.16f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("GPS", color = Color.White, style = MaterialTheme.typography.labelLarge)
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .clip(RoundedCornerShape(34.dp))
                        .background(Color.White.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mosque,
                        contentDescription = null,
                        tint = Color(0xFFFFDCC4),
                        modifier = Modifier.size(72.dp)
                    )
                }
                Text(
                    text = "Subuh | Dzuhur | Ashar | Maghrib | Isya",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.90f)
                )
            }
        }
    }
}

@Composable
private fun QuranOnboardingArt() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(294.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFDFDFC),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .padding(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(170.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.84f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.94f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MenuBook,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(82.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.22f))
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun QiblaOnboardingArt() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(320.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(294.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            )
            Box(
                modifier = Modifier
                    .size(236.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Explore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(132.dp)
                )
            }

            FloatingBadge(
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 18.dp, end = 4.dp),
                icon = Icons.Rounded.DateRange,
                title = "Hijriah",
                value = "1447 H"
            )

            FloatingBadge(
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 8.dp, bottom = 18.dp),
                icon = Icons.Rounded.HistoryEdu,
                title = "Hadist",
                value = "Harian",
                inverted = true
            )
        }
    }
}

@Composable
private fun PermissionPatternBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f)
                    )
                )
            )
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .padding(
                        start = if (index % 2 == 0) 24.dp else 220.dp,
                        top = (96 + index * 160).dp
                    )
                    .size(if (index == 1) 180.dp else 132.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == 1) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.10f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                    )
            )
        }
    }
}

@Composable
private fun PermissionTile(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    footer: (@Composable ColumnScope.() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!checked) }
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                NurSwitch(checked = checked, onClick = { onToggle(!checked) })
            }
            footer?.invoke(this)
        }
    }
}

@Composable
private fun NurSwitch(
    checked: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(50.dp)
            .height(30.dp)
            .clip(CircleShape)
            .background(
                if (checked) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun PrimarySetupButton(
    text: String,
    onClick: () -> Unit,
    compact: Boolean = false
) {
    Surface(
        modifier = if (compact) Modifier else Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = if (compact) 22.dp else 28.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun EditorialHeroCard(
    brush: Brush,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(390.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(brush),
        content = content
    )
}

@Composable
private fun FloatingBadge(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    inverted: Boolean = false
) {
    val backgroundColor = if (inverted) MaterialTheme.colorScheme.primary else Color.White
    val titleColor = if (inverted) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.70f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val valueColor = if (inverted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val iconTint = if (inverted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = backgroundColor,
        shadowElevation = 14.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (inverted) Color.White.copy(alpha = 0.10f)
                        else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.18f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = titleColor)
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
            }
        }
    }
}

private data class OnboardingPage(
    val title: String,
    val description: String
)
