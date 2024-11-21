package com.tutortoise.tutortoise.presentation.menuUser.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.FragmentLearnerHomeBinding
import com.tutortoise.tutortoise.presentation.chat.ChatListActivity
import com.tutortoise.tutortoise.presentation.notification.NotificationActivity
import com.tutortoise.tutortoise.presentation.subjects.SubjectsActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentLearnerHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLearnerHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.notification.setOnClickListener {
            val intent = Intent(requireContext(), NotificationActivity::class.java)
            startActivity(intent)
        }

        binding.chat.setOnClickListener {
            val intent = Intent(requireContext(), ChatListActivity::class.java)
            startActivity(intent)
        }

        binding.seemore.setOnClickListener {
            val intent = Intent(requireContext(), SubjectsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}