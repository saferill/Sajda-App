package com.sajda.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surah")
data class SurahEntity(
    @PrimaryKey val number: Int,
    val nameArabic: String,
    val transliteration: String,
    val translation: String,
    val revelationPlace: String,
    val totalVerses: Int,
    val audioUrl: String,
    val isDownloaded: Boolean = false,
    val localAudioPath: String? = null,
    val downloadedAt: Long? = null,
    val downloadedReciterId: String? = null
)

@Entity(tableName = "ayat")
data class AyatEntity(
    @PrimaryKey val id: Int,
    val surahNumber: Int,
    val ayatNumber: Int,
    val textArabic: String,
    val translation: String,
    val transliteration: String
)

@Entity(tableName = "bookmark")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surahNumber: Int,
    val ayatNumber: Int,
    val surahName: String,
    val folderName: String = "Favorites",
    val note: String = "",
    val highlightColor: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "last_read")
data class LastReadEntity(
    @PrimaryKey val id: Int = 1,
    val surahNumber: Int,
    val ayatNumber: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "prayer_time")
data class PrayerTimeEntity(
    @PrimaryKey val date: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val qiblaDirection: Double
)
