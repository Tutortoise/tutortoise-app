package com.tutortoise.tutortoise.presentation.main.tutor.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.data.repository.OrderRepository
import com.tutortoise.tutortoise.databinding.FragmentTutorHomeBinding
import com.tutortoise.tutortoise.domain.AuthManager
import com.tutortoise.tutortoise.presentation.chat.ChatListActivity
import com.tutortoise.tutortoise.presentation.chat.ChatRoomActivity
import com.tutortoise.tutortoise.presentation.main.tutor.home.adapter.HomeScheduledSessionsAdapter
import com.tutortoise.tutortoise.presentation.notification.NotificationActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeTutorFragment : Fragment() {
    private var _binding: FragmentTutorHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var authRepository: AuthRepository


    private val viewModel: HomeTutorViewModel by viewModels {
        HomeTutorViewModel.provideFactory(OrderRepository(requireContext()))
    }

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

        binding.rvScheduledSessions.apply {
            layoutManager = LinearLayoutManager(context).apply {
                isMeasurementCacheEnabled = false
            }
            adapter = HomeScheduledSessionsAdapter(
                emptyList()
            ) { learnerId ->
                val intent = Intent(requireContext(), ChatRoomActivity::class.java).apply {
                    putExtra("LEARNER_ID", learnerId)
                    putExtra("TUTOR_ID", AuthManager.getCurrentUserId())
                    putExtra("TUTOR_NAME", AuthManager.getInstance()?.getUserName())
                }
                startActivity(intent)
            }
        }

        // Set up click listeners
        binding.notification.setOnClickListener {
            startActivity(notificationIntent)
        }

        binding.chat.setOnClickListener {
            startActivity(chatIntent)
        }

        // Display user name
        displayUserName()


        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.scheduledOrders.collectLatest { result ->
                when {
                    result.isSuccess -> {
                        val items = result.getOrNull()
                        if (items.isNullOrEmpty()) {
                            binding.rvScheduledSessions.visibility = View.GONE
                            binding.noScheduleLayout.visibility = View.VISIBLE
                        } else {
                            binding.rvScheduledSessions.visibility = View.VISIBLE
                            binding.noScheduleLayout.visibility = View.GONE
                            (binding.rvScheduledSessions.adapter as HomeScheduledSessionsAdapter)
                                .updateSessions(items)
                            binding.rvScheduledSessions.requestLayout()
                        }
                    }

                    result.isFailure -> {
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch scheduled sessions",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }


        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            viewModel.fetchScheduledOrders()
        }

    }

    private fun displayUserName() {
        val sharedPreferences = authRepository.getSharedPreferences()
        val userName = sharedPreferences.getString("user_name", "Tutor")
        binding.greeting.text = "Hello, $userName!"
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchScheduledOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}