package com.sajda.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sajda.app.MainActivity
import com.sajda.app.R
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.util.AdhanReadiness
import com.sajda.app.util.AdhanSystemHelper
import com.sajda.app.util.Constants
import kotlinx.coroutines.flow.first

object AdhanRecoveryNotifier {

    suspend fun syncAfterSystemEvent(context: Context, action: String?) {
        val appContext = context.applicationContext
        val preferencesDataStore = PreferencesDataStore(appContext)
        val settings = preferencesDataStore.settingsFlow.first()

        if (!settings.permissionSetupCompleted || !settings.adzanEnabled) {
            cancel(appContext)
            Log.d(TAG, "Skipping recovery reminder because permission setup/adzan is disabled")
            return
        }

        val readiness = AdhanSystemHelper.buildReadiness(appContext)
        val issues = buildIssues(appContext, settings, readiness)

        if (issues.isEmpty()) {
            cancel(appContext)
            preferencesDataStore.appendAdhanLog(
                prayerName = "Sistem",
                status = appContext.getString(R.string.adhan_recovery_completed_without_issues),
                details = describeEvent(appContext, action)
            )
            Log.d(TAG, "No post-$action adhan issue detected")
            return
        }

        ensureChannel(appContext)

        val contentIntent = PendingIntent.getActivity(
            appContext,
            RECOVERY_SETTINGS_REQUEST_CODE,
            Intent(appContext, MainActivity::class.java).apply {
                putExtra(Constants.EXTRA_OPEN_TAB, "settings")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val quickActionLabel = if (issues.any { it.kind == RecoveryIssueKind.LOCATION }) {
            appContext.getString(R.string.choose_location)
        } else {
            appContext.getString(R.string.open_prayer)
        }
        val quickActionIntent = PendingIntent.getActivity(
            appContext,
            RECOVERY_PRAYER_REQUEST_CODE,
            Intent(appContext, MainActivity::class.java).apply {
                putExtra(Constants.EXTRA_OPEN_TAB, "prayer")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = appContext.getString(R.string.review_adhan_readiness)
        val summary = appContext.getString(
            R.string.adhan_alarms_were_repaired_after_event,
            describeEventSuffix(appContext, action)
        )
        val detailLines = buildList {
            add(summary)
            add(
                appContext.getString(
                    R.string.still_needs_attention,
                    issues.joinToString(", ") { it.label }
                )
            )
        }

        val notification = NotificationCompat.Builder(appContext, Constants.ADZAN_RECOVERY_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(detailLines.last())
            .setStyle(NotificationCompat.BigTextStyle().bigText(detailLines.joinToString("\n")))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .addAction(
                android.R.drawable.ic_menu_mylocation,
                quickActionLabel,
                quickActionIntent
            )
            .build()

        NotificationManagerCompat.from(appContext).notify(
            Constants.ADZAN_RECOVERY_NOTIFICATION_ID,
            notification
        )

        preferencesDataStore.appendAdhanLog(
            prayerName = "Sistem",
            status = appContext.getString(R.string.adhan_recovery_reminder_was_shown),
            details = buildString {
                append(describeEvent(appContext, action))
                append(" | ")
                append(issues.joinToString(" | ") { it.label })
            }
        )

        Log.d(TAG, "Recovery reminder shown with ${issues.size} issue(s)")
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context.applicationContext).cancel(Constants.ADZAN_RECOVERY_NOTIFICATION_ID)
    }

    private fun buildIssues(
        context: Context,
        settings: UserSettings,
        readiness: AdhanReadiness
    ): List<RecoveryIssue> {
        val issues = mutableListOf<RecoveryIssue>()

        if (!readiness.exactAlarmGranted) {
            issues += RecoveryIssue(
                kind = RecoveryIssueKind.SYSTEM,
                label = context.getString(R.string.exact_alarms)
            )
        }
        if (!readiness.batteryOptimizationIgnored) {
            issues += RecoveryIssue(
                kind = RecoveryIssueKind.SYSTEM,
                label = context.getString(R.string.battery_optimization_2)
            )
        }
        if (!readiness.notificationPermissionGranted || !readiness.appNotificationsEnabled || !readiness.adhanChannelEnabled) {
            issues += RecoveryIssue(
                kind = RecoveryIssueKind.SYSTEM,
                label = context.getString(R.string.adhan_notifications)
            )
        }
        if (settings.locationName.isBlank()) {
            issues += RecoveryIssue(
                kind = RecoveryIssueKind.LOCATION,
                label = context.getString(R.string.active_location)
            )
        } else if (settings.autoLocation && !readiness.locationPermissionGranted) {
            issues += RecoveryIssue(
                kind = RecoveryIssueKind.LOCATION,
                label = context.getString(R.string.location_permission)
            )
        }

        return issues
    }

    private fun describeEvent(context: Context, action: String?): String {
        return context.getString(
            R.string.recovery_trigger_label,
            describeEventSuffix(context, action)
        )
    }

    private fun describeEventSuffix(context: Context, action: String?): String {
        return when (action) {
            Intent.ACTION_BOOT_COMPLETED -> context.getString(R.string.after_the_phone_restarted)
            Intent.ACTION_MY_PACKAGE_REPLACED -> context.getString(R.string.after_the_app_update)
            else -> context.getString(R.string.after_system_resync)
        }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            Constants.ADZAN_RECOVERY_NOTIFICATION_CHANNEL,
            context.getString(R.string.nurapp_adhan_recovery_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.nurapp_adhan_recovery_channel_description)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    private data class RecoveryIssue(
        val kind: RecoveryIssueKind,
        val label: String
    )

    private enum class RecoveryIssueKind {
        SYSTEM,
        LOCATION
    }

    private const val TAG = "SajdaAdhan"
    private const val RECOVERY_SETTINGS_REQUEST_CODE = 9101
    private const val RECOVERY_PRAYER_REQUEST_CODE = 9102
}
