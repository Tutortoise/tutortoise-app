package com.tutortoise.tutortoise.presentation.main.tutor.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.databinding.FragmentTutorHomeBinding
import com.tutortoise.tutortoise.presentation.chat.ChatListActivity
import com.tutortoise.tutortoise.presentation.notification.NotificationActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeTutorFragment : Fragment() {
    private var _binding: FragmentTutorHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var authRepository: AuthRepository

    private val notificationIntent by lazy {
        Intent(
            requireContext(),
            NotificationActivity::class.java
        )
    }
    private val chatIntent by lazy { Intent(requireContext(), ChatListActivity::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorHomeBinding.inflate(inflater, container, false)
        authRepository = AuthRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up click listeners
        binding.notification.setOnClickListener {
            startActivity(notificationIntent)
        }

        binding.chat.setOnClickListener {
            startActivity(chatIntent)
        }

        // Display user name
        displayUserName()
        displayCurrentDate()

    }

    private fun displayUserName() {
        val sharedPreferences = authRepository.getSharedPreferences()
        val userName = sharedPreferences.getString("user_name", "Tutor")
        binding.greeting.text = "Hello, $userName!"
    }

    private fun displayCurrentDate() {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        binding.dateText.text = currentDate
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}