package com.tutortoise.tutortoise.presentation.main.learner.session

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tutortoise.tutortoise.data.repository.OrderRepository
import com.tutortoise.tutortoise.databinding.FragmentLearnerScheduleSessionBinding
import com.tutortoise.tutortoise.presentation.main.learner.session.adapter.OrdersAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ScheduledLearnerSessionFragment : Fragment() {
    private var _binding: FragmentLearnerScheduleSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScheduledLearnerSessionViewModel by viewModels {
        ScheduledLearnerSessionViewModel.provideFactory(OrderRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnerScheduleSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ordersState.collectLatest { result ->
                when {
                    result.isSuccess -> {
                        val orders = result.getOrNull()
                        if (orders.isNullOrEmpty()) {
                            // Show the "No booked sessions" view
                            binding.rvOrders.visibility = View.GONE
                            binding.noBookedSessionsView.root.visibility = View.VISIBLE
                            binding.noBookedSessionsView.btnFindTutor.setOnClickListener {

                            }
                        } else {
                            // Show the recycler view
                            binding.rvOrders.visibility = View.VISIBLE
                            binding.noBookedSessionsView.root.visibility = View.GONE
                            binding.rvOrders.layoutManager = LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                            binding.rvOrders.adapter = OrdersAdapter(orders)
                        }
                    }

                    result.isFailure -> {
                        val error =
                            result.exceptionOrNull()?.message ?: "Failed to get scheduled sessions"
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.fetchMyOrders("scheduled")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        viewModel.fetchMyOrders("scheduled")
    }

}