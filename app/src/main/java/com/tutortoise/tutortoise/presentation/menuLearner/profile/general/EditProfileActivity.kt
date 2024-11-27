package com.tutortoise.tutortoise.presentation.menuLearner.profile.general

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
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.data.repository.LearnerRepository
import com.tutortoise.tutortoise.data.repository.TutorRepository
import com.tutortoise.tutortoise.databinding.ActivityEditProfileBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private lateinit var authRepository: AuthRepository
    private lateinit var learnerRepository: LearnerRepository
    private lateinit var tutorRepository: TutorRepository

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

        binding.mapView.visibility = View.VISIBLE
        binding.mapView.loadUrl("file:///android_asset/map.html")

        binding.mapView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // Initialize map with offset coordinates
                binding.mapView.evaluateJavascript(
                    "initMap($offsetLat, $offsetLng)",
                    null
                )
            }
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

        locationCallback = object : LocationCallback() {
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
                            val locality = address.locality ?: ""
                            val city = address.adminArea ?: ""

                            val locationText = if (locality.isNotEmpty() && city.isNotEmpty()) {
                                "$locality, $city"
                            } else {
                                locality.ifEmpty { city }
                            }

                            runOnUiThread {
                                hideLoadingState()
                                binding.btnLocation.text =
                                    locationText.ifEmpty { "Location not found" }
                                // Load the OpenStreetMap
                                loadMap(location.latitude, location.longitude)
                            }
                        } ?: run {
                            runOnUiThread {
                                hideLoadingState()
                                binding.btnLocation.text = "Location not found"
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
                        }
                    }
                }
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }

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

                // Remove +62
                phoneNumber?.removePrefix("+62")
                binding.edtName.setText(name)
                binding.edtEmail.setText(email)
                binding.edtPhone.setText(phoneNumber)

                // Set gender
                val options =
                    resources.getStringArray(R.array.gender_options)
                val selectedOption = options.find { it.equals(gender, ignoreCase = true) }
                binding.spinnerGender.setText(selectedOption, false) // Set the selected value
            } ?: Log.e("EditProfileActivity", "Profile data is null")
        }
    }

    private fun updateProfile() {
        val sharedPreferences = authRepository.getSharedPreferences()
        val role = sharedPreferences.getString("user_role", null)

        lifecycleScope.launch {
            // Learner
            if (role == "learner") {
                // TODO: update profile for tutor
                val response = learnerRepository.updateLearnerProfile(
                    UpdateLearnerProfileRequest(
                        name = binding.edtName.text.toString(),
                        email = null,
                        phoneNumber = binding.edtPhone.text.toString().takeIf { it.isNotEmpty() },
                        gender = binding.spinnerGender.text.toString()
                            .replaceFirstChar { it.lowercase() },
                        city = null,
                        district = null,
                        learningStyle = null,
                        interests = null
                    )
                )
            } else {
                // TODO: update profile for tutor
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
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
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