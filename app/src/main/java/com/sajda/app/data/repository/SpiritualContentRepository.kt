package com.sajda.app.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sajda.app.BuildConfig
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.DailyDua
import com.sajda.app.domain.model.HadithEntry
import com.sajda.app.util.SpiritualContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

data class SpiritualContentBundle(
    val duas: List<DailyDua>,
    val hadithOfDay: HadithEntry?,
    val hadithCategories: Map<String, List<HadithEntry>>,
    val isRemote: Boolean,
    val sourceLabel: String
)

class SpiritualContentRepository(private val appContext: Context) {

    suspend fun load(language: AppLanguage): SpiritualContentBundle = withContext(Dispatchers.IO) {
        if (language == AppLanguage.ENGLISH) {
            val remoteDuas = fetchRemoteDuas()
            val remoteHadiths = fetchRemoteHadithCategories()
            if (remoteDuas.isNotEmpty() || remoteHadiths.isNotEmpty()) {
                val flattenedHadiths = remoteHadiths.values.flatten()
                return@withContext SpiritualContentBundle(
                    duas = remoteDuas.ifEmpty { SpiritualContent.dailyDuas },
                    hadithOfDay = pickHadithOfDay(flattenedHadiths),
                    hadithCategories = remoteHadiths.ifEmpty { SpiritualContent.groupedHadiths() },
                    isRemote = true,
                    sourceLabel = "Hadith API + Hisn Muslim"
                )
            }
        }

        val groupedHadiths = SpiritualContent.groupedHadiths()
        return@withContext SpiritualContentBundle(
            duas = SpiritualContent.dailyDuas,
            hadithOfDay = SpiritualContent.hadithOfDay(),
            hadithCategories = groupedHadiths,
            isRemote = false,
            sourceLabel = "Bundled offline content"
        )
    }

    private suspend fun fetchRemoteHadithCategories(): Map<String, List<HadithEntry>> {
        if (BuildConfig.HADITH_API_KEY.isBlank()) return emptyMap()

        val categories = linkedMapOf(
            "Intention" to "intention",
            "Prayer" to "prayer",
            "Character" to "kindness",
            "Patience" to "patience",
            "Gratitude" to "gratitude",
            "Knowledge" to "knowledge"
        )

        return categories.mapValues { (label, query) ->
            fetchHadithCategory(label, query)
        }.filterValues { it.isNotEmpty() }
    }

    private fun fetchHadithCategory(
        label: String,
        query: String
    ): List<HadithEntry> {
        val uri = Uri.parse(BuildConfig.HADITH_API_BASE_URL.trimEnd('/') + "/hadiths").buildUpon()
            .appendQueryParameter("apiKey", BuildConfig.HADITH_API_KEY)
            .appendQueryParameter("hadithEnglish", query)
            .appendQueryParameter("paginate", "5")
            .build()

        val root = getJson(uri.toString()) ?: return emptyList()
        val data = root.getAsJsonObject("hadiths")
            ?.getAsJsonArray("data")
            ?: return emptyList()

        return data.mapNotNull { element ->
            val item = element.asJsonObject
            val englishText = item.stringValue("hadithEnglish")
            if (englishText.isBlank()) return@mapNotNull null

            val book = item.getAsJsonObject("book")
            val chapter = item.getAsJsonObject("chapter")
            HadithEntry(
                id = "remote_${label.lowercase()}_${item.stringValue("id")}",
                category = label,
                title = chapter?.stringValue("chapterEnglish")
                    ?.takeIf { it.isNotBlank() }
                    ?: label,
                collection = book?.stringValue("bookName").orEmpty().ifBlank { "Hadith API" },
                reference = item.stringValue("hadithNumber"),
                narrator = item.stringValue("englishNarrator").ifBlank { "Hadith API" },
                text = englishText,
                arabicText = item.stringValue("hadithArabic"),
                sourceLabel = "Hadith API"
            )
        }
    }

    private fun fetchRemoteDuas(): List<DailyDua> {
        val root = getJson(BuildConfig.DUA_CONTENT_URL) ?: return emptyList()
        val englishCollections = root.getAsJsonArray("English") ?: return emptyList()
        val selectedTitles = setOf(
            "Words of remembrance for morning and evening",
            "The supplication before sleeping",
            "The supplication when waking up",
            "Words of remembrance after the athan",
            "The excellence of remembering Allah",
            "When leaving the home",
            "When entering the home",
            "The supplication of the traveler",
            "Remembrance after the salam of the prayer was said"
        )

        return englishCollections.mapNotNull { collectionElement ->
            val collection = collectionElement.asJsonObject
            val title = collection.stringValue("TITLE")
            if (title !in selectedTitles) return@mapNotNull null
            title to collection.getAsJsonArray("TEXT")
        }.flatMap { (title, texts) ->
            texts.mapNotNull { duaElement ->
                val item = duaElement.asJsonObject
                val arabicText = item.stringValue("ARABIC_TEXT").ifBlank { item.stringValue("Text") }
                val translation = item.stringValue("TRANSLATED_TEXT")
                if (arabicText.isBlank() || translation.isBlank()) return@mapNotNull null

                DailyDua(
                    id = "remote_dua_${item.stringValue("ID")}",
                    category = normalizeDuaCategory(title),
                    title = buildDuaTitle(title, item),
                    arabic = arabicText,
                    transliteration = item.stringValue("LANGUAGE_ARABIC_TRANSLATED_TEXT"),
                    translation = translation,
                    sourceLabel = "Hisn Muslim"
                )
            }
        }
    }

    private fun getJson(url: String): JsonObject? {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Sajda-App/${BuildConfig.VERSION_NAME}")
        }

        return runCatching {
            if (connection.responseCode !in 200..299) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
                .removePrefix("\uFEFF")
            JsonParser.parseString(body).asJsonObject
        }.getOrNull().also {
            connection.disconnect()
        }
    }

    private fun pickHadithOfDay(entries: List<HadithEntry>): HadithEntry? {
        if (entries.isEmpty()) return null
        val index = Math.floorMod(LocalDate.now().toEpochDay().toInt(), entries.size)
        return entries[index]
    }

    private fun normalizeDuaCategory(title: String): String {
        val normalized = title.lowercase()
        return when {
            "morning" in normalized || "evening" in normalized -> "Morning & Evening"
            "sleep" in normalized || "waking" in normalized -> "Sleep"
            "athan" in normalized || "prayer" in normalized -> "Prayer"
            "home" in normalized || "traveler" in normalized -> "Daily Life"
            "remembering allah" in normalized -> "Remembrance"
            else -> "Daily Life"
        }
    }

    private fun buildDuaTitle(categoryTitle: String, item: JsonObject): String {
        val transliterated = item.stringValue("LANGUAGE_ARABIC_TRANSLATED_TEXT")
            .substringBefore('(')
            .substringBefore('.')
            .trim()
        if (transliterated.isNotBlank()) {
            return transliterated.take(56)
        }

        val translation = item.stringValue("TRANSLATED_TEXT")
            .substringBefore('.')
            .trim()
        if (translation.isNotBlank()) {
            return translation.take(56)
        }

        return categoryTitle
    }

    private fun JsonObject.stringValue(key: String): String {
        return get(key)?.takeUnless { it.isJsonNull }?.asString.orEmpty().trim()
    }
}
