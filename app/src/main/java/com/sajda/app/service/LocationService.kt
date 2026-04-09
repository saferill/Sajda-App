package com.sajda.app.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class LocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val cityName: String
)

class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    // Ambil lokasi aktif lalu reverse geocode ke nama kota.
    suspend fun getCurrentLocationSnapshot(): LocationSnapshot? {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Permission lokasi belum diberikan")
            return null
        }

        return try {
            val location = getBestLocation() ?: run {
                Log.w(TAG, "Lokasi perangkat tidak tersedia")
                return null
            }

            val cityName = reverseGeocode(location.latitude, location.longitude)
            if (cityName.isBlank()) {
                Log.w(TAG, "Reverse geocode gagal mendapatkan nama kota")
                return null
            }

            LocationSnapshot(
                latitude = location.latitude,
                longitude = location.longitude,
                cityName = cityName
            )
        } catch (error: Exception) {
            Log.e(TAG, "Gagal ambil lokasi terkini", error)
            null
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getBestLocation(): Location? {
        val lastLocation = suspendCancellableCoroutine<Location?> { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener {
                    Log.e(TAG, "lastLocation gagal", it)
                    continuation.resume(null)
                }
        }

        if (lastLocation != null) {
            Log.d(TAG, "Menggunakan lastLocation")
            return lastLocation
        }

        Log.d(TAG, "lastLocation null, meminta fresh location")
        return requestFreshLocation()
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFreshLocation(): Location? {
        return suspendCancellableCoroutine { continuation ->
            val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 0L)
                .setMaxUpdates(1)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)
                    continuation.resume(result.lastLocation)
                }
            }

            continuation.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(callback)
            }

            fusedLocationClient.requestLocationUpdates(
                request,
                callback,
                Looper.getMainLooper()
            ).addOnFailureListener { error ->
                Log.e(TAG, "requestLocationUpdates gagal", error)
                fusedLocationClient.removeLocationUpdates(callback)
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        }
    }

    private suspend fun reverseGeocode(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale("id", "ID"))
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address = addresses?.firstOrNull()
                listOf(
                    address?.subAdminArea,
                    address?.locality,
                    address?.adminArea
                ).firstOrNull { !it.isNullOrBlank() }.orEmpty()
            } catch (error: Exception) {
                Log.e(TAG, "Reverse geocode error", error)
                ""
            }
        }
    }

    companion object {
        private const val TAG = "LocationService"
    }
}
