package com.tutortoise.tutortoise.presentation.commonProfile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.ProfileData
import com.tutortoise.tutortoise.data.model.UpdateLearnerProfileRequest
import com.tutortoise.tutortoise.data.model.UpdateTutorProfileRequest
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.data.repository.LearnerRepository
import com.tutortoise.tutortoise.data.repository.TutorRepository
import com.tutortoise.tutortoise.databinding.ActivityEditProfileBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.EventBus
import com.tutortoise.tutortoise.utils.ProfileUpdateEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var getImageLauncher: ActivityResultLauncher<Intent>
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

        initializeImagePickerLauncher()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                handleLocationResult(locationResult)
            }
        }

        binding.apply {
            btnBack.setOnClickListener { navigateBackToProfile() }
            binding.btnSave.setOnClickListener {
                if (!validateForm()) return@setOnClickListener

                val userRole = authRepository.getUserRole()
                val profileUpdateData = createProfileUpdateData()

                lifecycleScope.launch {
                    try {
                        when (userRole) {
                            "learner" -> {
                                learnerRepository.updateLearnerProfile(
                                    UpdateLearnerProfileRequest(
                                        name = profileUpdateData.name,
                                        email = profileUpdateData.email,
                                        phoneNumber = profileUpdateData.phoneNumber,
                                        gender = profileUpdateData.gender,
                                        city = profileUpdateData.city,
                                        district = profileUpdateData.district,
                                        learningStyle = null,
                                        interests = null
                                    )
                                )?.let {
                                    handleSuccessfulUpdate(
                                        profileUpdateData.name,
                                        profileUpdateData.email
                                    )
                                    navigateBackToProfile()
                                } ?: run {
                                    Toast.makeText(
                                        this@EditProfileActivity,
                                        "Failed to update profile",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            else -> {
                                tutorRepository.updateTutorProfile(
                                    UpdateTutorProfileRequest(
                                        name = profileUpdateData.name,
                                        email = profileUpdateData.email,
                                        phoneNumber = profileUpdateData.phoneNumber,
                                        gender = profileUpdateData.gender,
                                        city = profileUpdateData.city,
                                        district = profileUpdateData.district
                                    )
                                )?.let {
                                    handleSuccessfulUpdate(
                                        profileUpdateData.name,
                                        profileUpdateData.email
                                    )
                                    navigateBackToProfile()
                                } ?: run {
                                    Toast.makeText(
                                        this@EditProfileActivity,
                                        "Failed to update profile",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("EditProfileActivity", "Failed to update profile", e)
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            btnLocation.setOnClickListener {
                initializeWebViewIfNeeded()
                checkLocationPermission()
            }

            btnChangePhoto.setOnClickListener {
                handleChangePicture()
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
                                city = validAddress.subAdminArea
                                district = validAddress.locality
                            }

                            hideLoadingState()
                            val locationText = formatLocationText()
                            binding.btnLocation.text = locationText

                            if (locationData.hasValidLocation()) {
                                loadMap(validAddress.latitude, validAddress.longitude)
                                // Enable save button after location is set
                                binding.btnSave.isEnabled = true
                            } else {
                                binding.mapView.visibility = View.GONE
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "Could not determine location. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } ?: run {
                            hideLoadingState()
                            binding.btnLocation.text = "Location not found"
                            binding.mapView.visibility = View.GONE
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

    private fun validateForm(): Boolean {
        if (binding.edtName.text.toString().isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }

        val gender = binding.spinnerGender.text.toString().lowercase()
        if (gender !in listOf("male", "female", "prefer not to say")) {
            Toast.makeText(this, "Please select a valid gender", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
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
        return listOfNotNull(locationData.district, locationData.city)
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

            Glide.with(profileImage.context)
                .load(Constants.getProfilePictureUrl(profileData.id))
                .placeholder(R.drawable.default_profile_picture)
                .error(R.drawable.default_profile_picture)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(profileImage)
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
            binding.btnLocation.text = "Use current location"
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
        val locationValid = locationData.hasValidLocation()
        return ProfileUpdateData(
            name = binding.edtName.text.toString(),
            email = getUpdatedEmail(),
            phoneNumber = binding.edtPhone.text.toString().takeIf { it.isNotEmpty() },
            gender = binding.spinnerGender.text.toString().lowercase(),
            city = if (locationValid) locationData.city else null,
            district = if (locationValid) locationData.district else null
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

    private fun uriToRequestBody(uri: Uri): RequestBody {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val byteArray = inputStream?.readBytes() ?: byteArrayOf()
        return byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
    }

    private fun initializeImagePickerLauncher() {
        getImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val imageUri = result.data?.data
                    imageUri?.let {
                        val requestBody = uriToRequestBody(it)
                        val imagePart = MultipartBody.Part.createFormData(
                            "picture",
                            it.lastPathSegment,
                            requestBody
                        )

                        updateProfilePicture(imagePart)
                    }
                }
            }
    }

    private fun loadProfileImage(userId: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    userId?.let {
                        Glide.with(this@EditProfileActivity)
                            .load(Constants.getProfilePictureUrl(it))
                            .placeholder(R.drawable.default_profile_picture)
                            .error(R.drawable.default_profile_picture)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    Log.w("EditProfileActivity", "Failed to load profile image", e)
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable,
                                    model: Any,
                                    target: Target<Drawable>,
                                    dataSource: com.bumptech.glide.load.DataSource,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    return false
                                }
                            })
                            .into(binding.profileImage)
                    } ?: run {
                        binding.profileImage.setImageResource(R.drawable.default_profile_picture)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.profileImage.setImageResource(R.drawable.default_profile_picture)
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Using default profile picture",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateProfilePicture(picture: MultipartBody.Part) {
        val userRole = authRepository.getUserRole()
        lifecycleScope.launch {
            try {
                val result = when (userRole) {
                    "learner" -> learnerRepository.updateProfilePicture(picture)
                    else -> tutorRepository.updateProfilePicture(picture)
                }

                result.fold(
                    onSuccess = {
                        Toast.makeText(
                            this@EditProfileActivity,
                            it.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        loadProfileImage(authRepository.getUserId())
                    },
                    onFailure = {
                        Toast.makeText(
                            this@EditProfileActivity,
                            it.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Log.e("EditProfileActivity", "Failed to request updating profile picture", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@EditProfileActivity,
                        e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun handleChangePicture() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getImageLauncher.launch(intent)
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
        lifecycleScope.coroutineContext.cancelChildren()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}