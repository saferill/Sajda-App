package com.sajda.app.util

import com.sajda.app.domain.model.PrayerTime
import java.time.LocalDate
import java.util.Locale

object LocationUpdateDecider {
    fun shouldRefresh(
        savedCity: String?,
        newCity: String,
        cachedSchedules: List<PrayerTime>,
        today: LocalDate = LocalDate.now()
    ): Boolean {
        val normalizedSaved = normalizeCityName(savedCity.orEmpty())
        val normalizedNew = normalizeCityName(newCity)
        if (normalizedNew.isBlank()) return false
        if (!normalizedSaved.equals(normalizedNew, ignoreCase = true)) return true
        return cachedSchedules.none { it.date == today.toString() }
    }

    fun normalizeCityName(value: String): String {
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
}
