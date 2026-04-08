package com.sajda.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sajda.app.MainActivity
import com.sajda.app.R
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.util.Constants
import com.sajda.app.util.localizedPrayerName
import com.sajda.app.util.pick
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object AdzanAlertNotifier {

    fun showServiceFailureAlert(
        context: Context,
        prayerName: String,
        prayerKey: String,
        prayerTime: String,
        locationName: String,
        reason: String = ""
    ) {
        val appContext = context.applicationContext
        val language = runBlocking { PreferencesDataStore(appContext).settingsFlow.first().appLanguage }
        val displayName = localizedPrayerName(prayerName, language)
        val alertTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        ensureChannel(appContext, alertTone)

        val openPrayerIntent = Intent(appContext, MainActivity::class.java).apply {
            action = Constants.ACTION_OPEN_PRAYER_TAB
            putExtra(Constants.EXTRA_OPEN_TAB, "prayer")
            putExtra(Constants.EXTRA_PRAYER_NAME, prayerName)
            putExtra(Constants.EXTRA_PRAYER_KEY, prayerKey)
            putExtra(Constants.EXTRA_PRAYER_TIME, prayerTime)
            putExtra(Constants.EXTRA_LOCATION_NAME, locationName)
            putExtra("is_adhan", true)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val requestCode = "$prayerKey-$prayerTime-$locationName-alert".hashCode()
        val contentIntent = PendingIntent.getActivity(
            appContext,
            requestCode,
            openPrayerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenIntent = Intent(appContext, MainActivity::class.java).apply {
            action = Constants.ACTION_OPEN_PRAYER_TAB
            putExtra(Constants.EXTRA_OPEN_TAB, "prayer")
            putExtra(Constants.EXTRA_PRAYER_NAME, prayerName)
            putExtra(Constants.EXTRA_PRAYER_KEY, prayerKey)
            putExtra(Constants.EXTRA_PRAYER_TIME, prayerTime)
            putExtra(Constants.EXTRA_LOCATION_NAME, locationName)
            putExtra("is_adhan", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            appContext,
            requestCode + 1,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val metaLine = listOf(
            prayerTime.takeIf { it.isNotBlank() },
            locationName.takeIf { it.isNotBlank() }
        ).joinToString(" - ")

        val details = buildString {
            append(
                language.pick(
                    "Service adzan utama gagal dibuka. Notifikasi cadangan dipakai supaya pengingat tetap muncul.",
                    "The main adhan service could not start. A backup alert is shown so the reminder is still delivered."
                )
            )
            if (reason.isNotBlank()) {
                append(' ')
                append(language.pick("Penyebab: ", "Reason: "))
                append(reason)
            }
        }

        val summaryText = buildString {
            append(language.pick("Saatnya sholat ", "Time for "))
            append(displayName)
            if (metaLine.isNotBlank()) {
                append(" - ")
                append(metaLine)
            }
        }

        val notification = NotificationCompat.Builder(appContext, Constants.ADZAN_ALERT_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(language.pick("Waktu $displayName telah tiba", "$displayName time has arrived"))
            .setContentText(summaryText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(details))
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setSound(alertTone)
            .setVibrate(VIBRATION_PATTERN)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setTimeoutAfter(5 * 60_000L)
            .build()

        NotificationManagerCompat.from(appContext).notify(Constants.ADZAN_NOTIFICATION_ID, notification)
    }

    private fun ensureChannel(context: Context, alertTone: android.net.Uri?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            Constants.ADZAN_ALERT_NOTIFICATION_CHANNEL,
            "NurApp Adhan Alert",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Backup alert used when the main adhan service cannot start"
            enableVibration(true)
            vibrationPattern = VIBRATION_PATTERN
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            setSound(alertTone, audioAttributes)
        }
        manager.createNotificationChannel(channel)
    }

    private val VIBRATION_PATTERN = longArrayOf(0, 500, 300, 500, 300, 700)
}
