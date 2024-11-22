package com.tutortoise.tutortoise.presentation.menuLearner.profile.general

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.ActivityEditProfileBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted
                getCurrentLocation()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getCurrentLocation()
            }

            else -> {
                // No location access granted
                Toast.makeText(
                    this,
                    "Location permission is required to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000)
            .setMaxUpdateDelayMillis(10000)
            .build()

        locationCallback = object : LocationCallback() {
            // TODO: THIS PROBABLY FAILS ON SOME DEVICES
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val geocoder = Geocoder(this@EditProfileActivity)
                    try {
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1,
                            Geocoder.GeocodeListener { addresses ->
                                addresses.firstOrNull()?.let { address ->
                                    val locality = address.locality ?: ""
                                    val city = address.adminArea ?: ""

                                    val locationText =
                                        if (locality.isNotEmpty() && city.isNotEmpty()) {
                                            "$locality, $city"
                                        } else {
                                            locality.ifEmpty { city }
                                        }

                                    runOnUiThread {
                                        binding.btnLocation.text =
                                            locationText.ifEmpty { "Location not found" }
                                    }
                                }
                            }
                        )
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this@EditProfileActivity,
                                "Error getting address: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        binding.btnLocation.setOnClickListener {
            checkLocationPermission()
        }

        // Back to ProfileFragment in MainActivity
        binding.btnBack.setOnClickListener {
            backToProfile()
        }

        val spinnerGender = findViewById<AutoCompleteTextView>(R.id.spinnerGender)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            R.layout.dropdown_item
        )
        adapter.setDropDownViewResource(R.layout.dropdown_item)
        spinnerGender.setAdapter(adapter)
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }

            else -> {
                // Request location permissions
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun getCurrentLocation() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                "Location permission is required",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun backToProfile() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("startFragment", "profile")
        }
        startActivity(intent)
        finish()
    }
}