package com.sajda.app.service

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.AppUpdateRepository
import com.sajda.app.util.AppUpdateNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return

        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId <= 0L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val preferencesDataStore = PreferencesDataStore(context)
            val lastDownloadId = preferencesDataStore.getLastUpdateDownloadId()
            if (lastDownloadId != downloadId) {
                pendingResult.finish()
                return@launch
            }

            val repository = AppUpdateRepository(context)
            val downloadRecord = repository.getDownloadRecord(downloadId)
            when (downloadRecord?.status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    val installIntent = repository.buildInstallIntent(downloadId)
                    val permissionIntent = repository.buildUnknownSourcesIntent()
                    AppUpdateNotifier.notifyUpdateReady(
                        context = context,
                        versionLabel = "Sajda App versi terbaru",
                        installIntent = installIntent,
                        permissionIntent = permissionIntent,
                        needsInstallPermission = !repository.canRequestPackageInstalls()
                    )
                }

                DownloadManager.STATUS_FAILED -> {
                    AppUpdateNotifier.notifyUpdateDownloadFailed(context)
                }
            }
            pendingResult.finish()
        }
    }
}
