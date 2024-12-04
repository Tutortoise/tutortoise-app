package com.tutortoise.tutortoise.presentation.main.tutor.profile

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.databinding.FragmentTutorProfileBinding
import com.tutortoise.tutortoise.domain.AuthManager
import com.tutortoise.tutortoise.domain.EventBus
import com.tutortoise.tutortoise.domain.ProfileUpdateEvent
import com.tutortoise.tutortoise.presentation.auth.login.LoginActivity
import com.tutortoise.tutortoise.presentation.commonProfile.ChangePasswordActivity
import com.tutortoise.tutortoise.presentation.commonProfile.MyActivityActivity
import com.tutortoise.tutortoise.presentation.commonProfile.editProfile.EditProfileActivity
import com.tutortoise.tutortoise.presentation.main.tutor.tutories.SetScheduleActivity
import com.tutortoise.tutortoise.utils.Constants
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

                binding.tvName.text = name ?: "No name"
                binding.tvEmail.text = email ?: "No email"

                id?.let { userId ->
                    Glide.with(this@ProfileTutorFragment)
                        .load(Constants.getProfilePictureUrl(userId))
                        .placeholder(R.drawable.default_profile_picture) // Add a default placeholder image
                        .error(R.drawable.default_profile_picture) // Add an error placeholder image
                        .into(binding.ivProfile)
                } ?: run {
                    binding.ivProfile.setImageResource(R.drawable.default_profile_picture)
                    Log.w("ProfileTutorFragment", "User ID is null, using default profile picture")
                }
            }
        } catch (e: Exception) {
            Log.e("UserDetails", "Failed to fetch user details", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "Failed to load profile: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

                binding.tvName.text = "Unknown User"
                binding.tvEmail.text = "No email available"
                binding.ivProfile.setImageResource(R.drawable.ic_nav_profile)
            }
        }
    }

    private fun setupClickListeners() {
        binding.layoutEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.layoutChangePassword.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }

        binding.layoutMySchedule.setOnClickListener {
            startActivity(Intent(requireContext(), SetScheduleActivity::class.java))
        }

        binding.layoutMyActivity.setOnClickListener {
            startActivity(Intent(requireContext(), MyActivityActivity::class.java))
        }

        binding.layoutLogOut.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_dialog_logout, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        // Make dialog rounded corners
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)

        // Set up click listeners for the buttons
        dialogView.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            // Show loading state
            val loadingView = dialogView.findViewById<View>(R.id.loadingView)
            val buttonsLayout = dialogView.findViewById<View>(R.id.buttonsLayout)

            loadingView.visibility = View.VISIBLE
            buttonsLayout.visibility = View.GONE

            lifecycleScope.launch {
                try {
                    AuthManager.logout(requireContext())

                    val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }

                    val options = ActivityOptions.makeCustomAnimation(
                        requireContext(),
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )

                    startActivity(intent, options.toBundle())
                    dialog.dismiss()

                } catch (e: Exception) {
                    // Handle error
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Failed to logout. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadingView.visibility = View.GONE
                        buttonsLayout.visibility = View.VISIBLE
                    }
                }
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        profileUpdateListener?.let { listener ->
            EventBus.unsubscribe(ProfileUpdateEvent::class.java, listener)
        }
        _binding = null
    }
}