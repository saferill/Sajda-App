package com.sajda.app.service

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import com.sajda.app.MainActivity
import com.sajda.app.data.local.SajdaDatabase
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.PrayerTime
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.util.Constants
import com.sajda.app.util.DateTimeUtils
import com.sajda.app.util.displayName
import com.sajda.app.util.pick
import com.sajda.app.widget.PrayerTimesWidgetUpdater
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AdzanScheduler(private val context: Context) {

    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val preferencesDataStore = PreferencesDataStore(appContext)

    suspend fun reschedule(
        prayerTimes: List<PrayerTime>,
        settings: UserSettings,
        referenceTime: LocalDateTime = LocalDateTime.now()
    ) {
        cancelUpcoming(prayerTimes)
        if (!settings.adzanEnabled) {
            preferencesDataStore.updateNextScheduledPrayer("", "")
            Log.d(TAG, "Adhan disabled, cleared upcoming alarms")
            return
        }

        val upcomingPrayers = findUpcomingScheduledPrayers(prayerTimes, settings, referenceTime)
        upcomingPrayers.forEachIndexed { index, scheduledPrayer ->
            schedulePrayer(scheduledPrayer, isPrimary = index == 0)
        }
        val nextUpcoming = upcomingPrayers.firstOrNull()

        preferencesDataStore.updateNextScheduledPrayer(
            prayerName = nextUpcoming?.prayerName?.label.orEmpty(),
            scheduledAt = nextUpcoming?.scheduledAt?.let(DateTimeUtils::dateTimeString).orEmpty()
        )
        preferencesDataStore.appendAdhanLog(
            prayerName = nextUpcoming?.prayerName?.label ?: "Adzan",
            status = settings.pick("Jadwal diperbarui", "Schedules refreshed"),
            details = buildString {
                append(settings.pick("Lokasi ", "Location "))
                append(settings.locationName)
                append(". ")
                append(settings.pick("Metode ", "Method "))
                append(settings.prayerCalculationMethod.label)
                append(". ")
                append(settings.pick("Asar ", "Asr "))
                append(settings.asrMadhhab.label)
                append(". ")
                if (nextUpcoming != null) {
                    append(settings.pick("Alarm utama ", "Primary alarm "))
                    append(nextUpcoming.prayerName.displayName(settings.appLanguage))
                    append(settings.pick(" pada ", " at "))
                    append(DateTimeUtils.dateTimeString(nextUpcoming.scheduledAt))
                    append(".")
                } else {
                    append(settings.pick("Tidak ada alarm aktif.", "No active alarms."))
                }
            }
        )
        Log.d(
            TAG,
            "Scheduled ${upcomingPrayers.size} adhan alarm(s), next=${nextUpcoming?.prayerName?.label} at ${nextUpcoming?.scheduledAt}"
        )
        PrayerTimesWidgetUpdater.enqueueUpdate(appContext)
    }

    fun cancelUpcoming(prayerTimes: List<PrayerTime>) {
        prayerTimes.forEach { prayerTime ->
            PrayerName.entries.forEach { prayerName ->
                val pendingIntent = pendingIntentFor(
                    prayerTime = prayerTime,
                    prayerName = prayerName,
                    prayerTimeValue = timeFor(prayerTime, prayerName)
                )
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    private suspend fun schedulePrayer(scheduledPrayer: ScheduledPrayer, isPrimary: Boolean) {
        val triggerAtMillis = scheduledPrayer.scheduledAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val pendingIntent = pendingIntentFor(
            prayerTime = scheduledPrayer.prayerTime,
            prayerName = scheduledPrayer.prayerName,
            prayerTimeValue = scheduledPrayer.timeValue
        )
        val openAppIntent = Intent(appContext, MainActivity::class.java).apply {
            action = Constants.ACTION_OPEN_PRAYER_TAB
            putExtra(Constants.EXTRA_OPEN_TAB, "prayer")
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            appContext,
            400,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isPrimary) {
            alarmManager.setAlarmClock(
                AlarmClockInfo(triggerAtMillis, openAppPendingIntent),
                pendingIntent
            )
            preferencesDataStore.appendAdhanLog(
                prayerName = scheduledPrayer.prayerName.label,
                status = runBlocking {
                    preferencesDataStore.settingsFlow.first().appLanguage.pick(
                        "Alarm utama dijadwalkan",
                        "Primary alarm scheduled"
                    )
                },
                details = "AlarmClock | ${scheduledPrayer.prayerTime.locationName} | ${scheduledPrayer.timeValue}"
            )
            return
        }

        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        runCatching {
            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
            preferencesDataStore.appendAdhanLog(
                prayerName = scheduledPrayer.prayerName.label,
                status = runBlocking {
                    preferencesDataStore.settingsFlow.first().appLanguage.pick(
                        if (canScheduleExact) "Alarm exact dijadwalkan" else "Alarm fallback dijadwalkan",
                        if (canScheduleExact) "Exact alarm scheduled" else "Fallback alarm scheduled"
                    )
                },
                details = "${scheduledPrayer.prayerTime.locationName} | ${scheduledPrayer.timeValue}"
            )
        }.onFailure { error ->
            Log.e(TAG, "Failed to schedule ${scheduledPrayer.prayerName.label} at ${scheduledPrayer.scheduledAt}", error)
            preferencesDataStore.appendAdhanLog(
                prayerName = scheduledPrayer.prayerName.label,
                status = runBlocking {
                    preferencesDataStore.settingsFlow.first().appLanguage.pick(
                        "Gagal menjadwalkan alarm",
                        "Failed to schedule alarm"
                    )
                },
                details = error.message.orEmpty()
            )
        }
    }

    private fun pendingIntentFor(
        prayerTime: PrayerTime,
        prayerName: PrayerName,
        prayerTimeValue: String
    ): PendingIntent {
        val intent = Intent(appContext, AdzanReceiver::class.java).apply {
            action = Constants.ACTION_TRIGGER_ADZAN
            data = Uri.parse(
                "sajda://adhan/${prayerTime.date}/${prayerName.key}?time=$prayerTimeValue&loc=${Uri.encode(prayerTime.locationName)}"
            )
            putExtra(Constants.EXTRA_PRAYER_NAME, prayerName.label)
            putExtra(Constants.EXTRA_PRAYER_TIME, prayerTimeValue)
            putExtra(Constants.EXTRA_PRAYER_DATE, prayerTime.date)
            putExtra(Constants.EXTRA_LOCATION_NAME, prayerTime.locationName)
        }
        return PendingIntent.getBroadcast(
            appContext,
            "${prayerTime.date}-${prayerName.name}-$prayerTimeValue".hashCode(),
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

    private fun findUpcomingScheduledPrayers(
        prayerTimes: List<PrayerTime>,
        settings: UserSettings,
        referenceTime: LocalDateTime
    ): List<ScheduledPrayer> {
        return prayerTimes.asSequence().flatMap { prayerTime ->
            PrayerName.entries.asSequence()
                .filter { isEnabled(settings, it) }
                .map { prayerName ->
                    ScheduledPrayer(
                        prayerTime = prayerTime,
                        prayerName = prayerName,
                        timeValue = timeFor(prayerTime, prayerName),
                        scheduledAt = LocalDateTime.of(
                            LocalDate.parse(prayerTime.date),
                            LocalTime.parse(timeFor(prayerTime, prayerName))
                        )
                    )
                }
        }
            .filter { it.scheduledAt.isAfter(referenceTime) }
            .sortedBy { it.scheduledAt }
            .take(MAX_BACKUP_ALARMS)
            .toList()
    }

    private data class ScheduledPrayer(
        val prayerTime: PrayerTime,
        val prayerName: PrayerName,
        val timeValue: String,
        val scheduledAt: LocalDateTime
    )

    companion object {
        private const val MAX_BACKUP_ALARMS = 10
        private const val TAG = "SajdaAdhan"

        suspend fun repairNextAlarm(
            context: Context,
            referenceTime: LocalDateTime = LocalDateTime.now()
        ) {
            val appContext = context.applicationContext
            val preferencesDataStore = PreferencesDataStore(appContext)
            val settings = preferencesDataStore.settingsFlow.first()
            val prayerTimeRepository = PrayerTimeRepository(SajdaDatabase.getDatabase(appContext))
            var prayerTimes = prayerTimeRepository.getNextDaysPrayerTimes(days = 30)

            if (prayerTimes.isEmpty() || prayerTimes.none { !LocalDate.parse(it.date).isBefore(referenceTime.toLocalDate()) }) {
                prayerTimes = prayerTimeRepository.refreshPrayerTimes(settings, days = 30)
            }

            AdzanScheduler(appContext).reschedule(
                prayerTimes = prayerTimes,
                settings = settings,
                referenceTime = referenceTime
            )
        }
    }
}
