package com.sajda.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sajda.app.MainActivity
import com.sajda.app.R
import com.sajda.app.domain.model.AudioPlaybackState
import com.sajda.app.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioService : Service() {

    private lateinit var player: ExoPlayer
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var progressJob: Job? = null
    private var currentTitle: String = "Murattal"
    private var currentAudioPath: String? = null
    private var currentSurahNumber: Int = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        player = ExoPlayer.Builder(this).build().apply {
            addListener(
                object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        updatePlaybackState()
                        startForeground(
                            Constants.AUDIO_NOTIFICATION_ID,
                            createNotification(
                                if (isPlaying) {
                                    "Sedang memutar $currentTitle"
                                } else {
                                    "Audio dijeda"
                                }
                            )
                        )
                        if (isPlaying) {
                            startProgressUpdates()
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        updatePlaybackState()
                        if (playbackState == Player.STATE_ENDED) {
                            stopProgressUpdates()
                        }
                    }
                }
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_PLAY_AUDIO -> {
                val filePath = intent.getStringExtra(Constants.EXTRA_AUDIO_PATH)
                currentTitle = intent.getStringExtra(Constants.EXTRA_SURAH_TITLE) ?: "Murattal"
                currentSurahNumber = intent.getIntExtra(Constants.EXTRA_SURAH_NUMBER, 0)
                if (!filePath.isNullOrBlank()) {
                    currentAudioPath = filePath
                    playAudio(filePath)
                }
            }

            Constants.ACTION_PAUSE_AUDIO -> {
                if (player.isPlaying) {
                    player.pause()
                }
            }

            Constants.ACTION_RESUME_AUDIO -> {
                if (!player.isPlaying && player.mediaItemCount > 0) {
                    player.play()
                }
            }

            Constants.ACTION_STOP_AUDIO -> stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopProgressUpdates()
        player.release()
        AudioPlaybackStore.clear()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun playAudio(filePath: String) {
        val targetFile = File(filePath)
        if (!targetFile.exists()) {
            stopSelf()
            return
        }

        startForeground(
            Constants.AUDIO_NOTIFICATION_ID,
            createNotification("Menyiapkan $currentTitle")
        )

        player.setMediaItem(MediaItem.fromUri(Uri.fromFile(targetFile)))
        player.prepare()
        player.playWhenReady = true
        updatePlaybackState()
    }

    private fun createNotification(contentText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseOrResumeAction = if (player.isPlaying) {
            NotificationCompat.Action(
                0,
                "Pause",
                servicePendingIntent(Constants.ACTION_PAUSE_AUDIO, 11)
            )
        } else {
            NotificationCompat.Action(
                0,
                "Play",
                servicePendingIntent(Constants.ACTION_RESUME_AUDIO, 12)
            )
        }

        val stopAction = NotificationCompat.Action(
            0,
            "Stop",
            servicePendingIntent(Constants.ACTION_STOP_AUDIO, 13)
        )

        return NotificationCompat.Builder(this, Constants.AUDIO_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Sajda App")
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(player.isPlaying)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(pauseOrResumeAction)
            .addAction(stopAction)
            .build()
    }

    private fun servicePendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, AudioService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.AUDIO_NOTIFICATION_CHANNEL,
                "Pemutaran Audio Sajda",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun startProgressUpdates() {
        if (progressJob?.isActive == true) return
        progressJob = serviceScope.launch {
            while (isActive) {
                updatePlaybackState()
                delay(750)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updatePlaybackState() {
        val duration = player.duration.takeIf { it > 0 } ?: 0L
        val position = player.currentPosition.coerceAtLeast(0L)
        val progress = if (duration > 0L) position.toFloat() / duration.toFloat() else 0f

        AudioPlaybackStore.update(
            AudioPlaybackState(
                title = currentTitle,
                surahNumber = currentSurahNumber,
                audioPath = currentAudioPath,
                isPlaying = player.isPlaying,
                progress = progress.coerceIn(0f, 1f),
                elapsedLabel = formatDuration(position),
                durationLabel = formatDuration(duration)
            )
        )
    }

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return "%02d:%02d".format(minutes, seconds)
    }

    companion object {
        fun play(context: Context, audioPath: String, title: String, surahNumber: Int) {
            val intent = Intent(context, AudioService::class.java).apply {
                action = Constants.ACTION_PLAY_AUDIO
                putExtra(Constants.EXTRA_AUDIO_PATH, audioPath)
                putExtra(Constants.EXTRA_SURAH_TITLE, title)
                putExtra(Constants.EXTRA_SURAH_NUMBER, surahNumber)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun pause(context: Context) {
            val intent = Intent(context, AudioService::class.java).apply {
                action = Constants.ACTION_PAUSE_AUDIO
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, AudioService::class.java).apply {
                action = Constants.ACTION_RESUME_AUDIO
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, AudioService::class.java).apply {
                action = Constants.ACTION_STOP_AUDIO
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
