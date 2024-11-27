package com.tutortoise.tutortoise.presentation.onboarding.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tutortoise.tutortoise.databinding.FragmentRegisterAsBinding
import com.tutortoise.tutortoise.presentation.auth.login.LoginActivity
import com.tutortoise.tutortoise.presentation.auth.register.LearnerRegisterActivity
import com.tutortoise.tutortoise.presentation.auth.register.TutorRegisterActivity


class RegisterAsFragment : Fragment() {

    private var _binding: FragmentRegisterAsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterAsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Learner registration button click
        binding.btnLearner.setOnClickListener {
            val intent = Intent(requireContext(), LearnerRegisterActivity::class.java)
            startActivity(intent)
        }

        // Tutor registration button click
        binding.btnTutor.setOnClickListener {
            val intent = Intent(requireContext(), TutorRegisterActivity::class.java)
            startActivity(intent)
        }

        // Login text click
        binding.btnLogin.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}