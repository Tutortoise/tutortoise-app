package com.tutortoise.tutortoise.presentation.menuLearner.profile

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
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.databinding.FragmentLearnerProfileBinding
import com.tutortoise.tutortoise.presentation.login.LoginActivity
import com.tutortoise.tutortoise.presentation.menuLearner.profile.general.ChangePasswordActivity
import com.tutortoise.tutortoise.presentation.menuLearner.profile.general.EditProfileActivity
import com.tutortoise.tutortoise.presentation.menuLearner.profile.general.MyActivityActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentLearnerProfileBinding? = null
    private val binding get() = _binding!!
    // private lateinit var auth: FirebaseAuth
    private lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnerProfileBinding.inflate(inflater, container, false)
        authRepository = AuthRepository(requireContext())

        // Display user details
        displayUserInfo()

        // Handle logout
        setupClickListeners()

        return binding.root
    }

    private fun displayUserInfo() {
        lifecycleScope.launch {
            try {
                // Get the token from EncryptedSharedPreferences
                val token = AuthRepository(requireContext()).getToken()
                if (token == null) {
                    Log.d("UserDetails", "Token is null!")
                    Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val sharedPreferences = authRepository.getSharedPreferences()
                val role = sharedPreferences.getString("user_role", null)
                if (role == null) {
                    Log.e("UserDetails", "Role is null!")
                }

                val name = sharedPreferences.getString("user_name", null)
                val email = sharedPreferences.getString("user_email", null)
                binding.tvName.text = name
                binding.tvEmail.text = email

            } catch (e: Exception) {
                Log.e("UserDetails", "Failed to fetch user details", e)
                Toast.makeText(requireContext(), "Failed to fetch user details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        // Edit Profile
        binding.layoutEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        // Change Password
        binding.layoutChangePassword.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }

        // My Activity
        binding.layoutMyActivity.setOnClickListener {
            startActivity(Intent(requireContext(), MyActivityActivity::class.java))
        }

        // Logout
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
//                auth.signOut()
//                val intent = Intent(requireContext(), LoginActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logoutUser() {
        // Clear the authentication token
        authRepository.clearToken()

        // Redirect to LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}