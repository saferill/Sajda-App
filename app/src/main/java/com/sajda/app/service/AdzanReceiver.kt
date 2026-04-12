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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AdzanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Constants.ACTION_TRIGGER_ADZAN) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        val wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "SajdaApp::AdhanReceiverWakeLock")
        wakeLock.acquire(15000L)

        val prayerName = intent.getStringExtra(Constants.EXTRA_PRAYER_NAME) ?: "Waktu sholat"
        val prayerKey = intent.getStringExtra(Constants.EXTRA_PRAYER_KEY) ?: "fajr"
        val prayerTime = intent.getStringExtra(Constants.EXTRA_PRAYER_TIME).orEmpty()
        val prayerDate = intent.getStringExtra(Constants.EXTRA_PRAYER_DATE).orEmpty()
        val locationName = intent.getStringExtra(Constants.EXTRA_LOCATION_NAME).orEmpty()
        val serviceAlreadyRunning = AdzanService.isRunning()

        Log.d(TAG, "Alarm fired for $prayerName at $prayerTime on $prayerDate ($locationName)")
        AdzanAlertNotifier.showTriggeredAlert(
            context = appContext,
            prayerName = prayerName,
            prayerKey = prayerKey,
            prayerTime = prayerTime,
            locationName = locationName
        )

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                PreferencesDataStore(appContext).updateAdhanLastEvent(
                    prayerName = prayerName,
                    status = if (prayerTime.isBlank()) {
                        context.getString(com.sajda.app.R.string.alarm_triggered)
                    } else {
                        context.getString(com.sajda.app.R.string.alarm_triggered) + " | $prayerTime"
                    },
                    details = buildString {
                        append(context.getString(com.sajda.app.R.string.date))
                        append(prayerDate)
                        append(". ")
                        if (locationName.isNotBlank()) {
                            append(context.getString(com.sajda.app.R.string.location))
                            append(locationName)
                            append(".")
                        }
                    }
                )
                AdzanScheduler.repairNextAlarm(
                    context = appContext,
                    referenceTime = LocalDateTime.now().plusSeconds(30)
                )
            }.onFailure { error ->
                Log.e(TAG, "Failed to repair next adhan after $prayerName", error)
                val fallbackNote = runCatching {
                    AdzanScheduler.scheduleEmergencyNextDayFallback(
                        context = appContext,
                        prayerNameLabel = prayerName,
                        prayerKey = prayerKey,
                        prayerTimeValue = prayerTime,
                        prayerDate = prayerDate,
                        locationName = locationName
                    )
                    context.getString(com.sajda.app.R.string.emergency_next_day_fallback_armed)
                }.getOrElse { fallbackError ->
                    context.getString(com.sajda.app.R.string.emergency_fallback_also_failed) + fallbackError.message.orEmpty()
                }
                PreferencesDataStore(appContext).updateAdhanLastEvent(
                    prayerName = prayerName,
                    status = context.getString(com.sajda.app.R.string.alarm_triggered_but_the_next_schedule_co),
                    details = listOf(
                        error.message.orEmpty(),
                        fallbackNote
                    ).filter { it.isNotBlank() }.joinToString(" | ")
                )
            }
            pendingResult.finish()
        }

        if (serviceAlreadyRunning) {
            Log.d(TAG, "Skipping duplicate AdzanService start for $prayerName at $prayerTime because playback is active")
            return
        }

        val serviceIntent = Intent(appContext, AdzanService::class.java).apply {
            action = Constants.ACTION_TRIGGER_ADZAN
            putExtra(Constants.EXTRA_PRAYER_NAME, prayerName)
            putExtra(Constants.EXTRA_PRAYER_KEY, prayerKey)
            putExtra(Constants.EXTRA_PRAYER_TIME, prayerTime)
            putExtra(Constants.EXTRA_PRAYER_DATE, prayerDate)
            putExtra(Constants.EXTRA_LOCATION_NAME, locationName)
        }
        runCatching {
            ContextCompat.startForegroundService(appContext, serviceIntent)
        }.onFailure { error ->
            Log.e(TAG, "Failed to start AdzanService for $prayerName", error)
            AdzanAlertNotifier.showServiceFailureAlert(
                context = appContext,
                prayerName = prayerName,
                prayerKey = prayerKey,
                prayerTime = prayerTime,
                locationName = locationName,
                reason = error.message.orEmpty()
            )
            CoroutineScope(Dispatchers.IO).launch {
                PreferencesDataStore(appContext).updateAdhanLastEvent(
                    prayerName = prayerName,
                    status = context.getString(com.sajda.app.R.string.alarm_triggered_but_the_adhan_service_co),
                    details = error.message.orEmpty()
                )
            }
        }
    }

    companion object {
        private const val TAG = "SajdaAdhan"
    }
}
