package com.tutortoise.tutortoise.presentation.questioner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.repository.LearnerRepository
import com.tutortoise.tutortoise.databinding.FragmentQuestionerStyleBinding
import kotlinx.coroutines.launch

class QuestionerStyleFragment : Fragment(R.layout.fragment_questioner_style) {
    private var _binding: FragmentQuestionerStyleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuestionerStyleViewModel by viewModels {
        QuestionerStyleViewModel.provideFactory(LearnerRepository(requireContext()))
    }
    private var selectedStyle: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionerStyleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnVisual.setOnClickListener { selectStyle("visual") }
        binding.btnAuditory.setOnClickListener { selectStyle("auditory") }
        binding.btnKinesthetic.setOnClickListener { selectStyle("kinesthetic") }

        binding.continueButton.isEnabled = false
        binding.continueButton.setOnClickListener {
            selectedStyle?.let { style ->
                lifecycleScope.launch {
                    viewModel.updateLearningStyle(style).collect { result ->
                        result.fold(
                            onSuccess = {
                                (activity as? QuestionnaireActivity)?.navigateToNext()
                            },
                            onFailure = {
                                Toast.makeText(
                                    context,
                                    "Failed to update learning style",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun selectStyle(style: String) {
        selectedStyle = style
        binding.apply {
            btnVisual.isChecked = style == "visual"
            btnAuditory.isChecked = style == "auditory"
            btnKinesthetic.isChecked = style == "kinesthetic"

            btnVisual.strokeColor = if (style == "visual")
                ContextCompat.getColor(requireContext(), R.color.darkgreen)
            else
                ContextCompat.getColor(requireContext(), R.color.strokeColor)

            btnAuditory.strokeColor = if (style == "auditory")
                ContextCompat.getColor(requireContext(), R.color.darkgreen)
            else
                ContextCompat.getColor(requireContext(), R.color.strokeColor)

            btnKinesthetic.strokeColor = if (style == "kinesthetic")
                ContextCompat.getColor(requireContext(), R.color.darkgreen)
            else
                ContextCompat.getColor(requireContext(), R.color.strokeColor)

            continueButton.isEnabled = true
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    (activity as? QuestionnaireActivity)?.showExitConfirmationDialog()
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}