package com.tutortoise.tutortoise.presentation.menuLearner.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tutortoise.tutortoise.databinding.FragmentLearnerProfileBinding
import com.tutortoise.tutortoise.presentation.login.LoginActivity
import com.tutortoise.tutortoise.presentation.menuLearner.profile.general.ChangePasswordActivity
import com.tutortoise.tutortoise.presentation.menuLearner.profile.general.EditProfileActivity
import com.tutortoise.tutortoise.presentation.menuLearner.profile.general.MyActivityActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentLearnerProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnerProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        // Display user details
        val user = auth.currentUser
        displayUserInfo(user)

        // Handle logout
        setupClickListeners()

        return binding.root
    }

    private fun displayUserInfo(user: FirebaseUser?) {
        binding.tvName.text = user?.displayName ?: "Anonymous"
        binding.tvEmail.text = user?.email ?: "No email available"
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
                auth.signOut()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}