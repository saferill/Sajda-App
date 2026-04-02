package com.sajda.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdzanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Constants.ACTION_TRIGGER_ADZAN) return

        val prayerName = intent.getStringExtra(Constants.EXTRA_PRAYER_NAME) ?: "Waktu sholat"
        CoroutineScope(Dispatchers.IO).launch {
            PreferencesDataStore(context).updateAdhanLastEvent(
                prayerName = prayerName,
                status = "Alarm dipicu"
            )
        }

        val serviceIntent = Intent(context, AdzanService::class.java).apply {
            action = Constants.ACTION_TRIGGER_ADZAN
            putExtra(
                Constants.EXTRA_PRAYER_NAME,
                prayerName
            )
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
