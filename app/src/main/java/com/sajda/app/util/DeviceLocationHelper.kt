package com.sajda.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import com.sajda.app.domain.model.CityPreset
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class DeviceLocation(
    val label: String,
    val latitude: Double,
    val longitude: Double
)

sealed interface DeviceLocationResult {
    data class Success(val location: DeviceLocation) : DeviceLocationResult
    data class Error(val message: String) : DeviceLocationResult
}

object DeviceLocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    suspend fun getCurrentLocation(context: Context): DeviceLocationResult {
        if (!hasLocationPermission(context)) {
            return DeviceLocationResult.Error("Izin lokasi belum diberikan.")
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return DeviceLocationResult.Error("Layanan lokasi tidak tersedia di perangkat ini.")

        val enabledProviders = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        ).filter { provider ->
            runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
        }

        if (enabledProviders.isEmpty()) {
            return DeviceLocationResult.Error("GPS atau layanan lokasi perangkat sedang nonaktif.")
        }

        val freshLocation = enabledProviders.firstNotNullOfOrNull { provider ->
            requestSingleLocation(context, locationManager, provider)
        }

        val bestLocation = freshLocation
            ?: enabledProviders
                .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
                .maxByOrNull(Location::getTime)

        val location = bestLocation
            ?: return DeviceLocationResult.Error("Lokasi belum bisa didapatkan. Coba aktifkan GPS lalu tunggu beberapa detik.")

        return DeviceLocationResult.Success(
            DeviceLocation(
                label = resolveLocationLabel(location.latitude, location.longitude),
                latitude = location.latitude,
                longitude = location.longitude
            )
        )
    }

    private suspend fun requestSingleLocation(
        context: Context,
        locationManager: LocationManager,
        provider: String
    ): Location? {
        return withTimeoutOrNull(10_000L) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                suspendCancellableCoroutine { continuation ->
                    locationManager.getCurrentLocation(provider, null, context.mainExecutor) { location ->
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                suspendCancellableCoroutine { continuation ->
                    val listener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            locationManager.removeUpdates(this)
                            if (continuation.isActive) {
                                continuation.resume(location)
                            }
                        }
                    }

                    continuation.invokeOnCancellation {
                        locationManager.removeUpdates(listener)
                    }

                    locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                }
            }
        }
    }

    private fun resolveLocationLabel(latitude: Double, longitude: Double): String {
        val nearestCity = LocationConstants.cityPresets.minByOrNull { city ->
            distanceKm(latitude, longitude, city)
        }

        val nearestDistance = nearestCity?.let { distanceKm(latitude, longitude, it) }
        if (nearestCity != null && nearestDistance != null && nearestDistance <= 90.0) {
            return nearestCity.name
        }

        return "GPS ${"%.3f".format(Locale.US, latitude)}, ${"%.3f".format(Locale.US, longitude)}"
    }

    private fun distanceKm(latitude: Double, longitude: Double, city: CityPreset): Double {
        val earthRadiusKm = 6371.0
        val latDistance = Math.toRadians(city.latitude - latitude)
        val lonDistance = Math.toRadians(city.longitude - longitude)
        val startLatitude = Math.toRadians(latitude)
        val endLatitude = Math.toRadians(city.latitude)

        val haversine = sin(latDistance / 2).pow2() +
            sin(lonDistance / 2).pow2() * cos(startLatitude) * cos(endLatitude)
        val arc = 2 * atan2(sqrt(haversine), sqrt(1 - haversine))
        return earthRadiusKm * arc
    }

    private fun Double.pow2(): Double = this * this
}
