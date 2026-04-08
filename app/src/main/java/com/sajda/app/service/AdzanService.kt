package com.sajda.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.media.AudioFocusRequest
import android.graphics.BitmapFactory
import android.graphics.Color
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sajda.app.MainActivity
import com.sajda.app.R
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.util.Constants
import com.sajda.app.util.localizedPrayerName
import com.sajda.app.util.pick
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AdzanService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var fallbackRingtone: android.media.Ringtone? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var currentPrayerName: String = "Subuh"
    private var currentPrayerKey: String = "fajr"
    private var currentPrayerTime: String = ""
    private var currentPrayerDate: String = ""
    private var currentLocationName: String = ""

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_STOP_ADZAN -> {
                stopAdzan()
                return START_NOT_STICKY
            }
            Constants.ACTION_SNOOZE_ADZAN -> {
                currentPrayerName = intent.getStringExtra(Constants.EXTRA_PRAYER_NAME) ?: currentPrayerName
                currentPrayerKey = intent.getStringExtra(Constants.EXTRA_PRAYER_KEY) ?: currentPrayerKey
                snoozeAdzan(currentPrayerName, currentPrayerKey, intent.getIntExtra(Constants.EXTRA_SNOOZE_MINUTES, 10))
                return START_NOT_STICKY
            }
            else -> {
                currentPrayerName = intent?.getStringExtra(Constants.EXTRA_PRAYER_NAME) ?: PrayerName.FAJR.label
                currentPrayerKey = intent?.getStringExtra(Constants.EXTRA_PRAYER_KEY) ?: PrayerName.FAJR.key
                currentPrayerTime = intent?.getStringExtra(Constants.EXTRA_PRAYER_TIME).orEmpty()
                currentPrayerDate = intent?.getStringExtra(Constants.EXTRA_PRAYER_DATE).orEmpty()
                currentLocationName = intent?.getStringExtra(Constants.EXTRA_LOCATION_NAME).orEmpty()
                if (isServiceRunning) {
                    Log.d(TAG, "Ignoring duplicate adhan start for $currentPrayerName at $currentPrayerTime")
                    return START_NOT_STICKY
                }
                isServiceRunning = true
                if (currentLocationName.isBlank()) {
                    currentLocationName = resolveLocationLabel()
                }
                startForeground(Constants.ADZAN_NOTIFICATION_ID, createNotification(currentPrayerName))
                triggerAdzan(currentPrayerName, currentPrayerKey)
                return START_REDELIVER_INTENT
            }
        }
    }

    override fun onDestroy() {
        isServiceRunning = false
        releasePlayer()
        releaseFallbackRingtone()
        abandonAudioFocus()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun triggerAdzan(prayerName: String, prayerKey: String) {
        val preferencesDataStore = PreferencesDataStore(this@AdzanService)
        val settings = runBlocking { preferencesDataStore.settingsFlow.first() }
        currentLocationName = currentLocationName.ifBlank { resolveLocationLabel(settings) }
        val audioManager = getSystemService(AudioManager::class.java)
        val alarmVolumeAudible = audioManager.getStreamVolume(AudioManager.STREAM_ALARM) > 0
        val canPlaySound = (
            settings.overrideSilentMode || audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL
            ) && alarmVolumeAudible

        if (settings.vibrationEnabled) {
            vibrate()
        }

        if (!canPlaySound) {
            runBlocking {
                preferencesDataStore.updateAdhanLastEvent(
                    prayerName = prayerName,
                    status = settings.pick(
                        "Notif tampil, suara mengikuti mode perangkat",
                        "Notification shown, sound follows the device mode"
                    ),
                    details = "Silent override=${settings.overrideSilentMode}, ringer=${audioManager.ringerMode}, volumeAlarm=${audioManager.getStreamVolume(AudioManager.STREAM_ALARM)}"
                )
            }
            keepNotificationVisibleAndStop(
                createNotification(
                    prayerName = prayerName,
                    soundDisabled = true
                )
            )
            return
        }

        releasePlayer()
        val audioSource = resolveAdzanAudio(prayerKey, settings)
        val adzanUri = audioSource.uri ?: run {
            playShortFallbackAlert(prayerName, "Sumber audio tidak ditemukan")
            return
        }

        runCatching {
            requestAudioFocus()
            mediaPlayer = MediaPlayer().apply {
                setWakeMode(this@AdzanService, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    @Suppress("DEPRECATION")
                    setAudioStreamType(AudioManager.STREAM_ALARM)
                }
                setDataSource(this@AdzanService, adzanUri)
                isLooping = audioSource.shouldLoop
                setOnCompletionListener { finishAdzanPlayback() }
                setOnErrorListener { _, _, _ ->
                    playShortFallbackAlert(prayerName, "MediaPlayer error")
                    true
                }
                prepare()
                start()
            }
            runBlocking {
                preferencesDataStore.updateAdhanLastEvent(
                    prayerName = prayerName,
                    status = if (audioSource.isFallbackShort) {
                        settings.pick("Alarm pengganti diputar", "Fallback alert played")
                    } else {
                        settings.pick("Adzan diputar", "Adhan played")
                    },
                    details = adzanUri.toString()
                )
            }
            Log.d(TAG, "Playing adhan for $prayerName from $adzanUri")
            NotificationManagerCompat.from(this).notify(
                Constants.ADZAN_NOTIFICATION_ID,
                createNotification(prayerName, isPlaying = true)
            )
        }.onFailure {
            Log.e(TAG, "Failed to play adhan for $prayerName", it)
            playShortFallbackAlert(prayerName, it.message.orEmpty())
            runBlocking {
                preferencesDataStore.updateAdhanLastEvent(
                    prayerName = prayerName,
                    status = settings.pick(
                        "Gagal memutar audio, alarm pengganti dipakai",
                        "Audio playback failed, fallback alert was used"
                    ),
                    details = it.message.orEmpty()
                )
            }
        }
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(Vibrator::class.java)
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(2000)
        }
    }

    private fun createNotification(
        prayerName: String,
        isPlaying: Boolean = false,
        soundDisabled: Boolean = false
    ): Notification {
        val language = currentLanguage()
        val displayName = displayPrayerName(prayerName, language)
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            action = Constants.ACTION_OPEN_PRAYER_TAB
            putExtra(Constants.EXTRA_OPEN_TAB, "prayer")
            putExtra("is_adhan", true)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prayerPendingIntent = PendingIntent.getActivity(
            this,
            204,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AdzanService::class.java).apply {
            action = Constants.ACTION_STOP_ADZAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            202,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeMinutes = runBlocking { PreferencesDataStore(this@AdzanService).settingsFlow.first().adhanSnoozeMinutes }
        val snoozeIntent = Intent(this, AdzanService::class.java).apply {
            action = Constants.ACTION_SNOOZE_ADZAN
            putExtra(Constants.EXTRA_PRAYER_NAME, prayerName)
            putExtra(Constants.EXTRA_PRAYER_KEY, currentPrayerKey)
            putExtra(Constants.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            203,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val summaryText = buildSummaryText(prayerName)
        val expandedText = when {
            soundDisabled -> {
                language.pick(
                    "Notifikasi tetap tampil. Suara mengikuti mode perangkat atau izin sistem belum lengkap.",
                    "The notification is still shown. Sound follows the device mode or system access is incomplete."
                )
            }
            isPlaying -> {
                language.pick(
                    "Adzan sedang diputar. Gunakan tombol volume HP untuk mengatur besar suara.",
                    "The adhan is playing. Use your phone volume buttons to adjust the sound."
                )
            }
            else -> {
                language.pick(
                    "Buka halaman Sholat untuk melihat jadwal lengkap berikutnya.",
                    "Open the Prayer page to view the next full schedule."
                )
            }
        }

        val compactView = buildCompactNotificationView(
            prayerName = prayerName,
            statusLine = language.pick("Saatnya menunaikan sholat", "It is time to pray"),
            accentLine = buildMetaLine(language)
        )
        val expandedView = buildExpandedNotificationView(
            prayerName = prayerName,
            summaryText = summaryText,
            expandedText = expandedText,
            isPlaying = isPlaying,
            soundDisabled = soundDisabled
        )

        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            action = Constants.ACTION_OPEN_PRAYER_TAB
            putExtra(Constants.EXTRA_OPEN_TAB, "prayer")
            putExtra("is_adhan", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            205,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.ADZAN_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(language.pick("Waktu $displayName telah tiba", "$displayName time has arrived"))
            .setContentText(summaryText)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setAutoCancel(!isPlaying)
            .setColor(Color.parseColor("#0F5238"))
            .setColorized(true)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.sajda_logo_mark))
            .setCustomContentView(compactView)
            .setCustomBigContentView(expandedView)
            .setCustomHeadsUpContentView(compactView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_menu_directions,
                    language.pick("Sholat", "Prayer"),
                    prayerPendingIntent
                )
            )
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_lock_idle_alarm,
                    if (language == AppLanguage.ENGLISH) "Snooze ${snoozeMinutes}m" else "Tunda ${snoozeMinutes}m",
                    snoozePendingIntent
                )
            )
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_media_pause,
                    language.pick("Stop", "Stop"),
                    stopPendingIntent
                )
            )
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                Constants.ADZAN_NOTIFICATION_CHANNEL,
                "NurApp Adhan",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "NurApp adhan alerts and controls"
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 250, 180, 250, 180, 300)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.setShowBadge(false)
            channel.setSound(null, null)
            manager.createNotificationChannel(channel)
        }
    }

    private fun keepNotificationVisibleAndStop(notification: Notification) {
        NotificationManagerCompat.from(this).notify(Constants.ADZAN_NOTIFICATION_ID, notification)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(false)
        }
        stopSelf()
    }

    private fun stopAdzan() {
        runBlocking {
            PreferencesDataStore(this@AdzanService).updateAdhanLastEvent(
                prayerName = currentPrayerName,
                status = currentLanguage().pick("Dihentikan manual", "Stopped manually")
            )
        }
        releaseFallbackRingtone()
        releasePlayer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun finishAdzanPlayback() {
        runBlocking {
            PreferencesDataStore(this@AdzanService).updateAdhanLastEvent(
                prayerName = currentPrayerName,
                status = currentLanguage().pick("Selesai diputar", "Playback finished")
            )
        }
        releaseFallbackRingtone()
        releasePlayer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun snoozeAdzan(prayerName: String, prayerKey: String, minutes: Int) {
        val safeMinutes = minutes.coerceIn(5, 30)
        runBlocking {
            PreferencesDataStore(this@AdzanService).updateAdhanLastEvent(
                prayerName = prayerName,
                status = if (currentLanguage() == AppLanguage.ENGLISH) {
                    "Snoozed ${safeMinutes} minutes"
                } else {
                    "Ditunda $safeMinutes menit"
                }
            )
        }
        releaseFallbackRingtone()
        scheduleSnoozeAlarm(this, prayerName, prayerKey, safeMinutes)
        releasePlayer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun releasePlayer() {
        mediaPlayer?.runCatching {
            if (isPlaying) {
                stop()
            }
            reset()
            release()
        }
        mediaPlayer = null
        abandonAudioFocus()
    }

    private fun releaseFallbackRingtone() {
        fallbackRingtone?.stop()
        fallbackRingtone = null
    }

    private fun playShortFallbackAlert(prayerName: String, details: String) {
        releasePlayer()
        releaseFallbackRingtone()
        val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        fallbackRingtone = fallbackUri?.let { uri ->
            RingtoneManager.getRingtone(this, uri)?.apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                play()
            }
        }

        NotificationManagerCompat.from(this).notify(
            Constants.ADZAN_NOTIFICATION_ID,
            createNotification(
                prayerName = prayerName,
                soundDisabled = true
            )
        )

        runBlocking {
            PreferencesDataStore(this@AdzanService).appendAdhanLog(
                prayerName = prayerName,
                status = currentLanguage().pick("Alarm pengganti diputar", "Fallback alert played"),
                details = details
            )
        }

        android.os.Handler(mainLooper).postDelayed({
            releaseFallbackRingtone()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }, 3_500L)
    }

    private fun resolveAdzanAudio(prayerKey: String, settings: com.sajda.app.domain.model.UserSettings): AdzanAudioSource {
        val isFajr = prayerKey.equals(PrayerName.FAJR.key, ignoreCase = true)
        val preferredName = if (isFajr) {
            settings.adzanSound.subuhResName
        } else {
            settings.adzanSound.regularResName
        }

        val bundledResId = resources.getIdentifier(preferredName, "raw", packageName)
        if (bundledResId != 0) {
            val uri = Uri.parse("android.resource://$packageName/$bundledResId")
            return AdzanAudioSource(uri = uri, shouldLoop = false)
        }

        val genericResId = resources.getIdentifier("adzan", "raw", packageName)
        if (genericResId != 0) {
            val uri = Uri.parse("android.resource://$packageName/$genericResId")
            return AdzanAudioSource(uri = uri, shouldLoop = false)
        }

        val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        return AdzanAudioSource(uri = fallbackUri, shouldLoop = false, isFallbackShort = true)
    }

    private data class AdzanAudioSource(
        val uri: Uri?,
        val shouldLoop: Boolean,
        val isFallbackShort: Boolean = false
    )

    companion object {
        private const val TAG = "SajdaAdhan"
        @Volatile
        private var isServiceRunning = false

        fun isRunning(): Boolean = isServiceRunning

        private fun scheduleSnoozeAlarm(context: Context, prayerName: String, prayerKey: String, minutes: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val triggerAtMillis = System.currentTimeMillis() + minutes * 60_000L
            val intent = Intent(context, AdzanReceiver::class.java).apply {
                action = Constants.ACTION_TRIGGER_ADZAN
                putExtra(Constants.EXTRA_PRAYER_NAME, prayerName)
                putExtra(Constants.EXTRA_PRAYER_KEY, prayerKey)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                "snooze-$prayerName-$triggerAtMillis".hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        }

        fun play(context: android.content.Context, prayerName: String, prayerKey: String = "fajr") {
            val intent = Intent(context, AdzanService::class.java).apply {
                action = Constants.ACTION_TRIGGER_ADZAN
                putExtra(Constants.EXTRA_PRAYER_NAME, prayerName)
                putExtra(Constants.EXTRA_PRAYER_KEY, prayerKey)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: android.content.Context) {
            val intent = Intent(context, AdzanService::class.java).apply {
                action = Constants.ACTION_STOP_ADZAN
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }

    private fun currentLanguage(): AppLanguage {
        return runBlocking { PreferencesDataStore(this@AdzanService).settingsFlow.first().appLanguage }
    }

    private fun displayPrayerName(prayerName: String, language: AppLanguage = currentLanguage()): String {
        return localizedPrayerName(prayerName, language)
    }

    private fun buildSummaryText(prayerName: String): String {
        val language = currentLanguage()
        val displayName = displayPrayerName(prayerName, language)
        return buildString {
            append(language.pick("Saatnya sholat ", "Time for "))
            append(displayName)
            append('.')
        }
    }

    private fun buildCompactNotificationView(
        prayerName: String,
        statusLine: String,
        accentLine: String
    ): RemoteViews {
        val language = currentLanguage()
        val displayName = displayPrayerName(prayerName, language)
        return RemoteViews(packageName, R.layout.notification_adhan_compact).apply {
            setImageViewResource(R.id.logoView, R.drawable.sajda_logo_mark)
            setTextViewText(R.id.badgeView, "ADHAN")
            setTextViewText(R.id.titleView, language.pick("Waktu $displayName", "$displayName time"))
            setTextViewText(R.id.statusView, statusLine)
            setTextViewText(R.id.metaView, accentLine)
        }
    }

    private fun buildExpandedNotificationView(
        prayerName: String,
        summaryText: String,
        expandedText: String,
        isPlaying: Boolean,
        soundDisabled: Boolean
    ): RemoteViews {
        val language = currentLanguage()
        val displayName = displayPrayerName(prayerName, language)
        val locationLabel = resolveLocationLabel()
        return RemoteViews(packageName, R.layout.notification_adhan_expanded).apply {
            setImageViewResource(R.id.logoView, R.drawable.sajda_logo_mark)
            setTextViewText(R.id.badgeView, "NURAPP")
            setTextViewText(R.id.titleView, language.pick("Waktu $displayName telah tiba", "$displayName time has arrived"))
            setTextViewText(R.id.metaView, currentPrayerTime.ifBlank { language.pick("Sekarang", "Now") })
            if (locationLabel.isBlank()) {
                setViewVisibility(R.id.locationView, android.view.View.GONE)
            } else {
                setViewVisibility(R.id.locationView, android.view.View.VISIBLE)
                setTextViewText(R.id.locationView, locationLabel)
            }
            setTextViewText(R.id.summaryView, summaryText)
            setTextViewText(R.id.statusView, expandedText)
            setTextViewText(
                R.id.stateChipView,
                when {
                    soundDisabled -> language.pick("Suara mengikuti perangkat", "Sound follows the device")
                    isPlaying -> language.pick("Adzan sedang diputar", "Adhan is playing")
                    else -> language.pick("Notifikasi aktif", "Notification active")
                }
            )
        }
    }

    private fun requestAudioFocus() {
        val audioManager = getSystemService(AudioManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                .setWillPauseWhenDucked(false)
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
    }

    private fun abandonAudioFocus() {
        val audioManager = getSystemService(AudioManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let(audioManager::abandonAudioFocusRequest)
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun buildMetaLine(language: AppLanguage): String {
        return listOf(
            currentPrayerTime.ifBlank { language.pick("Sekarang", "Now") },
            resolveLocationLabel()
        ).filter { it.isNotBlank() }.joinToString(" - ")
    }

    private fun resolveLocationLabel(
        settings: com.sajda.app.domain.model.UserSettings? = null
    ): String {
        val savedSettings = settings ?: runBlocking { PreferencesDataStore(this@AdzanService).settingsFlow.first() }
        return listOf(currentLocationName, savedSettings.locationName)
            .map { it.trim() }
            .firstOrNull {
                it.isNotBlank() &&
                    !it.equals("Lokasi belum dipilih", ignoreCase = true) &&
                    !it.equals("Location not selected", ignoreCase = true)
            }
            .orEmpty()
    }
}
