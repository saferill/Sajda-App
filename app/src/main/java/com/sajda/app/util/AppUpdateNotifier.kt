package com.sajda.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sajda.app.MainActivity
import com.sajda.app.R
import com.sajda.app.domain.model.AppUpdateInfo
import com.sajda.app.service.AppUpdateActionReceiver

object AppUpdateNotifier {

    fun notifyUpdateAvailable(context: Context, updateInfo: AppUpdateInfo) {
        ensureChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(Constants.EXTRA_OPEN_TAB, "settings")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            40,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val downloadIntent = Intent(context, AppUpdateActionReceiver::class.java).apply {
            action = Constants.ACTION_START_APP_UPDATE_DOWNLOAD
            putExtra(Constants.EXTRA_UPDATE_VERSION_NAME, updateInfo.versionName)
            putExtra(Constants.EXTRA_UPDATE_RELEASE_NAME, updateInfo.releaseName)
            putExtra(Constants.EXTRA_UPDATE_DOWNLOAD_URL, updateInfo.downloadUrl)
            putExtra(Constants.EXTRA_UPDATE_RELEASE_PAGE_URL, updateInfo.releasePageUrl)
            putExtra(Constants.EXTRA_UPDATE_PUBLISHED_AT, updateInfo.publishedAt)
        }
        val downloadPendingIntent = PendingIntent.getBroadcast(
            context,
            updateInfo.versionName.hashCode(),
            downloadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bodyText = buildString {
            append("Versi ")
            append(updateInfo.versionName)
            append(" sudah tersedia. Tekan Unduh untuk memperbarui NurApp.")
            if (updateInfo.notes.isNotBlank()) {
                append("\n\n")
                append(updateInfo.notes.lineSequence().take(4).joinToString("\n"))
            }
        }

        val notification = NotificationCompat.Builder(context, Constants.UPDATE_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Update NurApp tersedia")
            .setContentText("Versi ${updateInfo.versionName} siap diunduh.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(0, "Unduh", downloadPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(Constants.UPDATE_AVAILABLE_NOTIFICATION_ID, notification)
    }

    fun notifyUpdateDownloadStarted(context: Context, versionName: String) {
        ensureChannel(context)
        val notification = NotificationCompat.Builder(context, Constants.UPDATE_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Mengunduh update NurApp")
            .setContentText("Versi $versionName sedang diunduh.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(Constants.UPDATE_AVAILABLE_NOTIFICATION_ID, notification)
    }

    fun notifyUpdateReady(
        context: Context,
        versionLabel: String,
        installIntent: Intent?,
        permissionIntent: Intent?,
        needsInstallPermission: Boolean
    ) {
        ensureChannel(context)

        val contentIntent = when {
            needsInstallPermission && permissionIntent != null -> PendingIntent.getActivity(
                context,
                41,
                permissionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            installIntent != null -> PendingIntent.getActivity(
                context,
                42,
                installIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            else -> null
        }

        val builder = NotificationCompat.Builder(context, Constants.UPDATE_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Update NurApp siap dipasang")
            .setContentText(
                if (needsInstallPermission) {
                    "Izinkan instalasi dari NurApp, lalu lanjutkan memasang $versionLabel."
                } else {
                    "Tekan untuk memasang $versionLabel sekarang."
                }
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        contentIntent?.let { builder.setContentIntent(it) }
        if (needsInstallPermission && permissionIntent != null) {
            builder.addAction(0, "Izinkan", contentIntent)
        } else if (installIntent != null && contentIntent != null) {
            builder.addAction(0, "Pasang", contentIntent)
        }

        NotificationManagerCompat.from(context).notify(Constants.UPDATE_READY_NOTIFICATION_ID, builder.build())
    }

    fun notifyUpdateDownloadFailed(context: Context) {
        ensureChannel(context)
        val notification = NotificationCompat.Builder(context, Constants.UPDATE_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Unduhan update gagal")
            .setContentText("Coba cek update lagi dari Settings.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(Constants.UPDATE_READY_NOTIFICATION_ID, notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                Constants.UPDATE_NOTIFICATION_CHANNEL,
                "App Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for NurApp updates and installation prompts."
            }
            manager.createNotificationChannel(channel)
        }
    }
}
