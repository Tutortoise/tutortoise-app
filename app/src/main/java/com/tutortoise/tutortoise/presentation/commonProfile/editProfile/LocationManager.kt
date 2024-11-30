package com.tutortoise.tutortoise.presentation.commonProfile.editProfile

import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationManager(
    private val activity: EditProfileActivity,
    private val locationUI: LocationUI,
    private val mapUI: MapUI,
    private val coroutineScope: CoroutineScope,
    private val locationData: LocationData
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(3000L)
        .setMaxUpdateDelayMillis(10000L)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            handleLocationResult(result)
        }
    }

    @SuppressWarnings("MissingPermission")
    fun startLocationUpdates() {
        try {
            locationUI.showLoading()
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: SecurityException) {
            locationUI.showError("Location permission required")
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        locationUI.hideLoading()
    }

    private fun handleLocationResult(result: LocationResult) {
        result.lastLocation?.let { location ->
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val address = getAddressFromLocation(location)
                    withContext(Dispatchers.Main) {
                        address?.let { validAddress ->
                            updateLocationData(validAddress)
                            if (locationData.hasValidLocation()) {
                                mapUI.updateLocation(
                                    validAddress.latitude,
                                    validAddress.longitude,
                                    formatLocationText(locationData)
                                )
                            } else {
                                mapUI.hide()
                                locationUI.showError("Could not determine location. Please try again.")
                            }
                        } ?: run {
                            locationUI.hideLoading()
                            locationUI.updateLocationText("Location not found")
                            mapUI.hide()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        handleLocationError(e)
                    }
                }
            }
        }
    }

    private fun updateLocationData(address: Address) {
        locationData.apply {
            city = cleanLocationName(address.subAdminArea)
            district = cleanLocationName(address.locality) ?: cleanLocationName(address.subLocality)
        }
        locationUI.hideLoading()
        locationUI.updateLocationText(formatLocationText(locationData))
    }

    private fun getAddressFromLocation(location: Location): Address? {
        return try {
            Geocoder(activity, Locale.ENGLISH)
                .getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()
        } catch (e: Exception) {
            Log.e("LocationManager", "Error getting address", e)
            null
        }
    }

    private fun handleLocationError(error: Exception) {
        locationUI.hideLoading()
        locationUI.showError("Error getting location: ${error.message}")
        mapUI.hide()
    }

    private fun formatLocationText(locationData: LocationData): String {
        return listOfNotNull(locationData.district, locationData.city)
            .joinToString(", ")
            .takeIf { it.isNotEmpty() } ?: "Your location"
    }

    private fun cleanLocationName(name: String?): String? {
        if (name.isNullOrEmpty()) return null

        val prefixes = listOf(
            "kota", "kabupaten", "kab.", "kota administratif",
            "kecamatan", "kec.", "administrative city",
            "city of", "district of", "regency of",
            "administrative regency", "administrative district",
            "administrative city", "city", "regency",
            "district", "administrative", "administratif",
            "administratif kota", "administratif kabupaten",
            "administratif kecamatan", "administratif kec.",
        )

        val pattern = prefixes.joinToString("|") { "^$it\\s+" }.toRegex(RegexOption.IGNORE_CASE)
        return name.replace(pattern, "").trim()
    }
}