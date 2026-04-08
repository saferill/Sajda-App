package com.sajda.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.sajda.app.MainActivity
import com.sajda.app.R
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.local.SajdaDatabase
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.util.Constants
import com.sajda.app.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerTimesWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        PrayerTimesWidgetUpdater.enqueueUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == Constants.ACTION_REFRESH_PRAYER_WIDGET) {
            PrayerTimesWidgetUpdater.enqueueUpdate(context)
        }
    }
}

object PrayerTimesWidgetUpdater {

    fun enqueueUpdate(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            updateNow(context.applicationContext)
        }
    }

    private suspend fun updateNow(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, PrayerTimesWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(component)
        if (widgetIds.isEmpty()) return

        val preferencesDataStore = PreferencesDataStore(context)
        val prayerTimeRepository = PrayerTimeRepository(SajdaDatabase.getDatabase(context))
        val settings = preferencesDataStore.settingsFlow.first()
        val todayPrayerTime = prayerTimeRepository.getTodayPrayerTime()
            ?: prayerTimeRepository.refreshPrayerTimes(settings).firstOrNull()

        val nextPrayer = todayPrayerTime?.let(DateTimeUtils::nextPrayer)
        val summary = todayPrayerTime?.let {
            "Subuh ${it.fajr} • Dzuhur ${it.dhuhr} • Maghrib ${it.maghrib}"
        } ?: "Jadwal sedang disiapkan"

        widgetIds.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.prayer_times_widget).apply {
                setTextViewText(R.id.widget_title, todayPrayerTime?.locationName ?: settings.locationName)
                setTextViewText(
                    R.id.widget_next_prayer,
                    nextPrayer?.let { "Berikutnya ${it.first.label}" } ?: "NurApp"
                )
                setTextViewText(R.id.widget_next_time, nextPrayer?.second ?: "--:--")
                setTextViewText(R.id.widget_summary, summary)
                setOnClickPendingIntent(R.id.widget_root, openPrayerPendingIntent(context))
            }
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    private fun openPrayerPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Constants.ACTION_OPEN_PRAYER_TAB
            putExtra(Constants.EXTRA_OPEN_TAB, "prayer")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            400,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
