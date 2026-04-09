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
import com.sajda.app.data.local.SajdaDatabase
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.util.Constants
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class PrayerScheduleWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = SajdaDatabase.getDatabase(applicationContext)
        val preferencesDataStore = PreferencesDataStore(applicationContext)
        val prayerTimeRepository = PrayerTimeRepository(database)
        val scheduler = AdzanScheduler(applicationContext)

        preferencesDataStore.resetDailyCounterIfNeeded()
        val settings = preferencesDataStore.settingsFlow.first()
        val prayerTimes = prayerTimeRepository.getNextDaysPrayerTimes(7)
            .ifEmpty { prayerTimeRepository.refreshPrayerTimes(settings) }
        scheduler.reschedule(prayerTimes, settings)
        return Result.success()
    }

    companion object {
        private const val REPAIR_INTERVAL_MINUTES = 15L

        fun enqueueImmediate(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                Constants.PRAYER_SCHEDULE_WORK_NAME + "_immediate",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<PrayerScheduleWorker>().build()
            )
        }

        fun ensurePeriodic(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                Constants.PRAYER_SCHEDULE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<PrayerScheduleWorker>(
                    REPAIR_INTERVAL_MINUTES,
                    TimeUnit.MINUTES
                ).build()
            )
        }
    }
}
