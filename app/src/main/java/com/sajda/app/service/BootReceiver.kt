package com.sajda.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (
            intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent?.action == Intent.ACTION_TIME_CHANGED ||
            intent?.action == Intent.ACTION_TIMEZONE_CHANGED ||
            intent?.action == Intent.ACTION_DATE_CHANGED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    AdzanScheduler.repairNextAlarm(
                        context = context.applicationContext,
                        referenceTime = LocalDateTime.now()
                    )
                }
                pendingResult.finish()
            }
            PrayerScheduleWorker.enqueueImmediate(context)
            PrayerScheduleWorker.ensurePeriodic(context)
        }
    }
}
