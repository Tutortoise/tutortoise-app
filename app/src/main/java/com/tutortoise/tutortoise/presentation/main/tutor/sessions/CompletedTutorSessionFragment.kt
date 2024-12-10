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
import com.tutortoise.tutortoise.databinding.FragmentTutorCompletedSessionBinding
import com.tutortoise.tutortoise.presentation.main.tutor.sessions.adapter.CompletedOrdersAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CompletedTutorSessionFragment : Fragment() {
    private var _binding: FragmentTutorCompletedSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CompletedTutorSessionViewModel by viewModels {
        CompletedTutorSessionViewModel.provideFactory(
            OrderRepository(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorCompletedSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.noSessionsView.tvNoSessionTitle.text = "No Completed Session"
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ordersState.collectLatest { result ->
                when {
                    result.isSuccess -> {
                        val orders = result.getOrNull()
                        if (orders.isNullOrEmpty()) {
                            binding.rvOrders.visibility = View.GONE
                            binding.noSessionsView.root.visibility = View.VISIBLE
                            binding.noSessionsView.tvNoSessionTitle.text = "No Completed Session"
                        } else {
                            binding.rvOrders.visibility = View.VISIBLE
                            binding.noSessionsView.root.visibility = View.GONE
                            binding.rvOrders.layoutManager =
                                LinearLayoutManager(
                                    requireContext(),
                                    LinearLayoutManager.VERTICAL,
                                    false
                                )
                            binding.rvOrders.adapter = CompletedOrdersAdapter(orders)
                        }
                    }

                    result.isFailure -> {
                        val error =
                            result.exceptionOrNull()?.message ?: "Failed to get completed sessions"
                        Toast.makeText(
                            requireContext(),
                            error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        viewModel.fetchMyOrders("completed")
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchMyOrders("completed")
    }
}