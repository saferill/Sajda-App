package com.sajda.app.service

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.local.SajdaDatabase
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.domain.model.PrayerTime
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AdzanManager(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val locationService = LocationService(context)
    private val apiService = RetrofitClient.apiService
    private val prayerTimeRepository = PrayerTimeRepository(SajdaDatabase.getDatabase(context))
    private val preferencesDataStore = PreferencesDataStore(context)
    private val adzanScheduler = AdzanScheduler(context)

    // Cek kota GPS, bandingkan dengan kota tersimpan, lalu update jadwal jika perlu.
    suspend fun checkAndUpdateLocation(): Boolean {
        return try {
            val locationSnapshot = locationService.getCurrentLocationSnapshot() ?: return false
            val newCity = normalizeCityName(locationSnapshot.cityName)
            val savedCity = getSavedCity()
            val cachedSchedules = getSavedPrayerTimes()

            Log.d(TAG, "Kota GPS=$newCity, kota tersimpan=$savedCity")

            if (savedCity.equals(newCity, ignoreCase = true) && hasTodaySchedule(cachedSchedules)) {
                Log.d(TAG, "Kota sama dan jadwal hari ini sudah ada, skip fetch ulang")
                return false
            }

            val cityId = findCityIdFromApi(newCity) ?: run {
                Log.e(TAG, "ID kota tidak ditemukan untuk $newCity")
                return false
            }

            val schedules = fetchPrayerSchedules(cityId, newCity, locationSnapshot.latitude, locationSnapshot.longitude)
            if (schedules.isEmpty()) {
                Log.e(TAG, "Jadwal sholat dari API kosong")
                return false
            }

            if (cachedSchedules.isNotEmpty()) {
                Log.d(TAG, "Membatalkan alarm adzan lama")
                adzanScheduler.cancelUpcoming(cachedSchedules)
            }

            saveToSharedPreferences(newCity, cityId, schedules)
            prayerTimeRepository.savePrayerTimes(schedules)
            preferencesDataStore.updateLocation(
                locationName = newCity,
                latitude = locationSnapshot.latitude,
                longitude = locationSnapshot.longitude,
                automatic = true
            )

            val settings = preferencesDataStore.settingsFlow.first()
            adzanScheduler.reschedule(schedules, settings)
            Log.d(TAG, "Alarm adzan berhasil di-set ulang untuk kota $newCity")
            true
        } catch (error: Exception) {
            Log.e(TAG, "checkAndUpdateLocation gagal", error)
            false
        }
    }

    fun hasCachedScheduleForToday(): Boolean = hasTodaySchedule(getSavedPrayerTimes())

    private suspend fun findCityIdFromApi(cityName: String): String? {
        return try {
            Log.d(TAG, "Mencari ID kota baru ke API MyQuran: $cityName")
            val response = apiService.getAllCities()
            val target = normalizeCityName(cityName)
            response.data.firstOrNull { city ->
                val candidate = normalizeCityName(city.lokasi)
                candidate == target || candidate.contains(target) || target.contains(candidate)
            }?.id
        } catch (error: Exception) {
            Log.e(TAG, "Gagal mencari ID kota", error)
            null
        }
    }

    private suspend fun fetchPrayerSchedules(
        cityId: String,
        cityName: String,
        latitude: Double,
        longitude: Double
    ): List<PrayerTime> {
        val today = LocalDate.now()
        return listOf(today, today.plusDays(1)).mapNotNull { date ->
            try {
                Log.d(TAG, "Fetch jadwal sholat ${date} untuk cityId=$cityId")
                val response = apiService.getPrayerSchedule(cityId, date.year, date.monthValue, date.dayOfMonth)
                response.data?.jadwal?.toPrayerTime(
                    date = date,
                    cityName = cityName,
                    latitude = latitude,
                    longitude = longitude
                )
            } catch (error: Exception) {
                Log.e(TAG, "Gagal fetch jadwal sholat untuk $date", error)
                null
            }
        }
    }

    private fun saveToSharedPreferences(cityName: String, cityId: String, schedules: List<PrayerTime>) {
        sharedPreferences.edit()
            .putString(KEY_CITY_NAME, cityName)
            .putString(KEY_CITY_ID, cityId)
            .putString(KEY_SCHEDULES_JSON, gson.toJson(schedules))
            .apply()
        Log.d(TAG, "SharedPreferences diperbarui untuk kota $cityName")
    }

    private fun getSavedCity(): String? = sharedPreferences.getString(KEY_CITY_NAME, null)

    private fun getSavedPrayerTimes(): List<PrayerTime> {
        val raw = sharedPreferences.getString(KEY_SCHEDULES_JSON, null).orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<PrayerTime>>() {}.type
            gson.fromJson<List<PrayerTime>>(raw, type).orEmpty()
        }.getOrElse {
            Log.e(TAG, "Gagal parse jadwal dari SharedPreferences", it)
            emptyList()
        }
    }

    private fun hasTodaySchedule(schedules: List<PrayerTime>): Boolean {
        val today = LocalDate.now().toString()
        return schedules.any { it.date == today }
    }

    private fun normalizeCityName(value: String): String {
        return value
            .lowercase(Locale.US)
            .replace("kota", "")
            .replace("kabupaten", "")
            .replace("kab.", "")
            .replace("provinsi", "")
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun MyQuranDailySchedule.toPrayerTime(
        date: LocalDate,
        cityName: String,
        latitude: Double,
        longitude: Double
    ): PrayerTime {
        val normalizedDate = parseMyQuranDate(tanggal) ?: date.toString()
        return PrayerTime(
            date = normalizedDate,
            locationName = cityName,
            latitude = latitude,
            longitude = longitude,
            fajr = subuh,
            dhuhr = dzuhur,
            asr = ashar,
            maghrib = maghrib,
            isha = isya,
            qiblaDirection = 0.0
        )
    }

    private fun parseMyQuranDate(rawDate: String): String? {
        if (rawDate.isBlank()) return null
        return runCatching {
            LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }.recoverCatching {
            LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        }.getOrNull()?.toString()
    }

    companion object {
        private const val TAG = "AdzanManager"
        private const val PREFS_NAME = "auto_adzan_prefs"
        private const val KEY_CITY_NAME = "city_name"
        private const val KEY_CITY_ID = "city_id"
        private const val KEY_SCHEDULES_JSON = "schedules_json"
    }
}
