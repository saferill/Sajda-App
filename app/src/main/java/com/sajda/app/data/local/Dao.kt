package com.sajda.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahDao {
    @Query("SELECT * FROM surah ORDER BY number ASC")
    fun observeAllSurah(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM surah WHERE number = :number")
    suspend fun getSurahByNumber(number: Int): SurahEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSurah(surahList: List<SurahEntity>)

    @Query("SELECT COUNT(*) FROM surah")
    suspend fun count(): Int

    @Query("SELECT * FROM surah")
    suspend fun getAllSurah(): List<SurahEntity>

    @Query(
        """
        UPDATE surah
        SET isDownloaded = :isDownloaded,
            localAudioPath = :localAudioPath,
            downloadedAt = :downloadedAt,
            downloadedReciterId = :downloadedReciterId
        WHERE number = :surahNumber
        """
    )
    suspend fun updateAudioState(
        surahNumber: Int,
        isDownloaded: Boolean,
        localAudioPath: String?,
        downloadedAt: Long?,
        downloadedReciterId: String?
    )

    @Query("SELECT * FROM surah WHERE isDownloaded = 1 ORDER BY downloadedAt DESC LIMIT 1")
    suspend fun getLastDownloadedSurah(): SurahEntity?
}

@Dao
interface AyatDao {
    @Query("SELECT * FROM ayat WHERE surahNumber = :surahNumber ORDER BY ayatNumber ASC")
    fun observeAyatBySurah(surahNumber: Int): Flow<List<AyatEntity>>

    @Query("SELECT * FROM ayat WHERE surahNumber = :surahNumber ORDER BY ayatNumber ASC")
    suspend fun getAyatBySurah(surahNumber: Int): List<AyatEntity>

    @Query("SELECT * FROM ayat WHERE surahNumber = :surahNumber AND ayatNumber = :ayatNumber")
    suspend fun getAyat(surahNumber: Int, ayatNumber: Int): AyatEntity?

    @Query("SELECT * FROM ayat ORDER BY surahNumber ASC, ayatNumber ASC LIMIT 1 OFFSET :offset")
    suspend fun getAyatByOffset(offset: Int): AyatEntity?

    @Query("SELECT * FROM ayat ORDER BY surahNumber ASC, ayatNumber ASC")
    suspend fun getAllAyat(): List<AyatEntity>

    @Query(
        """
        SELECT * FROM ayat
        WHERE textArabic LIKE '%' || :query || '%'
            OR translation LIKE '%' || :query || '%'
            OR transliteration LIKE '%' || :query || '%'
        ORDER BY surahNumber ASC, ayatNumber ASC
        LIMIT 40
        """
    )
    suspend fun searchAyat(query: String): List<AyatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAyat(ayatList: List<AyatEntity>)

    @Query("SELECT COUNT(*) FROM ayat")
    suspend fun count(): Int
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmark ORDER BY updatedAt DESC, createdAt DESC")
    fun observeBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmark WHERE surahNumber = :surahNumber AND ayatNumber = :ayatNumber")
    suspend fun getBookmark(surahNumber: Int, ayatNumber: Int): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmark WHERE surahNumber = :surahNumber AND ayatNumber = :ayatNumber")
    suspend fun deleteBookmarkByAyat(surahNumber: Int, ayatNumber: Int)
}

@Dao
interface LastReadDao {
    @Query("SELECT * FROM last_read WHERE id = 1")
    suspend fun getLastRead(): LastReadEntity?

    @Query("SELECT * FROM last_read WHERE id = 1")
    fun observeLastRead(): Flow<LastReadEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLastRead(lastRead: LastReadEntity)
}

@Dao
interface PrayerTimeDao {
    @Query("SELECT * FROM prayer_time WHERE date = :date LIMIT 1")
    suspend fun getPrayerTimeByDate(date: String): PrayerTimeEntity?

    @Query("SELECT * FROM prayer_time WHERE date = :date LIMIT 1")
    fun observePrayerTimeByDate(date: String): Flow<PrayerTimeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPrayerTimes(prayerTimes: List<PrayerTimeEntity>)

    @Query("SELECT * FROM prayer_time WHERE date >= :startDate ORDER BY date ASC")
    fun observePrayerTimesFrom(startDate: String): Flow<List<PrayerTimeEntity>>

    @Query("SELECT * FROM prayer_time WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getPrayerTimesBetween(startDate: String, endDate: String): List<PrayerTimeEntity>

    @Query("DELETE FROM prayer_time WHERE date < :startDate OR date > :endDate")
    suspend fun pruneOutsideRange(startDate: String, endDate: String)
}
