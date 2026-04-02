package com.sajda.app.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

data class AdhanReadiness(
    val notificationPermissionGranted: Boolean,
    val appNotificationsEnabled: Boolean,
    val adhanChannelEnabled: Boolean,
    val exactAlarmGranted: Boolean,
    val batteryOptimizationIgnored: Boolean,
    val silentModeActive: Boolean,
    val alarmVolumeLevel: Int
)

object AdhanSystemHelper {

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun areAppNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun isAdhanChannelEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return areAppNotificationsEnabled(context)
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channel = notificationManager?.getNotificationChannel(Constants.ADZAN_NOTIFICATION_CHANNEL)
        return channel?.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun isSilentModeActive(context: Context): Boolean {
        val audioManager = context.getSystemService(AudioManager::class.java)
        return audioManager?.ringerMode != AudioManager.RINGER_MODE_NORMAL
    }

    fun alarmVolumeLevel(context: Context): Int {
        val audioManager = context.getSystemService(AudioManager::class.java)
        return audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 0
    }

    fun buildReadiness(context: Context): AdhanReadiness {
        return AdhanReadiness(
            notificationPermissionGranted = hasNotificationPermission(context),
            appNotificationsEnabled = areAppNotificationsEnabled(context),
            adhanChannelEnabled = isAdhanChannelEnabled(context),
            exactAlarmGranted = canScheduleExactAlarms(context),
            batteryOptimizationIgnored = isIgnoringBatteryOptimizations(context),
            silentModeActive = isSilentModeActive(context),
            alarmVolumeLevel = alarmVolumeLevel(context)
        )
    }

    fun openNotificationSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
    }

    fun openExactAlarmSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        runCatching {
            context.startActivity(intent)
        }.recoverCatching {
            openNotificationSettings(context)
        }
    }

    fun openBatteryOptimizationSettings(context: Context) {
        val directIntent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(directIntent)
        } catch (_: ActivityNotFoundException) {
            context.startActivity(fallbackIntent)
        } catch (_: SecurityException) {
            context.startActivity(fallbackIntent)
        }
    }
}
