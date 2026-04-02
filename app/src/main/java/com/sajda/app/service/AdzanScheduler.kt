package com.sajda.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.PrayerTime
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.util.Constants
import com.sajda.app.util.DateTimeUtils
import com.sajda.app.widget.PrayerTimesWidgetUpdater
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AdzanScheduler(private val context: Context) {

    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun reschedule(prayerTimes: List<PrayerTime>, settings: UserSettings) {
        cancelUpcoming(prayerTimes)
        val preferencesDataStore = PreferencesDataStore(appContext)
        if (!settings.adzanEnabled) {
            preferencesDataStore.updateNextScheduledPrayer("", "")
            return
        }

        prayerTimes.forEach { prayerTime ->
            PrayerName.entries.forEach { prayerName ->
                if (isEnabled(settings, prayerName)) {
                    schedulePrayer(prayerTime, prayerName)
                }
            }
        }

        val nextUpcoming = findNextScheduledPrayer(prayerTimes, settings)
        preferencesDataStore.updateNextScheduledPrayer(
            prayerName = nextUpcoming?.first?.label.orEmpty(),
            scheduledAt = nextUpcoming?.second?.let(DateTimeUtils::dateTimeString).orEmpty()
        )
        PrayerTimesWidgetUpdater.enqueueUpdate(appContext)
    }

    fun cancelUpcoming(prayerTimes: List<PrayerTime>) {
        prayerTimes.forEach { prayerTime ->
            PrayerName.entries.forEach { prayerName ->
                val pendingIntent = pendingIntentFor(prayerTime.date, prayerName)
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    private fun schedulePrayer(prayerTime: PrayerTime, prayerName: PrayerName) {
        val triggerDateTime = LocalDateTime.of(LocalDate.parse(prayerTime.date), LocalTime.parse(timeFor(prayerTime, prayerName)))
        if (triggerDateTime.isBefore(LocalDateTime.now())) return

        val triggerAtMillis = triggerDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val pendingIntent = pendingIntentFor(prayerTime.date, prayerName)
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun pendingIntentFor(date: String, prayerName: PrayerName): PendingIntent {
        val intent = Intent(appContext, AdzanReceiver::class.java).apply {
            action = Constants.ACTION_TRIGGER_ADZAN
            putExtra(Constants.EXTRA_PRAYER_NAME, prayerName.label)
        }
        return PendingIntent.getBroadcast(
            appContext,
            "$date-${prayerName.name}".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun isEnabled(settings: UserSettings, prayerName: PrayerName): Boolean {
        return when (prayerName) {
            PrayerName.FAJR -> settings.fajrAdzanEnabled
            PrayerName.DHUHR -> settings.dhuhrAdzanEnabled
            PrayerName.ASR -> settings.asrAdzanEnabled
            PrayerName.MAGHRIB -> settings.maghribAdzanEnabled
            PrayerName.ISHA -> settings.ishaAdzanEnabled
        }
    }

    private fun timeFor(prayerTime: PrayerTime, prayerName: PrayerName): String {
        return when (prayerName) {
            PrayerName.FAJR -> prayerTime.fajr
            PrayerName.DHUHR -> prayerTime.dhuhr
            PrayerName.ASR -> prayerTime.asr
            PrayerName.MAGHRIB -> prayerTime.maghrib
            PrayerName.ISHA -> prayerTime.isha
        }
    }

    private fun findNextScheduledPrayer(
        prayerTimes: List<PrayerTime>,
        settings: UserSettings
    ): Pair<PrayerName, LocalDateTime>? {
        val now = LocalDateTime.now()
        return prayerTimes.asSequence().flatMap { prayerTime ->
            PrayerName.entries.asSequence()
                .filter { isEnabled(settings, it) }
                .map { prayerName ->
                    prayerName to LocalDateTime.of(
                        LocalDate.parse(prayerTime.date),
                        LocalTime.parse(timeFor(prayerTime, prayerName))
                    )
                }
        }.filter { (_, scheduledAt) ->
            !scheduledAt.isBefore(now)
        }.minByOrNull { (_, scheduledAt) ->
            scheduledAt
        }
    }
}
