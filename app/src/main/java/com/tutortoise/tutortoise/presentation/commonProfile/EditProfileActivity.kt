package com.tutortoise.tutortoise.presentation.commonProfile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.pref.ProfileData
import com.tutortoise.tutortoise.data.pref.UpdateLearnerProfileRequest
import com.tutortoise.tutortoise.data.pref.UpdateTutorProfileRequest
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.data.repository.LearnerRepository
import com.tutortoise.tutortoise.data.repository.TutorRepository
import com.tutortoise.tutortoise.databinding.ActivityEditProfileBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity
import com.tutortoise.tutortoise.utils.EventBus
import com.tutortoise.tutortoise.utils.ProfileUpdateEvent
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private lateinit var authRepository: AuthRepository
    private lateinit var learnerRepository: LearnerRepository
    private lateinit var tutorRepository: TutorRepository

    private data class LocationData(
        var city: String? = null,
        var district: String? = null
    )

    private val locationData = LocationData()

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.mapView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
    }

    private fun loadMap(latitude: Double, longitude: Double) {
        // Calculate offset coordinates for privacy (showing approximate location)
        val offsetLat = (latitude * 100).toInt() / 100.0
        val offsetLng = (longitude * 100).toInt() / 100.0

        binding.mapView.apply {
            visibility = View.VISIBLE
            loadUrl("file:///android_asset/map.html")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    // Create location text from city and district
                    val locationText = listOfNotNull(locationData.city, locationData.district)
                        .joinToString(", ")
                        .takeIf { it.isNotEmpty() } ?: "Your location"

                    // Initialize map with offset coordinates and location name
                    evaluateJavascript(
                        "initMap($offsetLat, $offsetLng, '$locationText')",
                        null
                    )
                }
            }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                val geocoder = Geocoder(this@EditProfileActivity)
                try {
                    val addresses = geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    )

                    addresses?.firstOrNull()?.let { address ->
                        val locality = address.locality ?: ""  // City
                        val adminArea = address.adminArea ?: ""  // District

                        // Update location data
                        locationData.city = locality
                        locationData.district = adminArea

                        val locationText = if (locality.isNotEmpty() && adminArea.isNotEmpty()) {
                            "$locality, $adminArea"
                        } else {
                            locality.ifEmpty { adminArea }
                        }

                        runOnUiThread {
                            hideLoadingState()
                            binding.btnLocation.text = locationText.ifEmpty { "Location not found" }

                            // Only show map if we have valid location data
                            if (locality.isNotEmpty() || adminArea.isNotEmpty()) {
                                loadMap(location.latitude, location.longitude)
                            } else {
                                binding.mapView.visibility = View.GONE
                            }
                        }
                    } ?: run {
                        runOnUiThread {
                            hideLoadingState()
                            binding.btnLocation.text = "Location not found"
                            binding.mapView.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        hideLoadingState()
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Error getting address: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.mapView.visibility = View.GONE
                    }
                }
            }
            fusedLocationClient.removeLocationUpdates(this)
        }
    }

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

        authRepository = AuthRepository(applicationContext)
        learnerRepository = LearnerRepository(applicationContext)
        tutorRepository = TutorRepository(applicationContext)

        setupWebView()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000)
            .setMaxUpdateDelayMillis(10000)
            .build()

        binding.btnLocation.setOnClickListener {
            checkLocationPermission()
        }

        // Back to ProfileFragment in MainActivity
        binding.btnBack.setOnClickListener {
            backToProfile()
        }

        binding.btnSave.setOnClickListener {
            updateProfile()
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

        // Fill profile data
        fillProfileData()
    }

    private fun showLoadingState() {
        binding.btnLocation.apply {
            isEnabled = false
            icon = CircularProgressDrawable(this@EditProfileActivity).apply {
                setStyle(CircularProgressDrawable.DEFAULT)
                setColorSchemeColors(
                    ContextCompat.getColor(
                        this@EditProfileActivity,
                        R.color.darkgreen
                    )
                )
                start()
            }
            text = "Getting location..."
        }
    }

    private fun hideLoadingState() {
        binding.btnLocation.apply {
            isEnabled = true
            icon = ContextCompat.getDrawable(this@EditProfileActivity, R.drawable.ic_location)
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                showLoadingState()
                getCurrentLocation()
            }

            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun updateSharedPreferences(
        name: String?,
        email: String?
    ) {
        val sharedPreferences = authRepository.getSharedPreferences()
        val editor = sharedPreferences.edit()

        if (name != null) {
            editor.putString("user_name", name)
        }
        if (email != null) {
            editor.putString("user_email", email)
        }

        editor.apply()
    }


    private fun fillProfileData() {
        val sharedPreferences = authRepository.getSharedPreferences()
        val role = sharedPreferences.getString("user_role", null)

        Log.d("EditProfileActivity", "Setting user role to $role")
        lifecycleScope.launch {
            val profileData: ProfileData? = if (role == "learner") {
                learnerRepository.fetchLearnerProfile()?.data
            } else {
                tutorRepository.fetchTutorProfile()?.data
            }

            profileData?.let { data ->
                val (name, gender, email, phoneNumber) = listOf(
                    data.name, data.gender, data.email, data.phoneNumber
                )

                // Set city and district from existing profile data
                locationData.city = data.city
                locationData.district = data.district

                // Remove +62
                phoneNumber?.removePrefix("+62")
                binding.edtName.setText(name)
                binding.edtEmail.setText(email)
                binding.edtPhone.setText(phoneNumber)

                // Set gender
                val options = resources.getStringArray(R.array.gender_options)
                val selectedOption = options.find { it.equals(gender, ignoreCase = true) }
                binding.spinnerGender.setText(selectedOption, false)

                // Update location button text and map if location exists
                if (!locationData.city.isNullOrEmpty() || !locationData.district.isNullOrEmpty()) {
                    val locationText =
                        listOfNotNull(locationData.city, locationData.district).joinToString(", ")
                    binding.btnLocation.text = locationText

                    // Get approximate coordinates for the city/district and show map
                    getCoordinatesForLocation("${locationData.city} ${locationData.district}")
                } else {
                    binding.mapView.visibility = View.GONE
                }
            } ?: Log.e("EditProfileActivity", "Profile data is null")
        }
    }

    private fun getCoordinatesForLocation(address: String) {
        try {
            val geocoder = Geocoder(this)
            val locations = geocoder.getFromLocationName(address, 1)

            locations?.firstOrNull()?.let { location ->
                loadMap(location.latitude, location.longitude)
            } ?: run {
                binding.mapView.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e("EditProfileActivity", "Error getting coordinates: ${e.message}")
            binding.mapView.visibility = View.GONE
        }
    }


    private fun updateLearnerProfile(newData: UpdateLearnerProfileRequest) {
        lifecycleScope.launch {
            try {
                learnerRepository.updateLearnerProfile(newData)
                updateSharedPreferences(newData.name, newData.email)
                // Publish event after successful update
                EventBus.publish(ProfileUpdateEvent(newData.name, newData.email))
            } catch (e: Exception) {
                Log.e("EditProfileActivity", "Failed to update learner profile", e)
            }
        }
    }


    private fun updateTutorProfile(newData: UpdateTutorProfileRequest) {
        lifecycleScope.launch {
            try {
                tutorRepository.updateTutorProfile(newData)
                updateSharedPreferences(newData.name, newData.email)
                // Publish event after successful update
                EventBus.publish(ProfileUpdateEvent(newData.name, newData.email))
            } catch (e: Exception) {
                Log.e("EditProfileActivity", "Failed to update tutor profile", e)
            }
        }
    }

    private fun updateProfile() {
        val sharedPreferences = authRepository.getSharedPreferences()
        val role = sharedPreferences.getString("user_role", null)

        val name = binding.edtName.text.toString()
        val oldEmail = sharedPreferences.getString("user_email", null)
        val newEmail = binding.edtEmail.text.toString()
        val email = if (oldEmail != newEmail) newEmail else null
        val phoneNumber = binding.edtPhone.text.toString().takeIf { it.isNotEmpty() }
        val gender = binding.spinnerGender.text.toString()
            .replaceFirstChar { it.lowercase() }

        if (role == "learner") {
            updateLearnerProfile(
                UpdateLearnerProfileRequest(
                    name = name,
                    email = email,
                    phoneNumber = phoneNumber,
                    gender = gender,
                    city = locationData.city,
                    district = locationData.district,
                    learningStyle = null,
                    interests = null,
                )
            )
        } else {
            updateTutorProfile(
                UpdateTutorProfileRequest(
                    name = name,
                    email = email,
                    phoneNumber = phoneNumber,
                    gender = gender,
                    city = locationData.city,
                    district = locationData.district
                )
            )
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
        } catch (_: SecurityException) {
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
        hideLoadingState()
    }

    private fun backToProfile() {
        val intent = Intent(this, MainActivity::class.java).apply {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("startFragment", "profile")
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.loadUrl("about:blank")
    }
}