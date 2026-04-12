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
import com.sajda.app.ui.viewmodel.AppUpdateViewModel
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.util.Constants

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UpdateCenterScreen(
    settings: UserSettings,
    updateState: AppUpdateUiState,
    viewModel: AppUpdateViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    OverlayShell(
        title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.app_updates),
        subtitle = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.installed_v_updatestate_currentversionna),
        onBack = onBack
    ) {
        SanctuaryCard {
            SectionHeader(
                eyebrow = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.status),
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.manual_update)
            )
            Text(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.installed_version_updatestate_currentver),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = if (updateState.hasUpdate) {
                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.new_version_available_updatestate_latest)
                } else {
                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.tap_check_now_to_look_for_the_latest_rel)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (updateState.lastCheckedAt.isNotBlank()) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.last_checked_updatestate_lastcheckedat),
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
                        androidx.compose.ui.res.stringResource(com.sajda.app.R.string.checking)
                    } else {
                        androidx.compose.ui.res.stringResource(com.sajda.app.R.string.check_now)
                    },
                    selected = false,
                    onClick = { if (!updateState.isChecking) viewModel.checkForUpdates() }
                )
            }
        }

        SanctuaryCard {
            SectionHeader(
                eyebrow = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.how_it_works),
                title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.update_from_github_releases)
            )
            Text(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.nurapp_no_longer_checks_for_updates_auto),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.android_may_still_ask_for_install_from_t),
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
                            androidx.compose.ui.res.stringResource(com.sajda.app.R.string.downloading)
                        } else {
                            androidx.compose.ui.res.stringResource(com.sajda.app.R.string.download_update)
                        },
                        selected = true,
                        onClick = { if (!updateState.isDownloading) viewModel.downloadLatestUpdate() }
                    )
                    if (updateState.canInstallDownloadedUpdate) {
                        ChoiceChip(
                            label = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.install_now),
                            selected = true,
                            onClick = viewModel::installDownloadedUpdate
                        )
                    }
                    ChoiceChip(
                        label = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.open_releases),
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
                    eyebrow = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.current),
                    title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.no_update_detected)
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.use_the_check_now_button_to_manually_loo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
