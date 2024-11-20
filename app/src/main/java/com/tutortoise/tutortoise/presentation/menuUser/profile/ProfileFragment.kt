package com.tutortoise.tutortoise.presentation.menuUser.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.presentation.login.LoginActivity

class ProfileFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        tvName = view.findViewById(R.id.tvName)
        tvEmail = view.findViewById(R.id.tvEmail)
        val logoutLayout = view.findViewById<View>(R.id.layoutLogOut)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // Display user details
        displayUserInfo(user)

        // Handle logout
        logoutLayout.setOnClickListener {
            showLogoutConfirmation()
        }

        return view
    }

    private fun displayUserInfo(user: FirebaseUser?) {
        if (user != null) {
            tvName.text = user.displayName ?: "Anonymous" // Default name if not set
            tvEmail.text = user.email ?: "No email available"
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
}