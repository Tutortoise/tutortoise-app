package com.tutortoise.tutortoise.presentation.commonProfile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebSettings
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var learnerRepository: LearnerRepository
    private lateinit var tutorRepository: TutorRepository
    private lateinit var locationCallback: LocationCallback


    private data class ProfileUpdateData(
        val name: String,
        val email: String?,
        val phoneNumber: String?,
        val gender: String,
        val city: String?,
        val district: String?
    )

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private var webViewInitialized = false
    private val locationData = LocationData()

    private data class LocationData(
        var city: String? = null,
        var district: String? = null
    )

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(3000L)
        .setMaxUpdateDelayMillis(10000L)
        .build()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocation()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getCurrentLocation()
            }

            else -> {
                showLocationPermissionError()
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

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                handleLocationResult(locationResult)
            }
        }

        binding.apply {
            btnBack.setOnClickListener { navigateBackToProfile() }
            btnSave.setOnClickListener {
                val userRole = authRepository.getUserRole()
                val profileUpdateData = createProfileUpdateData()

                when (userRole) {
                    "learner" -> updateLearnerProfile(profileUpdateData)
                    else -> {
                        val request = UpdateTutorProfileRequest(
                            name = profileUpdateData.name,
                            email = profileUpdateData.email,
                            phoneNumber = profileUpdateData.phoneNumber,
                            gender = profileUpdateData.gender,
                            city = profileUpdateData.city,
                            district = profileUpdateData.district
                        )

                        lifecycleScope.launch {
                            try {
                                tutorRepository.updateTutorProfile(request)
                                handleSuccessfulUpdate(
                                    profileUpdateData.name,
                                    profileUpdateData.email
                                )
                            } catch (e: Exception) {
                                Log.e("EditProfileActivity", "Failed to update tutor profile", e)
                            }
                        }
                    }
                }

                navigateBackToProfile()
            }
            btnLocation.setOnClickListener {
                initializeWebViewIfNeeded()
                checkLocationPermission()
            }
        }

        val spinnerGender = findViewById<AutoCompleteTextView>(R.id.spinnerGender)
        ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            R.layout.item_dropdown
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.item_dropdown)
            spinnerGender.setAdapter(adapter)
        }

        val userRole = authRepository.getUserRole()
        Log.d("EditProfileActivity", "Setting user role to $userRole")

        lifecycleScope.launch {
            val profileData = fetchProfileData(userRole)
            profileData?.let { updateUIWithProfileData(it) }
                ?: Log.e("EditProfileActivity", "Profile data is null")
        }
    }

    private fun handleLocationResult(locationResult: LocationResult) {
        locationResult.lastLocation?.let { location ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val address = getAddressFromLocation(location)
                    withContext(Dispatchers.Main) {
                        address?.let { validAddress ->
                            locationData.apply {
                                city = validAddress.locality
                                district = validAddress.adminArea
                            }

                            hideLoadingState()
                            val locationText = formatLocationText()
                            binding.btnLocation.text = locationText

                            if (locationData.hasValidLocation()) {
                                loadMap(validAddress.latitude, validAddress.longitude)
                            } else {
                                binding.mapView.visibility = View.GONE
                            }
                        } ?: {
                            hideLoadingState()
                            binding.btnLocation.text = "Location not found"
                            binding.mapView.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        handleLocationError(e)
                    }
                } finally {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                        .addOnCompleteListener {
                            Log.d("EditProfileActivity", "Location updates removed")
                        }
                }
            }
        }
    }

    private fun loadMap(latitude: Double, longitude: Double) {
        initializeWebViewIfNeeded()

        binding.mapView.apply {
            visibility = View.VISIBLE
            loadUrl("file:///android_asset/map.html")
            webViewClient = createWebViewClient(latitude, longitude)
        }
    }

    private fun createWebViewClient(latitude: Double, longitude: Double) =
        object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val locationText = formatLocationText()
                val offsetLat = (latitude * 100).toInt() / 100.0
                val offsetLng = (longitude * 100).toInt() / 100.0

                view?.evaluateJavascript(
                    "initMap($offsetLat, $offsetLng, '$locationText')",
                    null
                )
            }
        }


    private fun formatLocationText(): String {
        return listOfNotNull(locationData.city, locationData.district)
            .joinToString(", ")
            .takeIf { it.isNotEmpty() } ?: "Your location"
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebViewIfNeeded() {
        if (!webViewInitialized) {
            binding.mapView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                setRenderPriority(WebSettings.RenderPriority.HIGH)
            }
            binding.mapView.visibility = View.GONE
            webViewInitialized = true
        }
    }

    private fun getAddressFromLocation(location: Location): Address? {
        return Geocoder(this).getFromLocation(
            location.latitude,
            location.longitude,
            1
        )?.firstOrNull()
    }

    private fun LocationData.hasValidLocation(): Boolean {
        return !city.isNullOrEmpty() || !district.isNullOrEmpty()
    }

    private fun handleLocationError(error: Exception) {
        hideLoadingState()
        showLocationErrorToast(error)
        binding.mapView.visibility = View.GONE
    }

    private fun showLocationErrorToast(error: Exception) {
        Toast.makeText(
            this,
            "Error getting location: ${error.message}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showLocationPermissionError() {
        Toast.makeText(
            this,
            "Location permission is required to use this feature",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun checkLocationPermission() {
        when {
            hasLocationPermission() -> {
                binding.btnLocation.apply {
                    isEnabled = false
                    icon = createLoadingIndicator()
                    text = "Getting location..."
                }
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

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createLoadingIndicator(): CircularProgressDrawable {
        return CircularProgressDrawable(this).apply {
            setStyle(CircularProgressDrawable.DEFAULT)
            setColorSchemeColors(
                ContextCompat.getColor(
                    this@EditProfileActivity,
                    R.color.darkgreen
                )
            )
            start()
        }
    }

    private fun hideLoadingState() {
        binding.btnLocation.apply {
            isEnabled = true
            icon = ContextCompat.getDrawable(this@EditProfileActivity, R.drawable.ic_location)
        }
    }

    private suspend fun fetchProfileData(role: String?): ProfileData? {
        return when (role) {
            "learner" -> learnerRepository.fetchLearnerProfile()?.data
            else -> tutorRepository.fetchTutorProfile()?.data
        }
    }

    private fun updateUIWithProfileData(profileData: ProfileData) {
        updateBasicProfileInfo(profileData)
        updateLocationInfo(profileData)
    }

    private fun updateBasicProfileInfo(profileData: ProfileData) {
        binding.apply {
            edtName.setText(profileData.name)
            edtEmail.setText(profileData.email)
            edtPhone.setText(profileData.phoneNumber?.removePrefix("+62"))
            updateGenderSpinner(profileData.gender)
        }
    }

    private fun updateGenderSpinner(gender: String?) {
        val options = resources.getStringArray(R.array.gender_options)
        val selectedOption = options.find { it.equals(gender, ignoreCase = true) }
        binding.spinnerGender.setText(selectedOption, false)
    }

    private fun updateLocationInfo(profileData: ProfileData) {
        locationData.apply {
            city = profileData.city
            district = profileData.district
        }

        if (locationData.hasValidLocation()) {
            binding.btnLocation.text = formatLocationText()
            getCoordinatesForLocation("${locationData.city} ${locationData.district}")
        } else {
            binding.mapView.visibility = View.GONE
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

    private fun createProfileUpdateData(): ProfileUpdateData {
        return ProfileUpdateData(
            name = binding.edtName.text.toString(),
            email = getUpdatedEmail(),
            phoneNumber = binding.edtPhone.text.toString().takeIf { it.isNotEmpty() },
            gender = binding.spinnerGender.text.toString().lowercase(),
            city = locationData.city,
            district = locationData.district
        )
    }

    private fun getUpdatedEmail(): String? {
        val oldEmail = authRepository.getSharedPreferences().getString("user_email", null)
        val newEmail = binding.edtEmail.text.toString()
        return if (oldEmail != newEmail) newEmail else null
    }

    private fun updateLearnerProfile(data: ProfileUpdateData) {
        val request = UpdateLearnerProfileRequest(
            name = data.name,
            email = data.email,
            phoneNumber = data.phoneNumber,
            gender = data.gender,
            city = data.city,
            district = data.district,
            learningStyle = null,
            interests = null
        )

        lifecycleScope.launch {
            try {
                learnerRepository.updateLearnerProfile(request)
                handleSuccessfulUpdate(data.name, data.email)
            } catch (e: Exception) {
                Log.e("EditProfileActivity", "Failed to update learner profile", e)
            }
        }
    }

    private fun handleSuccessfulUpdate(name: String, email: String?) {
        authRepository.getSharedPreferences().edit().apply {
            name.let { putString("user_name", it) }
            email?.let { putString("user_email", it) }
            apply()
        }
        EventBus.publish(ProfileUpdateEvent(name, email))
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
            showLocationPermissionError()
        }
    }

    private fun navigateBackToProfile() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("startFragment", "profile")
            startActivity(this)
        }
        finish()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
            .addOnCompleteListener {
                Log.d("EditProfileActivity", "Location updates removed")
            }
        hideLoadingState()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.loadUrl("about:blank")
        binding.mapView.destroy()
    }
}