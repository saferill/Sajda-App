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
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.util.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AdzanService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var currentPrayerName: String = "Waktu sholat"
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
                snoozeAdzan(currentPrayerName, intent.getIntExtra(Constants.EXTRA_SNOOZE_MINUTES, 10))
                return START_NOT_STICKY
            }
            else -> {
                currentPrayerName = intent?.getStringExtra(Constants.EXTRA_PRAYER_NAME) ?: "Waktu sholat"
                currentPrayerTime = intent?.getStringExtra(Constants.EXTRA_PRAYER_TIME).orEmpty()
                currentPrayerDate = intent?.getStringExtra(Constants.EXTRA_PRAYER_DATE).orEmpty()
                currentLocationName = intent?.getStringExtra(Constants.EXTRA_LOCATION_NAME).orEmpty()
                startForeground(Constants.ADZAN_NOTIFICATION_ID, createNotification(currentPrayerName))
                triggerAdzan(currentPrayerName)
                return START_REDELIVER_INTENT
            }
        }
    }

    override fun onDestroy() {
        releasePlayer()
        abandonAudioFocus()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun triggerAdzan(prayerName: String) {
        val preferencesDataStore = PreferencesDataStore(this@AdzanService)
        val settings = runBlocking { preferencesDataStore.settingsFlow.first() }
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
                    status = "Notif tampil, suara mengikuti mode perangkat"
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
        val audioSource = resolveAdzanAudio(prayerName)
        val adzanUri = audioSource.uri ?: run {
            keepNotificationVisibleAndStop(
                createNotification(
                    prayerName = prayerName,
                    soundDisabled = true
                )
            )
            return
        }

        runCatching {
            requestAudioFocus()
            mediaPlayer = MediaPlayer().apply {
                setWakeMode(this@AdzanService, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(this@AdzanService, adzanUri)
                isLooping = audioSource.shouldLoop
                setOnCompletionListener { finishAdzanPlayback() }
                setOnErrorListener { _, _, _ ->
                    stopAdzan()
                    true
                }
                prepare()
                start()
            }
            runBlocking {
                preferencesDataStore.updateAdhanLastEvent(
                    prayerName = prayerName,
                    status = "Adzan diputar"
                )
            }
            Log.d(TAG, "Playing adhan for $prayerName from $adzanUri")
            NotificationManagerCompat.from(this).notify(
                Constants.ADZAN_NOTIFICATION_ID,
                createNotification(prayerName, isPlaying = true)
            )
        }.onFailure {
            Log.e(TAG, "Failed to play adhan for $prayerName", it)
            runBlocking {
                preferencesDataStore.updateAdhanLastEvent(
                    prayerName = prayerName,
                    status = "Gagal memutar audio, notif tetap tampil"
                )
            }
            keepNotificationVisibleAndStop(
                createNotification(
                    prayerName = prayerName,
                    soundDisabled = true
                )
            )
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
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            action = Constants.ACTION_OPEN_PRAYER_TAB
            putExtra(Constants.EXTRA_OPEN_TAB, "prayer")
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
                "$summaryText Notifikasi tetap muncul, tetapi suara mengikuti mode perangkat atau akses sistem belum lengkap."
            }
            isPlaying -> {
                "$summaryText Adzan sedang dikumandangkan. Ketuk Stop untuk menghentikan atau Snooze untuk menunda."
            }
            else -> {
                "$summaryText Buka halaman Prayer untuk melihat jadwal lengkap dan countdown berikutnya."
            }
        }

        val compactView = buildCompactNotificationView(
            prayerName = prayerName,
            statusLine = if (soundDisabled) "Notifikasi aktif, suara mengikuti perangkat" else "Saatnya bersiap menunaikan sholat",
            accentLine = currentPrayerTime.ifBlank { "Sajda App" }
        )
        val expandedView = buildExpandedNotificationView(
            prayerName = prayerName,
            summaryText = summaryText,
            expandedText = expandedText,
            isPlaying = isPlaying,
            soundDisabled = soundDisabled
        )

        return NotificationCompat.Builder(this, Constants.ADZAN_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Waktu $prayerName telah tiba")
            .setContentText(summaryText)
            .setSubText(currentLocationName.ifBlank { "Sajda App" })
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
            .setCustomHeadsUpContentView(expandedView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_menu_directions,
                    "Prayer",
                    prayerPendingIntent
                )
            )
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_lock_idle_alarm,
                    "Snooze ${snoozeMinutes}m",
                    snoozePendingIntent
                )
            )
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_media_pause,
                    "Stop",
                    stopPendingIntent
                )
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                Constants.ADZAN_NOTIFICATION_CHANNEL,
                "Notifikasi Adzan Sajda",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifikasi dan kontrol suara adzan"
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
                status = "Dihentikan manual"
            )
        }
        releasePlayer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun finishAdzanPlayback() {
        runBlocking {
            PreferencesDataStore(this@AdzanService).updateAdhanLastEvent(
                prayerName = currentPrayerName,
                status = "Selesai diputar"
            )
        }
        releasePlayer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun snoozeAdzan(prayerName: String, minutes: Int) {
        val safeMinutes = minutes.coerceIn(5, 30)
        runBlocking {
            PreferencesDataStore(this@AdzanService).updateAdhanLastEvent(
                prayerName = prayerName,
                status = "Ditunda $safeMinutes menit"
            )
        }
        scheduleSnoozeAlarm(this, prayerName, safeMinutes)
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

    private fun resolveAdzanAudio(prayerName: String): AdzanAudioSource {
        val preferredName = if (prayerName.equals(PrayerName.FAJR.label, ignoreCase = true)) {
            "adzan_subuh"
        } else {
            "adzan_regular"
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

        return AdzanAudioSource(uri = fallbackUri, shouldLoop = true)
    }

    private data class AdzanAudioSource(
        val uri: Uri?,
        val shouldLoop: Boolean
    )

    companion object {
        private const val TAG = "SajdaAdhan"

        private fun scheduleSnoozeAlarm(context: Context, prayerName: String, minutes: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val triggerAtMillis = System.currentTimeMillis() + minutes * 60_000L
            val intent = Intent(context, AdzanReceiver::class.java).apply {
                action = Constants.ACTION_TRIGGER_ADZAN
                putExtra(Constants.EXTRA_PRAYER_NAME, prayerName)
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

        fun play(context: android.content.Context, prayerName: String) {
            val intent = Intent(context, AdzanService::class.java).apply {
                action = Constants.ACTION_TRIGGER_ADZAN
                putExtra(Constants.EXTRA_PRAYER_NAME, prayerName)
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

    private fun buildSummaryText(prayerName: String): String {
        return buildString {
            append("Saatnya menunaikan sholat ")
            append(prayerName)
            if (currentPrayerTime.isNotBlank()) {
                append(" pada ")
                append(currentPrayerTime)
            }
            if (currentLocationName.isNotBlank()) {
                append(" di ")
                append(currentLocationName)
            }
            append('.')
        }
    }

    private fun buildCompactNotificationView(
        prayerName: String,
        statusLine: String,
        accentLine: String
    ): RemoteViews {
        return RemoteViews(packageName, R.layout.notification_adhan_compact).apply {
            setImageViewResource(R.id.logoView, R.drawable.sajda_logo_mark)
            setTextViewText(R.id.badgeView, "ADHAN")
            setTextViewText(R.id.titleView, "Waktu $prayerName telah tiba")
            setTextViewText(R.id.statusView, statusLine)
            setTextViewText(
                R.id.metaView,
                listOf(currentLocationName, accentLine, currentPrayerDate)
                    .filter { it.isNotBlank() }
                    .joinToString("  |  ")
            )
        }
    }

    private fun buildExpandedNotificationView(
        prayerName: String,
        summaryText: String,
        expandedText: String,
        isPlaying: Boolean,
        soundDisabled: Boolean
    ): RemoteViews {
        return RemoteViews(packageName, R.layout.notification_adhan_expanded).apply {
            setImageViewResource(R.id.logoView, R.drawable.sajda_logo_mark)
            setTextViewText(R.id.badgeView, "SAJDA APP")
            setTextViewText(R.id.titleView, "Waktu $prayerName telah tiba")
            setTextViewText(R.id.metaView, currentPrayerTime.ifBlank { "Sekarang" })
            setTextViewText(R.id.locationView, currentLocationName.ifBlank { "Lokasi belum dipilih" })
            setTextViewText(R.id.summaryView, summaryText)
            setTextViewText(R.id.statusView, expandedText)
            setTextViewText(
                R.id.stateChipView,
                when {
                    soundDisabled -> "Suara mengikuti perangkat"
                    isPlaying -> "Adzan sedang diputar"
                    else -> "Notifikasi aktif"
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
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
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
}
