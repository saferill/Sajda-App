package com.sajda.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val supportedActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED
        )

        if (action !in supportedActions) return

        val appContext = context.applicationContext
        val shouldShowRecoveryReminder = action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                Log.d(TAG, "System event received: $action, repairing adhan alarms")
                AdzanScheduler.repairNextAlarm(
                    context = appContext,
                    referenceTime = LocalDateTime.now()
                )
                if (shouldShowRecoveryReminder) {
                    AdhanRecoveryNotifier.syncAfterSystemEvent(
                        context = appContext,
                        action = action
                    )
                } else {
                    AdhanRecoveryNotifier.cancel(appContext)
                }
            }.onFailure { error ->
                Log.e(TAG, "Failed handling system event $action", error)
            }
            pendingResult.finish()
        }
        PrayerScheduleWorker.enqueueImmediate(appContext)
        PrayerScheduleWorker.ensurePeriodic(appContext)
    }

    companion object {
        private const val TAG = "SajdaAdhan"
    }
}
