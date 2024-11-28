package com.tutortoise.tutortoise.presentation.main.tutor.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.databinding.FragmentTutorProfileBinding
import com.tutortoise.tutortoise.presentation.auth.login.LoginActivity
import com.tutortoise.tutortoise.presentation.commonProfile.ChangePasswordActivity
import com.tutortoise.tutortoise.presentation.commonProfile.EditProfileActivity
import com.tutortoise.tutortoise.presentation.commonProfile.MyActivityActivity
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.EventBus
import com.tutortoise.tutortoise.utils.ProfileUpdateEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileTutorFragment : Fragment() {
    private var _binding: FragmentTutorProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var authRepository: AuthRepository
    private var profileUpdateListener: ((Any) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create listener
        profileUpdateListener = { event ->
            if (event is ProfileUpdateEvent) {
                lifecycleScope.launch {
                    displayUserInfo()
                }
            }
        }

        // Subscribe to events
        EventBus.subscribe(ProfileUpdateEvent::class.java, profileUpdateListener!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorProfileBinding.inflate(inflater, container, false)
        authRepository = AuthRepository(requireContext())

        // Display user details
        lifecycleScope.launch {
            displayUserInfo()
        }

        setupClickListeners()

        return binding.root
    }

    private suspend fun displayUserInfo() {
        try {
            withContext(Dispatchers.Main) {
                val sharedPreferences = authRepository.getSharedPreferences()
                val id = authRepository.getUserId()
                val name = sharedPreferences.getString("user_name", null)
                val email = sharedPreferences.getString("user_email", null)
                binding.tvName.text = name
                binding.tvEmail.text = email
                Glide.with(this@ProfileTutorFragment)
                    .load(Constants.getProfilePictureUrl(id!!))
                    .into(binding.ivProfile)
            }
        } catch (e: Exception) {
            Log.e("UserDetails", "Failed to fetch user details", e)
            Toast.makeText(
                requireContext(),
                "Failed to fetch user details: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupClickListeners() {
        binding.layoutEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.layoutChangePassword.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }

        binding.layoutMyActivity.setOnClickListener {
            startActivity(Intent(requireContext(), MyActivityActivity::class.java))
        }

        binding.layoutLogOut.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                logoutUser()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logoutUser() {
        authRepository.clearToken()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        profileUpdateListener?.let { listener ->
            EventBus.unsubscribe(ProfileUpdateEvent::class.java, listener)
        }
        _binding = null
    }
}