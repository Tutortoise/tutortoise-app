package com.tutortoise.tutortoise.presentation.main.learner.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tutortoise.tutortoise.data.repository.OrderRepository
import com.tutortoise.tutortoise.databinding.FragmentLearnerPendingSessionBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity
import com.tutortoise.tutortoise.presentation.main.learner.session.adapter.OrdersAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PendingLearnerSessionFragment : Fragment() {
    private var _binding: FragmentLearnerPendingSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PendingLearnerSessionViewModel by viewModels {
        PendingLearnerSessionViewModel.provideFactory(OrderRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnerPendingSessionBinding.inflate(inflater, container, false)

        binding.noBookedSessionsView.root.visibility = View.GONE
        setupClickFindTutor()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ordersState.collectLatest { result ->
                when {
                    result.isSuccess -> {
                        val orders = result.getOrNull()
                        if (orders.isNullOrEmpty()) {
                            binding.rvOrders.visibility = View.GONE
                            binding.noBookedSessionsView.root.visibility = View.VISIBLE
                            binding.noBookedSessionsView.tvNoSessionTitle.text =
                                "No pending Session"
                        } else {
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
                        Toast.makeText(
                            requireContext(),
                            result.exceptionOrNull()?.message ?: "Error loading sessions",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        viewModel.fetchMyOrders("pending")
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchMyOrders("pending")
    }

    private fun setupClickFindTutor() {
        binding.noBookedSessionsView.btnFindTutor.setOnClickListener {
            (activity as? MainActivity)?.navigateToExploreFromSession()
        }
    }

}