package com.sajda.app.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.sajda.app.domain.model.Ayat
import com.sajda.app.domain.model.Surah

object QuranDataLoader {

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

        val surahs = translatedSurahs.map { asset ->
            Surah(
                number = asset.id,
                nameArabic = asset.name,
                transliteration = asset.transliteration,
                translation = asset.translation,
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
                    transliteration = transliterationLookup[verse.id]?.transliteration.orEmpty()
                )
            }
        }

        return surahs to ayats
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
