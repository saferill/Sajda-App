package com.sajda.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajda.app.BuildConfig
import com.sajda.app.domain.model.AdhanStyle
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.QuranReadingMode
import com.sajda.app.ui.component.SajdaTopAction
import com.sajda.app.ui.component.SajdaTopBar
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.viewmodel.AppUpdateUiState
import com.sajda.app.ui.viewmodel.SettingsViewModel

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
    val isEnglish = settings.appLanguage == AppLanguage.ENGLISH

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 150.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            SajdaTopBar(
                title = "NurApp",
                subtitle = if (onBack != null) {
                    if (isEnglish) "Settings" else "Pengaturan"
                } else {
                    null
                },
                leading = onBack?.let {
                    {
                        SajdaTopAction(
                            icon = Icons.Rounded.ArrowBack,
                            label = if (isEnglish) "Back" else "Kembali",
                            onClick = it
                        )
                    }
                },
                trailing = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }

        item {
            SectionLabel(if (isEnglish) "Adhan & Notifications" else "Adzan & Notifikasi")
            SanctuaryCard {
                ToggleRow(
                    title = if (isEnglish) "Automatic Adhan" else "Suara Adzan",
                    subtitle = settings.adzanSound.title,
                    checked = settings.adzanEnabled,
                    onCheckedChange = viewModel::setAdzanEnabled
                )
                ActionRow(
                    title = if (isEnglish) "Advanced Adhan Settings" else "Pengaturan Adzan Lengkap",
                    subtitle = if (isEnglish) "Sound, snooze, vibration" else "Suara, snooze, getaran",
                    onClick = onOpenAdhanSettings
                )
                SliderRow(
                    title = if (isEnglish) "Notification Volume" else "Volume Notifikasi",
                    value = settings.adhanSnoozeMinutes.toFloat(),
                    valueRange = 5f..30f,
                    onValueChange = { viewModel.setAdhanSnoozeMinutes(it.toInt()) }
                )
            }
        }

        item {
            SectionLabel(if (isEnglish) "Appearance" else "Tampilan")
            SanctuaryCard {
                ToggleRow(
                    title = if (isEnglish) "Dark Mode" else "Mode Gelap",
                    subtitle = if (isEnglish) "Follow your preference" else "Ikuti pengaturan sistem",
                    checked = settings.darkMode || settings.nightMode,
                    onCheckedChange = {
                        viewModel.setDarkMode(it)
                        viewModel.setNightMode(it)
                    }
                )
                SliderRow(
                    title = if (isEnglish) "Font Size" else "Ukuran Font",
                    value = settings.arabicFontSize.toFloat(),
                    valueRange = 24f..40f,
                    onValueChange = { viewModel.setArabicFontSize(it.toInt()) }
                )
                ActionRow(
                    title = if (isEnglish) "Appearance Details" else "Detail Tampilan",
                    subtitle = if (isEnglish) "Theme, focus mode, spacing" else "Tema, mode fokus, spacing",
                    onClick = onOpenAppearanceSettings
                )
            }
        }

        item {
            SectionLabel("Al-Qur'an")
            SanctuaryCard {
                ActionRow(
                    title = if (isEnglish) "Translation Type" else "Tipe Terjemahan",
                    subtitle = when (settings.quranReadingMode) {
                        QuranReadingMode.ARABIC_ONLY -> if (isEnglish) "Arabic Only" else "Arab Saja"
                        QuranReadingMode.ARABIC_INDONESIAN -> if (isEnglish) "Arabic + Indonesian" else "Arab + Indonesia"
                        QuranReadingMode.ARABIC_ENGLISH -> if (isEnglish) "Arabic + English" else "Arab + English"
                        QuranReadingMode.ALL -> if (isEnglish) "Complete" else "Lengkap"
                    },
                    onClick = onOpenLanguageSettings
                )
                ActionRow(
                    title = if (isEnglish) "Audio Quality" else "Kualitas Audio",
                    subtitle = if (isEnglish) "Standard (64 kbps)" else "Standar (64 kbps)",
                    onClick = onOpenAudioManagement
                )
                ActionRow(
                    title = if (isEnglish) "Select Qari" else "Pilih Qari",
                    subtitle = when (settings.adzanSound) {
                        AdhanStyle.MISHARY -> "Mishary Rashid Alafasy"
                        AdhanStyle.ABDULBASET -> "Abdul Baset Abdussamad"
                        AdhanStyle.MAKKAH -> "Makkah"
                        AdhanStyle.MADINAH -> "Madinah"
                        AdhanStyle.DEFAULT -> if (isEnglish) "System Default" else "Default Android"
                    },
                    onClick = onOpenAudioManagement
                )
            }
        }

        item {
            SectionLabel(if (isEnglish) "Location & Time" else "Lokasi & Waktu")
            SanctuaryCard {
                ToggleRow(
                    title = if (isEnglish) "Auto Detect Location" else "Deteksi Lokasi Otomatis",
                    subtitle = settings.locationName,
                    checked = settings.autoLocation,
                    onCheckedChange = viewModel::setAutoLocation
                )
                ActionRow(
                    title = if (isEnglish) "Calculation Method" else "Metode Perhitungan (Mazhab)",
                    subtitle = "${settings.prayerCalculationMethod.label} • ${settings.asrMadhhab.label}",
                    onClick = onOpenLocationSettings
                )
            }
        }

        item {
            SectionLabel(if (isEnglish) "Other" else "Lainnya")
            SanctuaryCard {
                ActionRow(
                    title = if (isEnglish) "App Updates" else "Pembaruan Aplikasi",
                    subtitle = if (updateState.hasUpdate) {
                        if (isEnglish) "Version ${updateState.latestVersionName} available" else "Versi ${updateState.latestVersionName} tersedia"
                    } else {
                        if (isEnglish) "Already on the latest build" else "Sudah di versi terbaru"
                    },
                    onClick = onOpenUpdateCenter
                )
                ActionRow(
                    title = if (isEnglish) "Reminders & Goals" else "Reminder & Target",
                    subtitle = if (isEnglish) "Daily routine and worship flow" else "Rutinitas harian dan progres ibadah",
                    onClick = onOpenSmartReminders
                )
                ActionRow(
                    title = if (isEnglish) "More Tools" else "Fitur Tambahan",
                    subtitle = if (isEnglish) "Audio info, widget preview, and states" else "Info audio, pratinjau widget, dan empty state",
                    onClick = onOpenBackgroundAudioInfo
                )
            }
        }

        item {
            Text(
                text = "NurApp Version ${BuildConfig.VERSION_NAME} (${if (updateState.hasUpdate) "Update Ready" else "Stable"})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
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
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        androidx.compose.material3.Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}
