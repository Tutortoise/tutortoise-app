package com.tutortoise.tutortoise.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.tutortoise.tutortoise.MainActivity
import com.tutortoise.tutortoise.R

class LoginRegisterFragment : Fragment(R.layout.fragment_login_register) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_login_register, container, false)

        val loginButton: MaterialButton = binding.findViewById(R.id.continueButton)
        val registerButton: MaterialButton = binding.findViewById(R.id.registButton)

        loginButton.setOnClickListener {
            navigateToMainActivity()
        }

        registerButton.setOnClickListener {
            navigateToMainActivity()
        }

        return binding
    }

    private fun navigateToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}