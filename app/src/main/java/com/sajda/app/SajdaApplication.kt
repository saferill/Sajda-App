package com.sajda.app

import android.app.Application
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
@UnstableApi
class SajdaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
