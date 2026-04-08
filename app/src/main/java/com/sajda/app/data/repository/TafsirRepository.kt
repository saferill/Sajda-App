package com.sajda.app.data.repository

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.sajda.app.data.api.EquranApi
import com.sajda.app.domain.model.Ayat
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.TafsirEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TafsirRepository @Inject constructor(
    private val equranApi: EquranApi
) {
    private val cache = mutableMapOf<Int, List<TafsirEntry>>()

    suspend fun getTafsirForAyat(
        surahNumber: Int,
        ayat: Ayat,
        appLanguage: AppLanguage
    ): TafsirEntry? = withContext(Dispatchers.IO) {
        val entries = cache[surahNumber] ?: fetchSurahTafsir(surahNumber).also {
            cache[surahNumber] = it
        }
        val directMatch = entries.firstOrNull { it.ayatNumber == ayat.ayatNumber }
        if (directMatch != null) return@withContext directMatch

        // Fallback when remote tafsir is unavailable for the selected verse.
        return@withContext TafsirEntry(
            surahNumber = surahNumber,
            ayatNumber = ayat.ayatNumber,
            sourceName = "EQuran.id",
            sourceDescription = if (appLanguage == AppLanguage.INDONESIAN) {
                "Tafsir penuh belum tersedia untuk ayat ini."
            } else {
                "Full tafsir is not available yet for this verse."
            },
            text = if (appLanguage == AppLanguage.INDONESIAN) ayat.translation else ayat.englishTranslation.ifBlank { ayat.translation }
        )
    }

    private suspend fun fetchSurahTafsir(surahNumber: Int): List<TafsirEntry> {
        val root = equranApi.getTafsir("https://equran.id/api/v2/tafsir/$surahNumber")
        val data = root.getAsJsonObject("data") ?: return emptyList()
        val tafsirArray = data.findArray("tafsir") ?: return emptyList()
        val sourceName = "EQuran.id"
        val sourceDescription = data.stringValue("deskripsi")
        return tafsirArray.mapNotNull { element ->
            val item = element.asJsonObjectOrNull() ?: return@mapNotNull null
            val ayatNumber = item.get("ayat")?.asInt ?: return@mapNotNull null
            val text = item.stringValue("teks")
            if (text.isBlank()) return@mapNotNull null
            TafsirEntry(
                surahNumber = surahNumber,
                ayatNumber = ayatNumber,
                sourceName = sourceName,
                sourceDescription = sourceDescription,
                text = text
            )
        }
    }

    private fun JsonObject.findArray(key: String): JsonArray? {
        val value = get(key) ?: return null
        return if (value.isJsonArray) value.asJsonArray else null
    }

    private fun JsonObject.stringValue(key: String): String {
        val value = get(key) ?: return ""
        if (value.isJsonNull) return ""
        return value.asString
    }

    private fun JsonElement.asJsonObjectOrNull(): JsonObject? {
        return if (isJsonObject) asJsonObject else null
    }
}
