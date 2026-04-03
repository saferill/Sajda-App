package com.sajda.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sajda.app.data.repository.AppUpdateRepository
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.domain.model.AppUpdateInfo
import com.sajda.app.util.AppUpdateNotifier
import com.sajda.app.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppUpdateActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Constants.ACTION_START_APP_UPDATE_DOWNLOAD) return

        val versionName = intent.getStringExtra(Constants.EXTRA_UPDATE_VERSION_NAME).orEmpty()
        val releaseName = intent.getStringExtra(Constants.EXTRA_UPDATE_RELEASE_NAME).orEmpty()
        val downloadUrl = intent.getStringExtra(Constants.EXTRA_UPDATE_DOWNLOAD_URL).orEmpty()
        val releasePageUrl = intent.getStringExtra(Constants.EXTRA_UPDATE_RELEASE_PAGE_URL).orEmpty()
        val publishedAt = intent.getStringExtra(Constants.EXTRA_UPDATE_PUBLISHED_AT).orEmpty()

        if (versionName.isBlank() || downloadUrl.isBlank()) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val repository = AppUpdateRepository(context)
            val downloadId = repository.enqueueUpdateDownload(
            AppUpdateInfo(
                versionName = versionName,
                releaseName = releaseName.ifBlank { versionName },
                notes = "",
                downloadUrl = downloadUrl,
                releasePageUrl = releasePageUrl,
                publishedAt = publishedAt
            )
            )
            PreferencesDataStore(context).setLastUpdateDownloadId(downloadId)
            AppUpdateNotifier.notifyUpdateDownloadStarted(context, versionName)
            pendingResult.finish()
        }
    }
}
