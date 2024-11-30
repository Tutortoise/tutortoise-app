package com.tutortoise.tutortoise.presentation.commonProfile.editProfile

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.ProfileData
import com.tutortoise.tutortoise.data.model.UpdateLearnerProfileRequest
import com.tutortoise.tutortoise.data.model.UpdateTutorProfileRequest
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.data.repository.LearnerRepository
import com.tutortoise.tutortoise.data.repository.TutorRepository
import com.tutortoise.tutortoise.databinding.ActivityEditProfileBinding
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.EventBus
import com.tutortoise.tutortoise.utils.ProfileUpdateEvent
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import java.util.Locale

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var learnerRepository: LearnerRepository
    private lateinit var tutorRepository: TutorRepository

    private lateinit var locationManager: LocationManager
    private lateinit var locationUI: LocationUI
    private lateinit var mapUI: MapUI
    private val locationData = LocationData()

    private lateinit var imagePicker: ProfileImagePicker

    private data class ProfileUpdateData(
        val name: String,
        val email: String?,
        val phoneNumber: String?,
        val gender: String,
        val city: String?,
        val district: String?
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRepositories()
        setupLocationComponents()
        setupImagePicker()
        setupUI()
        loadInitialData()
    }

    private fun setupRepositories() {
        authRepository = AuthRepository(applicationContext)
        learnerRepository = LearnerRepository(applicationContext)
        tutorRepository = TutorRepository(applicationContext)
    }

    private fun setupLocationComponents() {
        locationUI = LocationUI(this, binding)
        mapUI = MapUI(binding.mapView)
        locationManager = LocationManager(
            activity = this,
            locationUI = locationUI,
            mapUI = mapUI,
            coroutineScope = lifecycleScope,
            locationData = locationData
        )
    }

    private fun setupImagePicker() {
        imagePicker = ProfileImagePicker(
            activity = this,
            onImageSelected = { uri -> handleImageSelection(uri) }
        )
    }

    private fun handleImageSelection(uri: Uri) {
        val requestBody = uriToRequestBody(uri)
        val imagePart = MultipartBody.Part.createFormData(
            "picture",
            uri.lastPathSegment,
            requestBody
        )
        updateProfilePicture(imagePart)
    }

    private fun setupUI() {
        setupGenderSpinner()
        setupClickListeners()
    }

    private fun setupGenderSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            R.layout.item_dropdown
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.item_dropdown)
            binding.spinnerGender.setAdapter(adapter)
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnBack.setOnClickListener { navigateBackToProfile() }
            btnSave.setOnClickListener { handleSave() }
            btnLocation.setOnClickListener { handleLocationRequest() }
            btnChangePhoto.setOnClickListener { imagePicker.launch() }
        }
    }

    private fun loadInitialData() {
        val userRole = authRepository.getUserRole()
        lifecycleScope.launch {
            fetchProfileData(userRole)?.let { updateUIWithProfileData(it) }
        }
    }

    private fun handleLocationRequest() {
        mapUI.initialize()
        if (hasLocationPermission()) {
            locationManager.startLocationUpdates()
        } else {
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                locationManager.startLocationUpdates()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                locationManager.startLocationUpdates()
            }

            else -> {
                locationUI.showError("Location permission is required")
            }
        }
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun handleSave() {
        if (!validateForm()) return

        val profileUpdateData = createProfileUpdateData()
        lifecycleScope.launch {
            try {
                updateProfile(profileUpdateData)
            } catch (e: Exception) {
                handleUpdateError(e)
            }
        }
    }

    private fun validateForm(): Boolean {
        if (binding.edtName.text.toString().isEmpty()) {
            locationUI.showError("Name cannot be empty")
            return false
        }

        val gender = binding.spinnerGender.text.toString().lowercase()
        if (gender !in listOf("male", "female", "prefer not to say")) {
            locationUI.showError("Please select a valid gender")
            return false
        }

        return true
    }

    private suspend fun updateProfile(data: ProfileUpdateData) {
        val userRole = authRepository.getUserRole()
        when (userRole) {
            "learner" -> updateLearnerProfile(data)
            else -> updateTutorProfile(data)
        }?.let {
            handleSuccessfulUpdate(data.name, data.email)
            navigateBackToProfile()
        } ?: run {
            locationUI.showError("Failed to update profile")
        }
    }

    private fun handleUpdateError(error: Exception) {
        locationUI.showError("Error: ${error.message}")
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

    private suspend fun updateLearnerProfile(data: ProfileUpdateData) =
        learnerRepository.updateLearnerProfile(
            UpdateLearnerProfileRequest(
                name = data.name,
                email = data.email,
                phoneNumber = data.phoneNumber,
                gender = data.gender,
                city = data.city,
                district = data.district,
                learningStyle = null,
                interests = null
            )
        )

    private suspend fun updateTutorProfile(data: ProfileUpdateData) =
        tutorRepository.updateTutorProfile(
            UpdateTutorProfileRequest(
                name = data.name,
                email = data.email,
                phoneNumber = data.phoneNumber,
                gender = data.gender,
                city = data.city,
                district = data.district
            )
        )

    private suspend fun fetchProfileData(role: String?) =
        when (role) {
            "learner" -> learnerRepository.fetchLearnerProfile()?.data
            else -> tutorRepository.fetchTutorProfile()?.data
        }

    private fun updateUIWithProfileData(profileData: ProfileData) {
        updateBasicInfo(profileData)
        updateLocationInfo(profileData)
    }

    private fun updateBasicInfo(profileData: ProfileData) {
        binding.apply {
            edtName.setText(profileData.name)
            edtEmail.setText(profileData.email)
            edtPhone.setText(profileData.phoneNumber?.removePrefix("+62"))

            val options = resources.getStringArray(R.array.gender_options)
            val selectedOption = options.find { it.equals(profileData.gender, ignoreCase = true) }
            spinnerGender.setText(selectedOption, false)
        }
        loadProfileImage(profileData.id)
    }

    private fun updateLocationInfo(profileData: ProfileData) {
        locationData.apply {
            city = cleanLocationName(profileData.city)
            district = cleanLocationName(profileData.district)
        }

        if (locationData.hasValidLocation()) {
            binding.btnLocation.text = formatLocationText()
            // Initialize map and show current location only if we have valid location data
            mapUI.initialize()
            getCoordinatesForLocation("${locationData.city}, ${locationData.district}, Indonesia")
        } else {
            binding.btnLocation.text = "Use current location"
            binding.mapView.visibility = View.GONE
        }
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

        // Create regex pattern for all prefixes
        val pattern = prefixes.joinToString("|") { "^$it\\s+" }.toRegex(RegexOption.IGNORE_CASE)

        // Remove prefix and trim
        return name.replace(pattern, "").trim()
    }

    private fun getCoordinatesForLocation(address: String) {
        try {
            val geocoder = Geocoder(this, Locale.ENGLISH)
            val locations = geocoder.getFromLocationName(address, 1)

            locations?.firstOrNull()?.let { location ->
                // Update map with the location
                mapUI.updateLocation(
                    location.latitude,
                    location.longitude,
                    formatLocationText()
                )
            } ?: run {
                mapUI.hide()
                locationUI.showError("Could not find location coordinates")
            }
        } catch (e: Exception) {
            Log.e("EditProfileActivity", "Error getting coordinates: ${e.message}")
            mapUI.hide()
            locationUI.showError("Error finding location")
        }
    }

    private fun formatLocationText(): String {
        return listOfNotNull(locationData.district, locationData.city)
            .joinToString(", ")
            .takeIf { it.isNotEmpty() } ?: "Your location"
    }

    private fun loadProfileImage(userId: String?) {
        Glide.with(this)
            .load(Constants.getProfilePictureUrl(userId.toString()))
            .placeholder(R.drawable.default_profile_picture)
            .error(R.drawable.default_profile_picture)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.profileImage)
    }

    private fun updateProfilePicture(picture: MultipartBody.Part) {
        lifecycleScope.launch {
            try {
                val result = when (authRepository.getUserRole()) {
                    "learner" -> learnerRepository.updateProfilePicture(picture)
                    else -> tutorRepository.updateProfilePicture(picture)
                }

                result.fold(
                    onSuccess = {
                        locationUI.showError(it.message)
                        loadProfileImage(authRepository.getUserId())
                    },
                    onFailure = { locationUI.showError(it.message.toString()) }
                )
            } catch (e: Exception) {
                locationUI.showError(e.message ?: "Failed to update profile picture")
            }
        }
    }

    private fun uriToRequestBody(uri: Uri): RequestBody {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val byteArray = inputStream?.readBytes() ?: byteArrayOf()
        return byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
    }

    private fun handleSuccessfulUpdate(name: String, email: String?) {
        authRepository.getSharedPreferences().edit().apply {
            putString("user_name", name)
            email?.let { putString("user_email", it) }
            apply()
        }
        EventBus.publish(ProfileUpdateEvent(name, email))
    }

    private fun navigateBackToProfile() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onPause() {
        super.onPause()
        locationManager.stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.apply {
            clearHistory()
            clearCache(true)
            loadUrl("about:blank")
        }
        lifecycleScope.coroutineContext.cancelChildren()
    }
}