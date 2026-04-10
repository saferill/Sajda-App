package com.sajda.app

import android.app.Application
import androidx.media3.common.util.UnstableApi
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.util.AppLocaleManager
import com.sajda.app.util.AppTranslations
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
@UnstableApi
class SajdaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppTranslations.init(this)
        runCatching {
            val language = runBlocking { PreferencesDataStore(this@SajdaApplication).settingsFlow.first().appLanguage }
            AppLocaleManager.apply(language)
        }
    }
}
