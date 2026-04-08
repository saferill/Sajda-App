package com.sajda.app.di

import android.content.Context
import com.sajda.app.data.local.SajdaDatabase
import com.sajda.app.data.local.SurahDao
import com.sajda.app.data.local.AyatDao
import com.sajda.app.data.local.BookmarkDao
import com.sajda.app.data.local.LastReadDao
import com.sajda.app.data.local.PrayerTimeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SajdaDatabase {
        return SajdaDatabase.getDatabase(context)
    }

    @Provides
    fun provideSurahDao(database: SajdaDatabase): SurahDao = database.surahDao()

    @Provides
    fun provideAyatDao(database: SajdaDatabase): AyatDao = database.ayatDao()

    @Provides
    fun provideBookmarkDao(database: SajdaDatabase): BookmarkDao = database.bookmarkDao()

    @Provides
    fun provideLastReadDao(database: SajdaDatabase): LastReadDao = database.lastReadDao()

    @Provides
    fun providePrayerTimeDao(database: SajdaDatabase): PrayerTimeDao = database.prayerTimeDao()
}
