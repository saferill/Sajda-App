package com.sajda.app.util

import com.sajda.app.domain.model.AsrMadhhab
import com.sajda.app.domain.model.PrayerCalculationMethod
import com.sajda.app.domain.model.PrayerTime
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

object PrayerTimeCalculator {

    data class DetailedPrayerTimes(
        val date: String,
        val locationName: String,
        val latitude: Double,
        val longitude: Double,
        val imsak: String,
        val fajr: String,
        val sunrise: String,
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String,
        val qiblaDirection: Double
    ) {
        fun toPrayerTime(): PrayerTime {
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
    }

    fun calculatePrayerTime(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        locationName: String,
        calculationMethod: PrayerCalculationMethod = PrayerCalculationMethod.KEMENAG,
        asrMadhhab: AsrMadhhab = AsrMadhhab.SHAFII,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): PrayerTime {
        return calculateDetailedPrayerTimes(
            date = date,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            calculationMethod = calculationMethod,
            asrMadhhab = asrMadhhab,
            zoneId = zoneId
        ).toPrayerTime()
    }

    fun calculateDetailedPrayerTimes(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        locationName: String,
        calculationMethod: PrayerCalculationMethod = PrayerCalculationMethod.KEMENAG,
        asrMadhhab: AsrMadhhab = AsrMadhhab.SHAFII,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): DetailedPrayerTimes {
        val offsetHours = zoneId.rules.getOffset(date.atStartOfDay(zoneId).toInstant()).totalSeconds / 3600.0
        val dayOfYear = date.dayOfYear.toDouble()
        val gamma = 2.0 * Math.PI / 365.0 * (dayOfYear - 1.0)

        val equationOfTime = 229.18 * (
            0.000075 +
                0.001868 * cos(gamma) -
                0.032077 * sin(gamma) -
                0.014615 * cos(2 * gamma) -
                0.040849 * sin(2 * gamma)
            )

        val declination = 0.006918 -
            0.399912 * cos(gamma) +
            0.070257 * sin(gamma) -
            0.006758 * cos(2 * gamma) +
            0.000907 * sin(2 * gamma) -
            0.002697 * cos(3 * gamma) +
            0.00148 * sin(3 * gamma)

        val latitudeRad = Math.toRadians(latitude)
        val solarNoon = 720 - (4 * longitude) - equationOfTime + (offsetHours * 60)

        val fajrZenith = 90.0 + calculationMethod.fajrAngle
        val ishaZenith = 90.0 + (calculationMethod.ishaAngle ?: calculationMethod.fajrAngle)
        val fajr = calculateSolarEventMinutes(latitudeRad, declination, fajrZenith, solarNoon, morning = true) ?: 290.0
        val sunrise = calculateSolarEventMinutes(latitudeRad, declination, 90.833, solarNoon, morning = true) ?: 360.0
        val dhuhr = solarNoon + 2.0
        val asr = calculateAsrMinutes(
            latitudeRad = latitudeRad,
            declination = declination,
            solarNoon = dhuhr,
            shadowFactor = asrMadhhab.shadowFactor
        ) ?: (dhuhr + if (asrMadhhab == AsrMadhhab.HANAFI) 210.0 else 180.0)
        val maghrib = calculateSolarEventMinutes(latitudeRad, declination, 90.833, solarNoon, morning = false) ?: 1080.0
        val isha = calculationMethod.ishaIntervalMinutes?.let { maghrib + it } ?:
            (calculateSolarEventMinutes(latitudeRad, declination, ishaZenith, solarNoon, morning = false) ?: 1155.0)

        val safeDhuhr = dhuhr.coerceAtLeast(sunrise + 90)
        val safeAsr = asr.coerceAtLeast(safeDhuhr + 110)
        val safeIsha = isha.coerceAtLeast(maghrib + 55)

        return DetailedPrayerTimes(
            date = date.toString(),
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            imsak = formatMinutes(fajr - 10.0),
            fajr = formatMinutes(fajr),
            sunrise = formatMinutes(sunrise),
            dhuhr = formatMinutes(safeDhuhr),
            asr = formatMinutes(safeAsr),
            maghrib = formatMinutes(maghrib),
            isha = formatMinutes(safeIsha),
            qiblaDirection = calculateQiblaDirection(latitude, longitude)
        )
    }

    fun calculateQiblaDirection(latitude: Double, longitude: Double): Double {
        val makkahLatitude = Math.toRadians(21.4225)
        val makkahLongitude = Math.toRadians(39.8262)
        val currentLatitude = Math.toRadians(latitude)
        val currentLongitude = Math.toRadians(longitude)
        val deltaLongitude = makkahLongitude - currentLongitude

        val y = sin(deltaLongitude)
        val x = (cos(currentLatitude) * tan(makkahLatitude)) - (sin(currentLatitude) * cos(deltaLongitude))
        return (Math.toDegrees(Math.atan2(y, x)) + 360.0) % 360.0
    }

    private fun calculateSolarEventMinutes(
        latitudeRad: Double,
        declination: Double,
        zenithDegrees: Double,
        solarNoon: Double,
        morning: Boolean
    ): Double? {
        val hourAngle = calculateHourAngle(latitudeRad, declination, zenithDegrees) ?: return null
        return if (morning) solarNoon - (4 * hourAngle) else solarNoon + (4 * hourAngle)
    }

    private fun calculateAsrMinutes(
        latitudeRad: Double,
        declination: Double,
        solarNoon: Double,
        shadowFactor: Double
    ): Double? {
        val altitude = Math.toDegrees(atan(1.0 / (shadowFactor + tan(abs(latitudeRad - declination)))))
        val zenith = 90.0 - altitude
        val hourAngle = calculateHourAngle(latitudeRad, declination, zenith) ?: return null
        return solarNoon + (4 * hourAngle)
    }

    private fun calculateHourAngle(latitudeRad: Double, declination: Double, zenithDegrees: Double): Double? {
        val zenithRad = Math.toRadians(zenithDegrees)
        val cosHourAngle = (
            cos(zenithRad) - (sin(latitudeRad) * sin(declination))
            ) / (cos(latitudeRad) * cos(declination))

        if (cosHourAngle !in -1.0..1.0) {
            return null
        }

        return Math.toDegrees(acos(cosHourAngle))
    }

    private fun formatMinutes(minutes: Double): String {
        var safeMinutes = minutes.roundToInt()
        while (safeMinutes < 0) safeMinutes += 1440
        safeMinutes %= 1440
        val hour = safeMinutes / 60
        val minute = safeMinutes % 60
        return "%02d:%02d".format(hour, minute)
    }
}
