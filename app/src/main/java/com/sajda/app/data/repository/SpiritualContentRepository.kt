package com.sajda.app.data.repository

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.sajda.app.data.api.DuaApi
import com.sajda.app.data.api.HadithApi
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.DailyDua
import com.sajda.app.domain.model.HadithEntry
import com.sajda.app.util.SpiritualContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class SpiritualContentBundle(
    val duas: List<DailyDua>,
    val hadithOfDay: HadithEntry?,
    val hadithCategories: Map<String, List<HadithEntry>>,
    val isRemote: Boolean,
    val sourceLabel: String
)

@Singleton
class SpiritualContentRepository @Inject constructor(
    private val hadithApi: HadithApi,
    private val duaApi: DuaApi
) {
    private val hadithSources = linkedMapOf(
        "Niat" to ("bukhari" to "1-5"),
        "Ibadah" to ("muslim" to "1-5"),
        "Akhlak" to ("tirmidzi" to "1-5"),
        "Ilmu" to ("nasai" to "1-5")
    )

    private val equranDoaUrl = "https://equran.id/api/doa"
    private val myQuranDoaUrl = "https://api.myquran.com/v2/doa/all"

    suspend fun load(language: AppLanguage): SpiritualContentBundle = withContext(Dispatchers.IO) {
        if (language != AppLanguage.ENGLISH) {
            val remoteDuas = fetchRemoteDuas()
            val remoteHadiths = fetchRemoteHadithCategories()
            if (remoteDuas.isNotEmpty() || remoteHadiths.isNotEmpty()) {
                val flattenedHadiths = remoteHadiths.values.flatten()
                return@withContext SpiritualContentBundle(
                    duas = remoteDuas.ifEmpty { SpiritualContent.dailyDuas },
                    hadithOfDay = pickHadithOfDay(flattenedHadiths),
                    hadithCategories = remoteHadiths.ifEmpty { SpiritualContent.groupedHadiths() },
                    isRemote = true,
                    sourceLabel = "EQuran.id + Hadith Gading"
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
        return hadithSources.mapValues { (label, source) ->
            fetchHadithCategory(label, source.first, source.second)
        }.filterValues { it.isNotEmpty() }
    }

    private suspend fun fetchHadithCategory(
        label: String,
        bookId: String,
        range: String
    ): List<HadithEntry> {
        val root = try {
            hadithApi.getHadithBook(
                url = "https://api.hadith.gading.dev/books/$bookId",
                range = range
            )
        } catch (e: Exception) {
            return emptyList()
        }

        val data = root.getAsJsonObject("data")
            ?.getAsJsonArray("hadiths")
            ?: root.getAsJsonArray("hadiths")
            ?: return emptyList()
        val collection = root.getAsJsonObject("data")?.stringValue("name").orEmpty()
            .ifBlank { bookId.replace('-', ' ').replaceFirstChar(Char::uppercase) }

        return data.mapNotNull { element ->
            val item = element.asJsonObject
            val text = item.stringValue("id")
                .ifBlank { item.stringValue("translation") }
                .ifBlank { item.stringValue("text") }
            if (text.isBlank()) return@mapNotNull null

            val reference = item.stringValue("number")
            HadithEntry(
                id = "remote_${bookId}_${reference.ifBlank { item.stringValue("id") }}",
                category = label,
                title = label,
                collection = collection,
                reference = reference,
                narrator = collection,
                text = text,
                arabicText = item.stringValue("arab"),
                sourceLabel = "Hadith Gading"
            )
        }
    }

    private suspend fun fetchRemoteDuas(): List<DailyDua> {
        val primary = runCatching {
            parseDuaResponse(duaApi.getDuas(equranDoaUrl), "EQuran.id")
        }.getOrDefault(emptyList())
        if (primary.isNotEmpty()) return primary

        return runCatching {
            parseDuaResponse(duaApi.getDuas(myQuranDoaUrl), "MyQuran")
        }.getOrDefault(emptyList())
    }

    private fun parseDuaResponse(root: JsonElement, sourceLabel: String): List<DailyDua> {
        val items = root.findArray("data")
            ?: root.findArray("result")
            ?: root.asJsonArrayOrNull()
            ?: return emptyList()

        return items.mapNotNull { element ->
            val item = element.asJsonObjectOrNull() ?: return@mapNotNull null
            val title = item.firstString("judul", "title", "nama", "name")
            val rawCategory = item.firstString("grup", "group", "kategori", "category", "tag")
            val arabic = item.firstString("arab", "arabic", "arabic_text", "doa")
            val translation = item.firstString("arti", "translation", "terjemah", "indo")
            if (arabic.isBlank() || translation.isBlank()) return@mapNotNull null

            DailyDua(
                id = item.firstString("id", "nomor").ifBlank {
                    "${sourceLabel.lowercase()}_${title.ifBlank { translation.take(24) }}"
                },
                category = normalizeDuaCategory(title, rawCategory),
                title = buildDuaTitle(title, translation, rawCategory),
                arabic = arabic,
                transliteration = item.firstString("latin", "transliterasi", "transliteration"),
                translation = translation,
                sourceLabel = sourceLabel
            )
        }
    }

    private fun pickHadithOfDay(entries: List<HadithEntry>): HadithEntry? {
        if (entries.isEmpty()) return null
        val index = Math.floorMod(LocalDate.now().toEpochDay().toInt(), entries.size)
        return entries[index]
    }

    private fun normalizeDuaCategory(title: String, rawCategory: String): String {
        val normalized = "$title $rawCategory".lowercase()
        return when {
            "pagi" in normalized || "petang" in normalized || "dzikir" in normalized -> "Dzikir"
            "tidur" in normalized || "bangun" in normalized -> "Tidur"
            "azan" in normalized || "shalat" in normalized || "doa sholat" in normalized -> "Sholat"
            "rumah" in normalized || "perjalanan" in normalized || "safar" in normalized -> "Harian"
            rawCategory.isNotBlank() -> rawCategory
            else -> "Harian"
        }
    }

    private fun buildDuaTitle(title: String, translation: String, rawCategory: String): String {
        val normalizedTitle = title
            .substringBefore('.')
            .trim()
        if (normalizedTitle.isNotBlank()) {
            return normalizedTitle.take(56)
        }

        val translationTitle = translation
            .substringBefore('.')
            .trim()
        if (translationTitle.isNotBlank()) {
            return translationTitle.take(56)
        }

        return rawCategory.ifBlank { "Doa Harian" }
    }

    private fun JsonObject.stringValue(key: String): String {
        return get(key)?.takeUnless { it.isJsonNull }?.asString.orEmpty().trim()
    }

    private fun JsonObject.firstString(vararg keys: String): String {
        return keys.firstNotNullOfOrNull { key ->
            get(key)?.takeUnless { it.isJsonNull }?.asString?.trim()?.takeIf { it.isNotBlank() }
        }.orEmpty()
    }

    private fun JsonElement.findArray(key: String): JsonArray? {
        val rootObject = asJsonObjectOrNull() ?: return null
        return rootObject.getAsJsonArray(key)
    }

    private fun JsonElement.asJsonArrayOrNull(): JsonArray? {
        return if (isJsonArray) asJsonArray else null
    }

    private fun JsonElement.asJsonObjectOrNull(): JsonObject? {
        return if (isJsonObject) asJsonObject else null
    }
}
