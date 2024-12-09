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
import com.tutortoise.tutortoise.databinding.FragmentLearnerCompletedSessionBinding
import com.tutortoise.tutortoise.presentation.main.learner.session.adapter.OrdersAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CompletedLearnerSessionFragment : Fragment() {
    private var _binding: FragmentLearnerCompletedSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CompletedLearnerSessionViewModel by viewModels {
        CompletedLearnerSessionViewModel.provideFactory(OrderRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnerCompletedSessionBinding.inflate(inflater, container, false)
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
                            binding.noBookedSessionsView.btnFindTutor.setOnClickListener {

                            }
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

}