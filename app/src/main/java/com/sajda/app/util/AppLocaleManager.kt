package com.sajda.app.util

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.sajda.app.domain.model.AppLanguage

object AppLocaleManager {
    fun apply(language: AppLanguage) {
        runCatching {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(language.languageTag)
            )
        }.onFailure { error ->
            Log.e("AppLocaleManager", "Failed to apply locale ${language.languageTag}", error)
        }
    }
}
