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

    val msgGpsUpdated = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.the_active_location_was_updated_from_gps)
    val msgNotifActive = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.notification_permission_is_active_adhan)
    val msgNotifDenied = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.notification_permission_was_denied_adhan)
    val msgLocDenied = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.location_permission_has_not_been_granted)
    val msgAutoLocOff = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.auto_location_is_off_you_can_still_choos)
    val msgAdhanDisabled = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.adhan_reminders_were_disabled_in_the_app)
    val msgVerifyNotif = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.also_verify_that_app_notifications_and_t)
    val msgOverrideOn = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.nurapp_will_try_to_keep_playing_the_adha)
    val msgOverrideOff = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.the_phone_s_silent_mode_will_be_respecte)
    val msgBatteryOpen = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.open_battery_settings_and_mark_nurapp_as)


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
                statusMessage = msgGpsUpdated
            } else {
                when (val result = DeviceLocationHelper.getCurrentLocation(context)) {
                    is DeviceLocationResult.Success -> {
                        viewModel.updateLocation(
                            locationName = result.location.label,
                            latitude = result.location.latitude,
                            longitude = result.location.longitude,
                            automatic = true
                        )
                        statusMessage = msgGpsUpdated
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
            statusMessage = msgNotifActive
        } else {
            viewModel.setAdzanEnabled(false)
            statusMessage = msgNotifDenied
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
            statusMessage = msgLocDenied
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
                        contentDescription = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.back),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.permissions),
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
                            text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.allow_app_access),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.so_your_worship_experience_feels_complet),
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
                        title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.location_access),
                        subtitle = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.for_prayer_times_and_qibla_direction),
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
                                statusMessage = msgAutoLocOff
                            }
                        }
                    ) {
                        Text(
                            text = settings.locationName.ifBlank {
                                androidx.compose.ui.res.stringResource(com.sajda.app.R.string.no_active_location_yet)
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
                        title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.adhan_notifications_2),
                        subtitle = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.get_timely_prayer_reminders),
                        checked = settings.adzanEnabled &&
                            readiness.notificationPermissionGranted &&
                            readiness.appNotificationsEnabled,
                        onToggle = { enabled ->
                            if (!enabled) {
                                viewModel.setAdzanEnabled(false)
                                statusMessage = msgAdhanDisabled
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !readiness.notificationPermissionGranted
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.setAdzanEnabled(true)
                                AdhanSystemHelper.openNotificationSettings(context)
                                statusMessage = msgVerifyNotif
                            }
                        }
                    )
                }
                item {
                    PermissionTile(
                        icon = Icons.Rounded.VolumeUp,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        iconBackground = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                        title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.override_silent_mode),
                        subtitle = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.keep_the_adhan_audible_even_when_the_pho),
                        checked = settings.overrideSilentMode,
                        onToggle = {
                            viewModel.setOverrideSilentMode(it)
                            statusMessage = if (it) {
                                msgOverrideOn
                            } else {
                                msgOverrideOff
                            }
                        }
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.the_adhan_volume_still_follows_your_phon),
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
                        title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.battery_optimization),
                        subtitle = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.allow_background_work_so_reminders_stay),
                        checked = readiness.batteryOptimizationIgnored,
                        onToggle = {
                            AdhanSystemHelper.openBatteryOptimizationSettings(context)
                            statusMessage = msgBatteryOpen
                        }
                    ) {
                        Text(
                            text = if (readiness.batteryOptimizationIgnored) {
                                androidx.compose.ui.res.stringResource(com.sajda.app.R.string.nurapp_is_already_exempt_from_aggressive)
                            } else {
                                androidx.compose.ui.res.stringResource(com.sajda.app.R.string.this_still_needs_to_be_unrestricted_for)
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
                            text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.continue_to_dashboard),
                            onClick = onContinue
                        )
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.you_can_change_these_settings_anytime_fr),
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
    var page by rememberSaveable { mutableIntStateOf(0) }
    val pages = listOf(
        OnboardingPage(
            title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.onboarding_title_prayer),
            description = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.onboarding_desc_prayer)
        ),
        OnboardingPage(
            title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.onboarding_title_quran),
            description = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.onboarding_desc_quran)
        ),
        OnboardingPage(
            title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.onboarding_title_qibla),
            description = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.onboarding_desc_qibla)
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
                    text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.onboarding_skip),
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
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentPage) {
                        0 -> PrayerOnboardingArt()
                        1 -> QuranOnboardingArt()
                        else -> QiblaOnboardingArt()
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = pages[currentPage].title,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = pages[currentPage].description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                            0 -> androidx.compose.ui.res.stringResource(com.sajda.app.R.string.auto_detected)
                            1 -> androidx.compose.ui.res.stringResource(com.sajda.app.R.string.tafsir_and_audio)
                            else -> androidx.compose.ui.res.stringResource(com.sajda.app.R.string.calendar_and_qibla)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                PrimarySetupButton(
                    text = if (page == pages.lastIndex) {
                        androidx.compose.ui.res.stringResource(com.sajda.app.R.string.onboarding_start)
                    } else {
                        androidx.compose.ui.res.stringResource(com.sajda.app.R.string.onboarding_next)
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
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.onboarding_gps_label),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
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
                    text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.onboarding_prayer_list),
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
