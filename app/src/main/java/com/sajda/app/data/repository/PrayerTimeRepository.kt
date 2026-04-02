package com.sajda.app.data.repository

import com.sajda.app.data.local.PrayerTimeEntity
import com.sajda.app.data.local.SajdaDatabase
import com.sajda.app.domain.model.PrayerTime
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.util.DateTimeUtils
import com.sajda.app.util.PrayerTimeCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class PrayerTimeRepository(private val database: SajdaDatabase) {

    private val prayerTimeDao = database.prayerTimeDao()

    fun observeTodayPrayerTime(): Flow<PrayerTime?> {
        return prayerTimeDao.observePrayerTimeByDate(DateTimeUtils.todayString()).map { it?.toModel() }
    }

    fun observeWeeklyPrayerTimes(): Flow<List<PrayerTime>> {
        return prayerTimeDao.observePrayerTimesFrom(DateTimeUtils.todayString()).map { items ->
            items.take(7).map { it.toModel() }
        }
    }

    fun observeMonthlyPrayerTimes(): Flow<List<PrayerTime>> {
        return prayerTimeDao.observePrayerTimesFrom(DateTimeUtils.todayString()).map { items ->
            items.take(30).map { it.toModel() }
        }
    }

    suspend fun refreshPrayerTimes(settings: UserSettings, days: Int = 30): List<PrayerTime> {
        val startDate = LocalDate.now()
        val times = (0 until days).map { index ->
            PrayerTimeCalculator.calculatePrayerTime(
                date = startDate.plusDays(index.toLong()),
                latitude = settings.latitude,
                longitude = settings.longitude,
                locationName = settings.locationName,
                calculationMethod = settings.prayerCalculationMethod,
                asrMadhhab = settings.asrMadhhab
            )
        }

        prayerTimeDao.insertAllPrayerTimes(times.map { it.toEntity() })
        prayerTimeDao.pruneOutsideRange(
            startDate.toString(),
            startDate.plusDays(days.toLong() - 1L).toString()
        )
        return times
    }

    suspend fun getTodayPrayerTime(): PrayerTime? =
        prayerTimeDao.getPrayerTimeByDate(DateTimeUtils.todayString())?.toModel()

    suspend fun getNextDaysPrayerTimes(days: Int = 7): List<PrayerTime> {
        val start = LocalDate.now()
        val end = start.plusDays(days.toLong() - 1L)
        return prayerTimeDao.getPrayerTimesBetween(start.toString(), end.toString()).map { it.toModel() }
    }

    private fun PrayerTimeEntity.toModel(): PrayerTime {
        return PrayerTime(
            date = date,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            fajr = fajr,
            dhuhr = dhuhr,
            asr = asr,
            maghrib = maghrib,
            isha = isha,
            qiblaDirection = qiblaDirection
        )
    }

    private fun PrayerTime.toEntity(): PrayerTimeEntity {
        return PrayerTimeEntity(
            date = date,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            fajr = fajr,
            dhuhr = dhuhr,
            asr = asr,
            maghrib = maghrib,
            isha = isha,
            qiblaDirection = qiblaDirection
        )
    }
}
