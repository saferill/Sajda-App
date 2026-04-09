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
    val locationPermissionGranted: Boolean,
    val silentModeActive: Boolean,
    val alarmVolumeLevel: Int
)

data class AdhanVendorTip(
    val vendor: String,
    val title: String,
    val steps: List<String>
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
            locationPermissionGranted = DeviceLocationHelper.hasLocationPermission(context),
            silentModeActive = isSilentModeActive(context),
            alarmVolumeLevel = alarmVolumeLevel(context)
        )
    }

    fun helpChecklist(): List<String> = listOf(
        "Pastikan notifikasi aplikasi diizinkan.",
        "Pastikan izin exact alarm aktif.",
        "Lepaskan pembatasan baterai untuk NurApp.",
        "Naikkan volume alarm perangkat di atas nol.",
        "Aktifkan Override silent mode jika Anda ingin adzan tetap bersuara saat HP silent.",
        "Buka aplikasi minimal sekali setelah reboot atau update bila vendor sangat agresif."
    )

    fun vendorTips(context: Context): List<AdhanVendorTip> {
        val currentVendor = Build.MANUFACTURER.orEmpty().trim().replaceFirstChar { it.uppercase() }
        val allTips = listOf(
            AdhanVendorTip(
                vendor = "Xiaomi",
                title = "Nonaktifkan pembatasan MIUI",
                steps = listOf(
                    "Buka Security > Battery > App battery saver > NurApp > No restrictions.",
                    "Aktifkan Autostart untuk NurApp.",
                    "Kunci aplikasi di recent apps bila perlu."
                )
            ),
            AdhanVendorTip(
                vendor = "Oppo",
                title = "Izinkan berjalan di background",
                steps = listOf(
                    "Buka App Management > NurApp > Allow auto launch.",
                    "Matikan Auto optimize battery untuk NurApp.",
                    "Izinkan notifikasi pop-up dan lock screen."
                )
            ),
            AdhanVendorTip(
                vendor = "Vivo",
                title = "Aktifkan autostart dan baterai tanpa batas",
                steps = listOf(
                    "Buka Settings > Battery > Background high power consumption > aktifkan NurApp.",
                    "Buka Settings > Apps > All apps > NurApp > Permissions dan cek notifikasi.",
                    "Cari menu Auto start management lalu aktifkan NurApp."
                )
            ),
            AdhanVendorTip(
                vendor = "Samsung",
                title = "Keluarkan dari sleeping apps",
                steps = listOf(
                    "Buka Battery and device care > Battery > Background usage limits.",
                    "Hapus NurApp dari Sleeping apps dan Deep sleeping apps.",
                    "Set App battery usage ke Unrestricted."
                )
            ),
            AdhanVendorTip(
                vendor = "Realme",
                title = "Izinkan startup manager",
                steps = listOf(
                    "Buka App Management > Auto-launch > aktifkan NurApp.",
                    "Matikan Battery optimization untuk NurApp.",
                    "Izinkan notifikasi lock screen dan banner."
                )
            )
        )

        return allTips.sortedByDescending { it.vendor.equals(currentVendor, ignoreCase = true) }
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

    fun openAppDetailsSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
