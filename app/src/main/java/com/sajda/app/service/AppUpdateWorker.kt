package com.sajda.app.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.AppUpdateRepository
import com.sajda.app.util.AppUpdateNotifier
import com.sajda.app.util.Constants
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class AppUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val preferencesDataStore = PreferencesDataStore(applicationContext)
        val settings = preferencesDataStore.settingsFlow.first()
        if (!settings.autoUpdateCheckEnabled) return Result.success()

        val repository = AppUpdateRepository(applicationContext)
        val updateInfo = repository.fetchLatestRelease()
        preferencesDataStore.setLastUpdateCheckAt()

        if (updateInfo != null) {
            val lastNotifiedVersion = preferencesDataStore.getLastNotifiedUpdateVersion()
            if (lastNotifiedVersion != updateInfo.versionName) {
                AppUpdateNotifier.notifyUpdateAvailable(applicationContext, updateInfo)
                preferencesDataStore.setLastNotifiedUpdateVersion(updateInfo.versionName)
            }
        }

        return Result.success()
    }

    companion object {
        fun enqueueImmediate(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                Constants.APP_UPDATE_WORK_NAME + "_immediate",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<AppUpdateWorker>().build()
            )
        }

        fun ensurePeriodic(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                Constants.APP_UPDATE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<AppUpdateWorker>(12, TimeUnit.HOURS).build()
            )
        }
    }
}
