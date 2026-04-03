package com.sajda.app.ui.screen

import com.sajda.app.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesomeMotion
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SystemUpdateAlt
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajda.app.ui.component.HeroCard
import com.sajda.app.ui.component.SajdaLogoTile
import com.sajda.app.ui.component.SajdaTopAction
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.viewmodel.AppUpdateUiState
import com.sajda.app.ui.viewmodel.SettingsViewModel

private data class SettingsItem(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

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
    onOpenEmptyState: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val primaryItems = listOf(
        SettingsItem(
            title = "Adhan & Notifications",
            subtitle = "Kelola alarm sholat, suara, dan vibrasi",
            icon = Icons.Rounded.NotificationsActive,
            onClick = onOpenAdhanSettings
        ),
        SettingsItem(
            title = "Appearance",
            subtitle = "Tema, mode malam, dan mode fokus ibadah",
            icon = Icons.Rounded.Palette,
            onClick = onOpenAppearanceSettings
        ),
        SettingsItem(
            title = "Location & Calculation",
            subtitle = "Lokasi manual, GPS, dan arah kiblat",
            icon = Icons.Rounded.Explore,
            onClick = onOpenLocationSettings
        ),
        SettingsItem(
            title = "Language",
            subtitle = "Indonesia, English, Arabic-only reading",
            icon = Icons.Rounded.Language,
            onClick = onOpenLanguageSettings
        ),
        SettingsItem(
            title = "App Updates",
            subtitle = if (updateState.hasUpdate) {
                "Versi ${updateState.latestVersionName} siap diunduh"
            } else {
                "Cek update otomatis dan pasang build terbaru"
            },
            icon = Icons.Rounded.SystemUpdateAlt,
            onClick = onOpenUpdateCenter
        )
    )

    val journeyItems = listOf(
        SettingsItem(
            title = "Audio Management",
            subtitle = "Unduhan murattal, storage, dan player",
            icon = Icons.Rounded.VolumeUp,
            onClick = onOpenAudioManagement
        ),
        SettingsItem(
            title = "Worship Goals",
            subtitle = "Progress ayat harian, streak, dan target pekanan",
            icon = Icons.Rounded.AutoAwesomeMotion,
            onClick = onOpenWorshipProgress
        ),
        SettingsItem(
            title = "Smart Reminder",
            subtitle = "Reminder Qur'an, dzikir pagi, dan dzikir sore",
            icon = Icons.Rounded.Schedule,
            onClick = onOpenSmartReminders
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 180.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sajda v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SajdaTopAction(
                    icon = Icons.Rounded.ChevronRight,
                    label = "Info",
                    onClick = onOpenBackgroundAudioInfo
                )
            }
        }

        item {
            SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SajdaLogoTile(size = 64)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Spiritual Journey",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Assalamu Alaikum",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${settings.streakCount} days streak | ${settings.locationName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            if (updateState.hasUpdate) {
                HeroCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Update tersedia",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
                        )
                        Text(
                            text = updateState.releaseName.ifBlank { "Sajda App ${updateState.latestVersionName}" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Buka App Updates untuk mengunduh dan memasang versi terbaru.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "APP PREFERENCES",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                primaryItems.forEach { item ->
                    SettingsListCard(item)
                }
            }
        }

        item {
            Text(
                text = "CONTENT & JOURNEY",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                journeyItems.forEach { item ->
                    SettingsListCard(item)
                }
            }
        }

        item {
            HeroCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Support the Ummah",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Akses info player background, preview widget, dan empty state premium dari pusat pengaturan Sajda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingsCtaChip("Audio Info", onOpenBackgroundAudioInfo)
                        SettingsCtaChip("Widget", onOpenWidgetPreview)
                        SettingsCtaChip("Empty State", onOpenEmptyState)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsListCard(item: SettingsItem) {
    SanctuaryCard(
        modifier = Modifier.clickable(onClick = item.onClick),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsCtaChip(
    label: String,
    onClick: () -> Unit
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}
