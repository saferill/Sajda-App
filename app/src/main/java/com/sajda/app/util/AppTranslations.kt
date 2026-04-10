package com.sajda.app.util

import android.content.Context
import android.util.Log
import com.sajda.app.domain.model.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

object AppTranslations {
    private const val PREFS_NAME = "app_translations"
    private const val TAG = "AppTranslations"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val httpClient = OkHttpClient.Builder().build()
    private val pending = ConcurrentHashMap<String, Boolean>()
    private val _updates = MutableStateFlow(0)

    val updates: StateFlow<Int> = _updates

    @Volatile
    private var appContext: Context? = null

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    fun translate(english: String, language: AppLanguage): String {
        if (english.isBlank()) return english
        if (language == AppLanguage.ENGLISH) return english
        val context = appContext ?: return english
        val normalized = english.trim()
        val cacheKey = cacheKey(language, normalized)
        val cached = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(cacheKey, null)
        if (!cached.isNullOrBlank()) return cached
        if (pending.putIfAbsent(cacheKey, true) == null) {
            scope.launch {
                translateAndStore(context, normalized, language, cacheKey)
            }
        }
        return english
    }

    private fun translateAndStore(
        context: Context,
        english: String,
        language: AppLanguage,
        cacheKey: String
    ) {
        runCatching {
            val encoded = URLEncoder.encode(english, "UTF-8")
            val url = "https://translate.googleapis.com/translate_a/single" +
                "?client=gtx&sl=en&tl=${language.languageTag}&dt=t&q=$encoded"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            val translated = httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                parseTranslatedText(body).ifBlank { english }
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(cacheKey, translated)
                .apply()
            _updates.update { it + 1 }
        }.onFailure { error ->
            Log.e(TAG, "Translate failed for ${language.name}", error)
        }.also {
            pending.remove(cacheKey)
        }
    }

    private fun parseTranslatedText(response: String): String {
        if (response.isBlank()) return ""
        return try {
            val root = JSONArray(response)
            val segments = root.getJSONArray(0)
            val builder = StringBuilder()
            for (i in 0 until segments.length()) {
                val segment = segments.getJSONArray(i)
                builder.append(segment.optString(0))
            }
            builder.toString()
        } catch (error: Exception) {
            Log.e(TAG, "Translate parse failed", error)
            ""
        }
    }

    private fun cacheKey(language: AppLanguage, english: String): String {
        val hash = sha1(english)
        return "${language.languageTag}:$hash"
    }

    private fun sha1(value: String): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(value.toByteArray())
        val builder = StringBuilder(digest.size * 2)
        digest.forEach { byte ->
            builder.append(String.format("%02x", byte))
        }
        return builder.toString()
    }
}
