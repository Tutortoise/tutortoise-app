package com.tutortoise.tutortoise.presentation.main.tutor.sessions

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
import com.tutortoise.tutortoise.databinding.FragmentTutorPendingSessionBinding
import com.tutortoise.tutortoise.presentation.main.tutor.sessions.adapter.PendingOrdersAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PendingTutorSessionFragment : Fragment() {
    private var _binding: FragmentTutorPendingSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PendingTutorSessionViewModel by viewModels {
        PendingTutorSessionViewModel.provideFactory(OrderRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorPendingSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.noSessionsView.tvNoSessionTitle.text = "No Pending Session"
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ordersState.collectLatest { result ->
                when {
                    result.isSuccess -> {
                        val orders = result.getOrNull()
                        if (orders.isNullOrEmpty()) {
                            binding.rvOrders.visibility = View.GONE
                            binding.noSessionsView.root.visibility = View.VISIBLE
                            binding.noSessionsView.tvNoSessionTitle.text = "No Pending Session"
                        } else {
                            binding.rvOrders.visibility = View.VISIBLE
                            binding.noSessionsView.root.visibility = View.GONE
                            binding.rvOrders.layoutManager = LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                            binding.rvOrders.adapter = PendingOrdersAdapter(
                                orders,
                                onAcceptClick = { orderId ->
                                    viewModel.acceptOrder(orderId, "pending")
                                },
                                onRejectClick = { orderId ->
                                    viewModel.rejectOrder(orderId, "pending")
                                }
                            )
                        }
                    }

                    result.isFailure -> {
                        val error =
                            result.exceptionOrNull()?.message ?: "Failed to get pending sessions"
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
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
}