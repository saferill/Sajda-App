package com.sajda.app.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.sajda.app.domain.model.Ayat
import com.sajda.app.domain.model.Surah

object QuranDataLoader {

    private var englishSurahTranslations: Map<Int, String> = emptyMap()
    private var englishAyatTranslations: Map<Pair<Int, Int>, String> = emptyMap()

    fun loadQuranData(context: Context): Pair<List<Surah>, List<Ayat>> {
        val gson = Gson()
        val listType = object : TypeToken<List<QuranAssetSurah>>() {}.type
        val translatedSurahs = gson.fromJson<List<QuranAssetSurah>>(
            readAsset(context, Constants.QURAN_TRANSLATION_ASSET),
            listType
        )
        val transliterationSurahs = gson.fromJson<List<QuranAssetSurah>>(
            readAsset(context, Constants.QURAN_TRANSLITERATION_ASSET),
            listType
        ).associateBy { it.id }
        ensureEnglishTranslationsLoaded(context, gson)

        val surahs = translatedSurahs.map { asset ->
            Surah(
                number = asset.id,
                nameArabic = asset.name,
                transliteration = asset.transliteration,
                translation = asset.translation,
                englishTranslation = englishSurahTranslations[asset.id].orEmpty(),
                revelationPlace = asset.type.replaceFirstChar { it.uppercase() },
                totalVerses = asset.totalVerses,
                audioUrl = Constants.buildMurattalUrl(asset.id)
            )
        }

        val ayats = translatedSurahs.flatMap { asset ->
            val transliterationLookup = transliterationSurahs[asset.id]
                ?.verses
                ?.associateBy { it.id }
                .orEmpty()

            asset.verses.map { verse ->
                Ayat(
                    id = (asset.id * 1000) + verse.id,
                    surahNumber = asset.id,
                    ayatNumber = verse.id,
                    textArabic = verse.text,
                    translation = verse.translation.orEmpty(),
                    englishTranslation = englishAyatTranslations[asset.id to verse.id].orEmpty(),
                    transliteration = transliterationLookup[verse.id]?.transliteration.orEmpty()
                )
            }
        }

        return surahs to ayats
    }

    fun prepareSupportingData(context: Context) {
        ensureEnglishTranslationsLoaded(context, Gson())
    }

    fun englishSurahTranslation(surahNumber: Int): String = englishSurahTranslations[surahNumber].orEmpty()

    fun englishAyatTranslation(surahNumber: Int, ayatNumber: Int): String =
        englishAyatTranslations[surahNumber to ayatNumber].orEmpty()

    private fun ensureEnglishTranslationsLoaded(context: Context, gson: Gson) {
        if (englishSurahTranslations.isNotEmpty() && englishAyatTranslations.isNotEmpty()) return

        val listType = object : TypeToken<List<QuranEnglishAssetSurah>>() {}.type
        val englishSurahs = runCatching {
            gson.fromJson<List<QuranEnglishAssetSurah>>(
                readAsset(context, Constants.QURAN_ENGLISH_ASSET),
                listType
            )
        }.getOrDefault(emptyList())

        englishSurahTranslations = englishSurahs.associate { it.id to it.translation }
        englishAyatTranslations = englishSurahs.flatMap { surah ->
            surah.verses.map { verse ->
                (surah.id to verse.id) to verse.translation
            }
        }.toMap()
    }

    private fun readAsset(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}

private data class QuranAssetSurah(
    val id: Int,
    val name: String,
    val transliteration: String,
    val translation: String,
    val type: String,
    @SerializedName("total_verses")
    val totalVerses: Int,
    val verses: List<QuranAssetVerse>
)

private data class QuranAssetVerse(
    val id: Int,
    val text: String,
    val translation: String? = null,
    val transliteration: String? = null
)

private data class QuranEnglishAssetSurah(
    val id: Int,
    val translation: String,
    val verses: List<QuranEnglishAssetVerse>
)

private data class QuranEnglishAssetVerse(
    val id: Int,
    val translation: String
)
