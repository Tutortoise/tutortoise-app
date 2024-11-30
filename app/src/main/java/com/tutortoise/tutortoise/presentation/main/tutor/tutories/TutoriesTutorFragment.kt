package com.tutortoise.tutortoise.presentation.main.tutor.tutories

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.repository.TutoriesRepository
import com.tutortoise.tutortoise.databinding.FragmentTutorTutoriesBinding
import com.tutortoise.tutortoise.presentation.main.tutor.tutories.adapter.TutoriesAdapter
import com.tutortoise.tutortoise.presentation.main.tutor.tutories.editTutories.EditTutoriesActivity
import kotlinx.coroutines.launch

class TutoriesTutorFragment : Fragment() {
    private var _binding: FragmentTutorTutoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var tutoriesAdapter: TutoriesAdapter
    private lateinit var tutoriesRepository: TutoriesRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorTutoriesBinding.inflate(inflater, container, false)
        tutoriesRepository = TutoriesRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tutoriesAdapter = TutoriesAdapter { tutories ->
            try {
                val intent = Intent(requireContext(), EditTutoriesActivity::class.java).apply {
                    putExtra(EditTutoriesActivity.EXTRA_TUTORIES_ID, tutories.id)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("TutoriesTutorFragment", "Error starting EditTutoriesActivity", e)
                Toast.makeText(
                    requireContext(),
                    "Failed to open tutories: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.recyclerViewTutories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tutoriesAdapter
        }

        binding.btnAddTutory.setOnClickListener {
            findNavController().navigate(R.id.action_tutories_to_createTutories)
        }

        fetchTutories()
    }

    private fun fetchTutories() {
        lifecycleScope.launch {
            try {
                val response = tutoriesRepository.getMyTutories()
                if (response?.status == "success") {
                    val tutories = response.data ?: emptyList()
                    tutoriesAdapter.submitList(tutories)
                } else {
                    Toast.makeText(requireContext(), "Failed to load tutories.", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}