package com.sajda.app.ui.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.PrayerCalculationMethod
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.theme.surfaceContainerHigh
import com.sajda.app.ui.theme.surfaceContainerLow
import com.sajda.app.ui.viewmodel.SettingsViewModel
import com.sajda.app.util.DeviceLocationHelper
import com.sajda.app.util.DeviceLocationResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PermissionSetupScreen(
    settings: UserSettings,
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isEnglish = settings.appLanguage == AppLanguage.ENGLISH
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        status = if (granted) {
            if (isEnglish) "Notification permission granted." else "Izin notifikasi diberikan."
        } else {
            if (isEnglish) "Notification permission denied." else "Izin notifikasi ditolak."
        }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.any { it.value }) {
            scope.launch {
                when (val result = DeviceLocationHelper.getCurrentLocation(context)) {
                    is DeviceLocationResult.Success -> {
                        viewModel.setCurrentLocation(
                            locationName = result.location.label,
                            latitude = result.location.latitude,
                            longitude = result.location.longitude,
                            automatic = true
                        )
                        status = if (isEnglish) "Location updated from GPS." else "Lokasi diperbarui dari GPS."
                    }
                    is DeviceLocationResult.Error -> status = result.message
                }
            }
        } else {
            status = if (isEnglish) "Location permission denied." else "Izin lokasi ditolak."
        }
    }

    OverlayShell(
        title = if (isEnglish) "App Permissions" else "Akses Aplikasi",
        subtitle = if (isEnglish) "Enable the important permissions" else "Aktifkan izin yang penting",
        onBack = onBack
    ) {
        SanctuaryCard {
            PermissionRow(
                title = if (isEnglish) "Location Access" else "Izin Lokasi",
                subtitle = if (isEnglish) "For prayer times and qibla" else "Untuk waktu sholat dan arah kiblat",
                checked = settings.autoLocation
            ) {
                if (it) {
                    locationLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                } else {
                    viewModel.setAutoLocation(false)
                }
            }
            PermissionRow(
                title = if (isEnglish) "Adhan Notifications" else "Notifikasi Adzan",
                subtitle = if (isEnglish) "Reminders for every prayer" else "Pengingat adzan untuk setiap waktu sholat",
                checked = settings.adzanEnabled
            ) {
                viewModel.setAdzanEnabled(it)
                if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            Text(
                text = if (isEnglish) "Choose calculation method" else "Pilih metode perhitungan",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PrayerCalculationMethod.entries.forEach { method ->
                    Text(
                        text = method.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (settings.prayerCalculationMethod == method) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (settings.prayerCalculationMethod == method) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow)
                            .clickable { viewModel.setPrayerCalculationMethod(method) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
        status?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun PermissionRow(
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
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun NurAppOnboardingScreen(
    settings: UserSettings,
    viewModel: SettingsViewModel,
    onFinish: () -> Unit
) {
    val isEnglish = settings.appLanguage == AppLanguage.ENGLISH
    var page by rememberSaveable { mutableIntStateOf(0) }
    val pages = listOf(
        Triple(
            if (isEnglish) "Accurate Prayer Times" else "Jadwal Sholat Akurat",
            if (isEnglish) "Get adhan times based on your location in real time." else "Dapatkan waktu adzan yang tepat berdasarkan lokasi kamu.",
            Icons.Rounded.Mosque
        ),
        Triple(
            if (isEnglish) "Complete Qur'an & Tafsir" else "Al-Qur'an Lengkap & Tafsir",
            if (isEnglish) "Read the full Qur'an with translation, tafsir, and audio." else "Baca Al-Qur'an lengkap dengan terjemahan, tafsir, dan audio.",
            Icons.Rounded.MenuBook
        ),
        Triple(
            if (isEnglish) "Qibla & Islamic Tools" else "Kompas Kiblat & Fitur Lengkap",
            if (isEnglish) "Find qibla, daily hadith, Ramadan schedules, and Hijri calendar." else "Temukan arah kiblat, hadist harian, jadwal Ramadhan, dan kalender Hijriah.",
            Icons.Rounded.Explore
        )
    )
    val current = pages[page]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                text = if (isEnglish) "Skip" else "Lewati",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable {
                    viewModel.completeOnboarding()
                    onFinish()
                }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
                            )
                        )
                    )
                    .padding(28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Icon(
                    imageVector = current.third,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                Text(
                    text = current.first,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = androidx.compose.ui.graphics.Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = current.second,
                    style = MaterialTheme.typography.bodyLarge,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(pages.size) { index ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (page == index) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                            .padding(horizontal = if (page == index) 16.dp else 4.dp, vertical = 4.dp)
                    ) {}
                }
            }
        }

        Text(
            text = if (page == pages.lastIndex) {
                if (isEnglish) "Start Now" else "Mulai Sekarang"
            } else {
                if (isEnglish) "Next" else "Selanjutnya"
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable {
                    if (page == pages.lastIndex) {
                        viewModel.completeOnboarding()
                        onFinish()
                    } else {
                        page += 1
                    }
                }
                .padding(vertical = 18.dp),
            textAlign = TextAlign.Center
        )
    }
}
