package com.sajda.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AdzanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Constants.ACTION_TRIGGER_ADZAN) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val prayerName = intent.getStringExtra(Constants.EXTRA_PRAYER_NAME) ?: "Waktu sholat"
        val prayerTime = intent.getStringExtra(Constants.EXTRA_PRAYER_TIME).orEmpty()
        val prayerDate = intent.getStringExtra(Constants.EXTRA_PRAYER_DATE).orEmpty()
        val locationName = intent.getStringExtra(Constants.EXTRA_LOCATION_NAME).orEmpty()

        Log.d(TAG, "Alarm fired for $prayerName at $prayerTime on $prayerDate ($locationName)")

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                PreferencesDataStore(appContext).updateAdhanLastEvent(
                    prayerName = prayerName,
                    status = if (prayerTime.isBlank()) "Alarm dipicu" else "Alarm dipicu • $prayerTime"
                )
                AdzanScheduler.repairNextAlarm(
                    context = appContext,
                    referenceTime = LocalDateTime.now().plusSeconds(30)
                )
            }.onFailure {
                Log.e(TAG, "Failed to repair next adhan after $prayerName", it)
                PreferencesDataStore(appContext).updateAdhanLastEvent(
                    prayerName = prayerName,
                    status = "Alarm dipicu, tapi gagal memasang jadwal berikutnya"
                )
            }
            pendingResult.finish()
        }

        val serviceIntent = Intent(appContext, AdzanService::class.java).apply {
            action = Constants.ACTION_TRIGGER_ADZAN
            putExtra(Constants.EXTRA_PRAYER_NAME, prayerName)
            putExtra(Constants.EXTRA_PRAYER_TIME, prayerTime)
            putExtra(Constants.EXTRA_PRAYER_DATE, prayerDate)
            putExtra(Constants.EXTRA_LOCATION_NAME, locationName)
        }
        runCatching {
            ContextCompat.startForegroundService(appContext, serviceIntent)
        }.onFailure {
            Log.e(TAG, "Failed to start AdzanService for $prayerName", it)
            CoroutineScope(Dispatchers.IO).launch {
                PreferencesDataStore(appContext).updateAdhanLastEvent(
                    prayerName = prayerName,
                    status = "Alarm dipicu, tapi service adzan gagal dibuka"
                )
            }
        }
    }

    companion object {
        private const val TAG = "SajdaAdhan"
    }
}
