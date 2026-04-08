package com.sajda.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SurahEntity::class,
        AyatEntity::class,
        BookmarkEntity::class,
        LastReadEntity::class,
        PrayerTimeEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class SajdaDatabase : RoomDatabase() {

    abstract fun surahDao(): SurahDao
    abstract fun ayatDao(): AyatDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun lastReadDao(): LastReadDao
    abstract fun prayerTimeDao(): PrayerTimeDao

    companion object {
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE bookmark ADD COLUMN folderName TEXT NOT NULL DEFAULT 'Favorites'"
                )
                database.execSQL(
                    "ALTER TABLE bookmark ADD COLUMN highlightColor TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE bookmark ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "UPDATE bookmark SET updatedAt = createdAt WHERE updatedAt = 0"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE surah ADD COLUMN downloadedReciterId TEXT"
                )
            }
        }

        @Volatile
        private var INSTANCE: SajdaDatabase? = null

        fun getDatabase(context: Context): SajdaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SajdaDatabase::class.java,
                    "sajda_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
