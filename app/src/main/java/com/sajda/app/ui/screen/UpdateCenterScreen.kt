package com.sajda.app.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.component.SectionHeader
import com.sajda.app.ui.viewmodel.AppUpdateUiState
import com.sajda.app.ui.viewmodel.SettingsViewModel
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.util.Constants
import com.sajda.app.util.pick

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UpdateCenterScreen(
    settings: UserSettings,
    updateState: AppUpdateUiState,
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    OverlayShell(
        title = settings.pick("Update Aplikasi", "App Updates"),
        subtitle = settings.pick(
            "Terpasang v${updateState.currentVersionName}",
            "Installed v${updateState.currentVersionName}"
        ),
        onBack = onBack
    ) {
        SanctuaryCard {
            SectionHeader(
                eyebrow = settings.pick("Status", "Status"),
                title = settings.pick("Update manual", "Manual update")
            )
            Text(
                text = settings.pick(
                    "Versi terpasang: ${updateState.currentVersionName}",
                    "Installed version: ${updateState.currentVersionName}"
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = if (updateState.hasUpdate) {
                    settings.pick(
                        "Versi terbaru tersedia: ${updateState.latestVersionName}",
                        "New version available: ${updateState.latestVersionName}"
                    )
                } else {
                    settings.pick(
                        "Tekan Cek sekarang untuk mencari rilis terbaru.",
                        "Tap Check now to look for the latest release."
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (updateState.lastCheckedAt.isNotBlank()) {
                Text(
                    text = settings.pick(
                        "Terakhir dicek: ${updateState.lastCheckedAt}",
                        "Last checked: ${updateState.lastCheckedAt}"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            updateState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChoiceChip(
                    label = if (updateState.isChecking) {
                        settings.pick("Mengecek...", "Checking...")
                    } else {
                        settings.pick("Cek sekarang", "Check now")
                    },
                    selected = false,
                    onClick = { if (!updateState.isChecking) viewModel.checkForUpdates() }
                )
            }
        }

        SanctuaryCard {
            SectionHeader(
                eyebrow = settings.pick("Cara kerja", "How it works"),
                title = settings.pick("Update dari GitHub Releases", "Update from GitHub Releases")
            )
            Text(
                text = settings.pick(
                    "NurApp tidak lagi mengecek update otomatis saat aplikasi dibuka. Update hanya berjalan saat Anda menekan Cek sekarang, lalu unduh dan pasang dari halaman ini.",
                    "NurApp no longer checks for updates automatically when the app opens. Updates only run when you tap Check now, then download and install from this page."
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = settings.pick(
                    "Android tetap bisa meminta izin pemasangan dari sumber ini sebelum installer muncul.",
                    "Android may still ask for install-from-this-source permission before the installer appears."
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (updateState.hasUpdate) {
            SanctuaryCard {
                SectionHeader(
                    eyebrow = "Rilis terbaru",
                    title = updateState.releaseName.ifBlank { "Versi ${updateState.latestVersionName}" }
                )
                if (updateState.notes.isNotBlank()) {
                    Text(
                        text = updateState.notes.lineSequence().take(10).joinToString("\n"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Ada rilis baru. Unduh sekarang untuk memasang build NurApp terbaru.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChoiceChip(
                        label = if (updateState.isDownloading) {
                            settings.pick("Mengunduh...", "Downloading...")
                        } else {
                            settings.pick("Unduh update", "Download update")
                        },
                        selected = true,
                        onClick = { if (!updateState.isDownloading) viewModel.downloadLatestUpdate() }
                    )
                    if (updateState.canInstallDownloadedUpdate) {
                        ChoiceChip(
                            label = settings.pick("Pasang sekarang", "Install now"),
                            selected = true,
                            onClick = viewModel::installDownloadedUpdate
                        )
                    }
                    ChoiceChip(
                        label = settings.pick("Buka releases", "Open releases"),
                        selected = false,
                        onClick = {
                            val releaseUrl = updateState.releasePageUrl.ifBlank { Constants.UPDATE_RELEASES_PAGE_URL }
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl)))
                        }
                    )
                }
            }
        } else {
            SanctuaryCard {
                SectionHeader(
                    eyebrow = settings.pick("Saat ini", "Current"),
                    title = settings.pick("Belum ada update terdeteksi", "No update detected")
                )
                Text(
                    text = settings.pick(
                        "Gunakan tombol Cek sekarang untuk mencari rilis terbaru secara manual.",
                        "Use the Check now button to manually look for the latest release."
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
