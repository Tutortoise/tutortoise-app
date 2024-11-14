package com.tutortoise.tutortoise.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.FragmentOnboarding3Binding

class OnboardingFragment3 : Fragment() {

    private var _binding: FragmentOnboarding3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboarding3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.skipButton.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment3_to_loginRegisterFragment)
        }
        binding.continueButton.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment3_to_onboardingFragment4)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}