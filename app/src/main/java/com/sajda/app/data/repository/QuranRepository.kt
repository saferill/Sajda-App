package com.sajda.app.data.repository

import android.content.Context
import android.os.Environment
import com.sajda.app.data.local.AyatEntity
import com.sajda.app.data.local.BookmarkEntity
import com.sajda.app.data.local.LastReadEntity
import com.sajda.app.data.local.SajdaDatabase
import com.sajda.app.data.local.SurahEntity
import com.sajda.app.domain.model.Ayat
import com.sajda.app.domain.model.Bookmark
import com.sajda.app.domain.model.LastRead
import com.sajda.app.domain.model.QuranReciter
import com.sajda.app.domain.model.QuranSearchResult
import com.sajda.app.domain.model.SearchResultType
import com.sajda.app.domain.model.Surah
import com.sajda.app.util.Constants
import com.sajda.app.util.QuranDataLoader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.io.File

import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class QuranRepository @Inject constructor(
    private val database: SajdaDatabase,
    @ApplicationContext private val appContext: Context
) {

    private val surahDao = database.surahDao()
    private val ayatDao = database.ayatDao()
    private val bookmarkDao = database.bookmarkDao()
    private val lastReadDao = database.lastReadDao()

    suspend fun seedIfNeeded(context: Context) {
        QuranDataLoader.prepareSupportingData(context)
        if (surahDao.count() > 0 && ayatDao.count() > 0) return
        val (surahs, ayats) = QuranDataLoader.loadQuranData(context)
        surahDao.insertAllSurah(surahs.map { it.toEntity() })
        ayatDao.insertAllAyat(ayats.map { it.toEntity() })
    }

    fun observeAllSurah(): Flow<List<Surah>> = surahDao.observeAllSurah().map { items -> items.map { it.toModel() } }

    suspend fun getSurahByNumber(number: Int): Surah? = surahDao.getSurahByNumber(number)?.toModel()

    suspend fun getAyatBySurah(surahNumber: Int): List<Ayat> =
        ayatDao.getAyatBySurah(surahNumber).map { it.toModel() }

    fun observeAyatBySurah(surahNumber: Int): Flow<List<Ayat>> =
        ayatDao.observeAyatBySurah(surahNumber).map { items -> items.map { it.toModel() } }

    suspend fun getAyat(surahNumber: Int, ayatNumber: Int): Ayat? =
        ayatDao.getAyat(surahNumber, ayatNumber)?.toModel()

    suspend fun search(query: String): List<QuranSearchResult> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return emptyList()

        val surahResults = surahDao.getAllSurah()
            .filter { surah ->
                surah.transliteration.contains(trimmedQuery, ignoreCase = true) ||
                    surah.translation.contains(trimmedQuery, ignoreCase = true) ||
                    QuranDataLoader.englishSurahTranslation(surah.number).contains(trimmedQuery, ignoreCase = true) ||
                    surah.nameArabic.contains(trimmedQuery)
            }
            .take(10)
            .map { surah ->
                QuranSearchResult(
                    type = SearchResultType.SURAH,
                    title = surah.transliteration,
                    subtitle = listOf(
                        surah.translation,
                        QuranDataLoader.englishSurahTranslation(surah.number)
                    ).filter { it.isNotBlank() }.joinToString(" • "),
                    surahNumber = surah.number
                )
            }

        val ayatResults = ayatDao.searchAyat(trimmedQuery)
            .mapNotNull { ayat ->
                val surah = surahDao.getSurahByNumber(ayat.surahNumber) ?: return@mapNotNull null
                QuranSearchResult(
                    type = SearchResultType.AYAT,
                    title = "${surah.transliteration} • Ayat ${ayat.ayatNumber}",
                    subtitle = ayat.translation.take(120),
                    surahNumber = ayat.surahNumber,
                    ayatNumber = ayat.ayatNumber
                )
            }

        val englishAyatResults = ayatDao.getAllAyat()
            .filter { ayat ->
                QuranDataLoader.englishAyatTranslation(ayat.surahNumber, ayat.ayatNumber)
                    .contains(trimmedQuery, ignoreCase = true)
            }
            .take(20)
            .mapNotNull { ayat ->
                val surah = surahDao.getSurahByNumber(ayat.surahNumber) ?: return@mapNotNull null
                QuranSearchResult(
                    type = SearchResultType.AYAT,
                    title = "${surah.transliteration} • Ayah ${ayat.ayatNumber}",
                    subtitle = QuranDataLoader
                        .englishAyatTranslation(ayat.surahNumber, ayat.ayatNumber)
                        .take(120),
                    surahNumber = ayat.surahNumber,
                    ayatNumber = ayat.ayatNumber
                )
            }

        return (surahResults + ayatResults + englishAyatResults)
            .distinctBy { "${it.type}-${it.surahNumber}-${it.ayatNumber ?: 0}-${it.title}" }
            .take(24)
    }

    fun observeBookmarks(): Flow<List<Bookmark>> = bookmarkDao.observeBookmarks().map { items -> items.map { it.toModel() } }

    suspend fun addBookmark(bookmark: Bookmark) = bookmarkDao.insertBookmark(bookmark.toEntity())

    suspend fun removeBookmark(surahNumber: Int, ayatNumber: Int) = bookmarkDao.deleteBookmarkByAyat(surahNumber, ayatNumber)

    suspend fun getBookmark(surahNumber: Int, ayatNumber: Int): Bookmark? =
        bookmarkDao.getBookmark(surahNumber, ayatNumber)?.toModel()

    suspend fun saveBookmarkReflection(
        surahNumber: Int,
        ayatNumber: Int,
        surahName: String,
        folderName: String,
        note: String,
        highlightColor: String
    ) {
        val existing = bookmarkDao.getBookmark(surahNumber, ayatNumber)
        val now = System.currentTimeMillis()
        bookmarkDao.insertBookmark(
            BookmarkEntity(
                id = existing?.id ?: 0,
                surahNumber = surahNumber,
                ayatNumber = ayatNumber,
                surahName = surahName,
                folderName = folderName,
                note = note,
                highlightColor = highlightColor,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        )
    }

    fun observeLastRead(): Flow<LastRead?> = lastReadDao.observeLastRead().map { it?.toModel() }

    suspend fun getLastRead(): LastRead? = lastReadDao.getLastRead()?.toModel()

    suspend fun updateLastRead(surahNumber: Int, ayatNumber: Int) {
        lastReadDao.upsertLastRead(
            LastReadEntity(
                surahNumber = surahNumber,
                ayatNumber = ayatNumber,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateAudioState(
        surahNumber: Int,
        isDownloaded: Boolean,
        localAudioPath: String?,
        downloadedAt: Long?,
        downloadedReciterId: String?
    ) {
        surahDao.updateAudioState(
            surahNumber = surahNumber,
            isDownloaded = isDownloaded,
            localAudioPath = localAudioPath,
            downloadedAt = downloadedAt,
            downloadedReciterId = downloadedReciterId
        )
    }

    suspend fun getLastDownloadedSurah(): Surah? = surahDao.getLastDownloadedSurah()?.toModel()

    suspend fun getDailyAyat(date: LocalDate = LocalDate.now()): Ayat? {
        val ayatCount = ayatDao.count()
        if (ayatCount == 0) return null

        val seed = (date.toEpochDay() * 1103515245L + 12345L) and Long.MAX_VALUE
        val offset = (seed % ayatCount).toInt()
        return ayatDao.getAyatByOffset(offset)?.toModel()
    }

    private fun SurahEntity.toModel(): Surah {
        val downloadedFiles = downloadedAudioFiles(number)
        val downloadedIds = downloadedFiles.keys
        val activeAudioPath = localAudioPath
            ?.takeIf { File(it).exists() }
            ?: downloadedFiles.values.firstOrNull()?.absolutePath
        return Surah(
            number = number,
            nameArabic = nameArabic,
            transliteration = transliteration,
            translation = translation,
            englishTranslation = QuranDataLoader.englishSurahTranslation(number),
            revelationPlace = revelationPlace,
            totalVerses = totalVerses,
            audioUrl = audioUrl,
            isDownloaded = isDownloaded || downloadedIds.isNotEmpty(),
            localAudioPath = activeAudioPath,
            downloadedAt = downloadedAt,
            downloadedReciterId = downloadedReciterId,
            downloadedReciterIds = downloadedIds,
            downloadedAudioBytes = downloadedFiles.values.sumOf { it.length() }
        )
    }

    private fun Surah.toEntity(): SurahEntity {
        return SurahEntity(
            number = number,
            nameArabic = nameArabic,
            transliteration = transliteration,
            translation = translation,
            revelationPlace = revelationPlace,
            totalVerses = totalVerses,
            audioUrl = audioUrl,
            isDownloaded = isDownloaded,
            localAudioPath = localAudioPath,
            downloadedAt = downloadedAt,
            downloadedReciterId = downloadedReciterId
        )
    }

    private fun downloadedAudioFiles(surahNumber: Int): Map<String, File> {
        return QuranReciter.entries.mapNotNull { reciter ->
            val file = audioFileFor(surahNumber, reciter.id)
            if (file.exists()) {
                reciter.id to file
            } else {
                null
            }
        }.toMap()
    }

    private fun audioFileFor(surahNumber: Int, reciterId: String): File {
        val musicDir = appContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val targetDir = File(musicDir, Constants.AUDIO_DOWNLOAD_DIR).apply { mkdirs() }
        return File(targetDir, Constants.formatAudioFileName(surahNumber, reciterId))
    }

    private fun AyatEntity.toModel(): Ayat {
        return Ayat(
            id = id,
            surahNumber = surahNumber,
            ayatNumber = ayatNumber,
            textArabic = textArabic,
            translation = translation,
            englishTranslation = QuranDataLoader.englishAyatTranslation(surahNumber, ayatNumber),
            transliteration = transliteration
        )
    }

    private fun Ayat.toEntity(): AyatEntity {
        return AyatEntity(
            id = id,
            surahNumber = surahNumber,
            ayatNumber = ayatNumber,
            textArabic = textArabic,
            translation = translation,
            transliteration = transliteration
        )
    }

    private fun BookmarkEntity.toModel(): Bookmark {
        return Bookmark(
            id = id,
            surahNumber = surahNumber,
            ayatNumber = ayatNumber,
            surahName = surahName,
            folderName = folderName,
            note = note,
            highlightColor = highlightColor,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun Bookmark.toEntity(): BookmarkEntity {
        return BookmarkEntity(
            id = id,
            surahNumber = surahNumber,
            ayatNumber = ayatNumber,
            surahName = surahName,
            folderName = folderName,
            note = note,
            highlightColor = highlightColor,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun LastReadEntity.toModel(): LastRead {
        return LastRead(
            surahNumber = surahNumber,
            ayatNumber = ayatNumber,
            updatedAt = updatedAt
        )
    }
}
