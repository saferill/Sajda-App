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
        title = "App Updates",
        subtitle = "Terpasang v${updateState.currentVersionName}",
        onBack = onBack
    ) {
        SettingToggleCard("Cek update otomatis", settings.autoUpdateCheckEnabled) {
            viewModel.setAutoUpdateCheckEnabled(it)
        }

        SanctuaryCard {
            SectionHeader(eyebrow = "Status", title = "Kesehatan update")
            Text(
                text = "Versi terpasang: ${updateState.currentVersionName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = if (updateState.hasUpdate) {
                    "Versi terbaru tersedia: ${updateState.latestVersionName}"
                } else {
                    "Belum ada rilis publik yang lebih baru saat ini."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (updateState.lastCheckedAt.isNotBlank()) {
                Text(
                    text = "Terakhir dicek: ${updateState.lastCheckedAt}",
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
                    label = if (updateState.isChecking) "Mengecek..." else "Cek sekarang",
                    selected = false,
                    onClick = { if (!updateState.isChecking) viewModel.checkForUpdates() }
                )
            }
        }

        SanctuaryCard {
            SectionHeader(eyebrow = "Cara kerja", title = "Sumber rilis")
            Text(
                text = "Sajda App mengecek GitHub Releases untuk mencari APK yang lebih baru. Saat ada rilis baru, pengguna bisa mengunduh update dari dalam aplikasi lalu melanjutkan instalasi lewat installer sistem.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Penting: Android masih bisa meminta verifikasi APK atau izin instalasi dari Sajda App bila update dibagikan di luar Google Play. Update yang benar-benar mulus di semua HP tetap butuh distribusi lewat Play Store.",
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
                        text = "Ada rilis baru. Unduh sekarang untuk memasang build Sajda App terbaru.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChoiceChip(
                        label = if (updateState.isDownloading) "Memulai..." else "Unduh update",
                        selected = true,
                        onClick = { if (!updateState.isDownloading) viewModel.downloadLatestUpdate() }
                    )
                    ChoiceChip(
                        label = "Buka releases",
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
                SectionHeader(eyebrow = "Saat ini", title = "Versi aplikasi sudah terbaru")
                Text(
                    text = "Saat Anda menerbitkan APK yang lebih baru ke GitHub Releases, Sajda App bisa memberi tahu pengguna yang sudah memasang aplikasi lalu membantu mereka memperbarui dari dalam aplikasi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
