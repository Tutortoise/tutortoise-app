package com.tutortoise.tutortoise.presentation.menuLearner.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.databinding.FragmentLearnerProfileBinding
import com.tutortoise.tutortoise.presentation.login.LoginActivity
import com.tutortoise.tutortoise.presentation.menuLearner.profile.general.ChangePasswordActivity
import com.tutortoise.tutortoise.presentation.menuLearner.profile.general.EditProfileActivity
import com.tutortoise.tutortoise.presentation.menuLearner.profile.general.MyActivityActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentLearnerProfileBinding? = null
    private val binding get() = _binding!!
//    private lateinit var auth: FirebaseAuth
    private lateinit var AuthRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnerProfileBinding.inflate(inflater, container, false)
        AuthRepository = AuthRepository(requireContext())

        // Display user details
        displayUserInfo()

        // Handle logout
        setupClickListeners()

        return binding.root
    }

    private fun displayUserInfo() {
        val sharedPreferences = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "Anonymous")
        val userEmail = sharedPreferences.getString("user_email", "No email available")

        binding.tvName.text = userName
        binding.tvEmail.text = userEmail
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
        AuthRepository.clearToken()

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