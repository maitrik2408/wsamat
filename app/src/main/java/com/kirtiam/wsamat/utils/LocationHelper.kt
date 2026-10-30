package com.kirtiam.wsamat.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    data class LocationResult(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float,
        val timestamp: Long,
        val mapUrl: String,
        val formattedAddress: String
    )

    fun fetchCurrentLocation(onResult: (LocationResult?) -> Unit) {
        val hasFine = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            onResult(null)
            return
        }

        var callbackCalled = false
        val mainHandler = Handler(Looper.getMainLooper())

        fun safeDeliver(result: LocationResult?) {
            if (!callbackCalled) {
                callbackCalled = true
                mainHandler.post { onResult(result) }
            }
        }

        val cancellationTokenSource = CancellationTokenSource()

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    safeDeliver(buildLocationResult(location))
                } else {
                    fetchFallbackLocation { fallbackLoc ->
                        safeDeliver(fallbackLoc)
                    }
                }
            }.addOnFailureListener {
                fetchFallbackLocation { fallbackLoc ->
                    safeDeliver(fallbackLoc)
                }
            }
        } catch (e: Exception) {
            fetchFallbackLocation { fallbackLoc ->
                safeDeliver(fallbackLoc)
            }
        }

        // Safety timeout of 6 seconds
        mainHandler.postDelayed({
            if (!callbackCalled) {
                cancellationTokenSource.cancel()
                fetchFallbackLocation { fallbackLoc ->
                    safeDeliver(fallbackLoc)
                }
            }
        }, 6000)
    }

    @SuppressLint("MissingPermission")
    private fun fetchFallbackLocation(onResult: (LocationResult?) -> Unit) {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    onResult(buildLocationResult(loc))
                } else {
                    tryLocationManagerFallback(onResult)
                }
            }.addOnFailureListener {
                tryLocationManagerFallback(onResult)
            }
        } catch (e: Exception) {
            tryLocationManagerFallback(onResult)
        }
    }

    @SuppressLint("MissingPermission")
    private fun tryLocationManagerFallback(onResult: (LocationResult?) -> Unit) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                onResult(null)
                return
            }

            val gpsLocation = try { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: Exception) { null }
            val networkLocation = try { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: Exception) { null }
            val passiveLocation = try { locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) } catch (e: Exception) { null }

            val candidates = listOfNotNull(gpsLocation, networkLocation, passiveLocation)
            val bestLocation = candidates.maxByOrNull { it.time }

            if (bestLocation != null) {
                onResult(buildLocationResult(bestLocation))
                return
            }

            val provider = when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> null
            }

            if (provider != null) {
                val handler = Handler(Looper.getMainLooper())
                var listenerCalled = false

                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (!listenerCalled) {
                            listenerCalled = true
                            try { locationManager.removeUpdates(this) } catch (e: Exception) {}
                            onResult(buildLocationResult(location))
                        }
                    }

                    @Deprecated("Deprecated in API 29")
                    override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
                    override fun onProviderEnabled(p: String) {}
                    override fun onProviderDisabled(p: String) {}
                }

                try {
                    locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                    handler.postDelayed({
                        if (!listenerCalled) {
                            listenerCalled = true
                            try { locationManager.removeUpdates(listener) } catch (e: Exception) {}
                            onResult(null)
                        }
                    }, 4000)
                } catch (e: Exception) {
                    onResult(null)
                }
            } else {
                onResult(null)
            }
        } catch (e: Exception) {
            onResult(null)
        }
    }

    private fun buildLocationResult(location: Location): LocationResult {
        val mapUrl = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val formattedTime = dateFormat.format(Date(location.time))
        val addressStr = "Lat: %.5f, Lng: %.5f (Accuracy: %.0fm, Time: %s)"
            .format(location.latitude, location.longitude, location.accuracy, formattedTime)

        return LocationResult(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            timestamp = location.time,
            mapUrl = mapUrl,
            formattedAddress = addressStr
        )
    }
}
