package com.tutortoise.tutortoise.presentation.main.learner.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tutortoise.tutortoise.data.repository.SubjectRepository
import com.tutortoise.tutortoise.databinding.FragmentLearnerHomeBinding
import com.tutortoise.tutortoise.presentation.chat.ChatListActivity
import com.tutortoise.tutortoise.presentation.notification.NotificationActivity
import com.tutortoise.tutortoise.presentation.subjects.SubjectsActivity
import com.tutortoise.tutortoise.presentation.subjects.adapter.SubjectsAdapter
import kotlinx.coroutines.launch

class HomeLearnerFragment : Fragment() {
    private var _binding: FragmentLearnerHomeBinding? = null
    private val binding get() = _binding!!

    private val notificationIntent by lazy {
        Intent(
            requireContext(),
            NotificationActivity::class.java
        )
    }
    private val chatIntent by lazy { Intent(requireContext(), ChatListActivity::class.java) }
    private val subjectsIntent by lazy { Intent(requireContext(), SubjectsActivity::class.java) }

    private lateinit var subjectRepository: SubjectRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnerHomeBinding.inflate(inflater, container, false)

        subjectRepository = SubjectRepository(requireContext())

        // Fetch subjects
        fetchSubjects()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.notification.setOnClickListener {
            startActivity(notificationIntent)
        }

        binding.chat.setOnClickListener {
            startActivity(chatIntent)
        }

        binding.seemore.setOnClickListener {
            startActivity(subjectsIntent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchSubjects() {
        val recyclerView: RecyclerView = binding.rvHomeSubjects
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        lifecycleScope.launch {
            val subjects = subjectRepository.fetchPopularSubjects()
            recyclerView.adapter = subjects?.data?.let { SubjectsAdapter(it) }
        }
    }

}