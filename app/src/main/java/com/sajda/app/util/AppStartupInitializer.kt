package com.sajda.app.util

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.data.repository.QuranRepository
import com.sajda.app.service.AdzanManager
import com.sajda.app.service.AdzanScheduler
import com.sajda.app.service.LocationWorker
import com.sajda.app.service.PrayerScheduleWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStartupInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val quranRepository: QuranRepository,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val preferencesDataStore: PreferencesDataStore,
    private val adzanScheduler: AdzanScheduler
) {

    suspend fun initialize() {
        cancelAutomaticUpdateChecks()
        LocationWorker.enqueuePeriodic(context)

        runCatching {
            quranRepository.seedIfNeeded(context)
            runCatching {
                AdzanManager(context).checkAndUpdateLocation()
            }.onFailure { error ->
                Log.e("AppStartupInitializer", "Gagal auto update lokasi adzan", error)
            }

            val refreshedSettings = preferencesDataStore.settingsFlow.first()
            val prayerTimes = prayerTimeRepository.getNextDaysPrayerTimes(30)
                .ifEmpty { prayerTimeRepository.refreshPrayerTimes(refreshedSettings) }
            adzanScheduler.reschedule(prayerTimes, refreshedSettings)
            
            PrayerScheduleWorker.enqueueImmediate(context)
            PrayerScheduleWorker.ensurePeriodic(context)
        }.onFailure { error ->
            Log.e("AppStartupInitializer", "Startup initialization failed", error)
        }
    }

    private fun cancelAutomaticUpdateChecks() {
        runCatching {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(Constants.APP_UPDATE_WORK_NAME)
            workManager.cancelUniqueWork("${Constants.APP_UPDATE_WORK_NAME}_immediate")
        }.onFailure { error ->
            Log.e("AppStartupInitializer", "Gagal membatalkan update otomatis", error)
        }
    }
}
