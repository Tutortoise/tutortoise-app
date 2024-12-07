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
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.repository.OrderRepository
import com.tutortoise.tutortoise.databinding.FragmentTutorScheduledSessionBinding
import com.tutortoise.tutortoise.presentation.main.tutor.sessions.adapter.ScheduledOrdersAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScheduledTutorSessionFragment : Fragment() {
    private var _binding: FragmentTutorScheduledSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScheduledTutorSessionViewModel by viewModels {
        ScheduledTutorSessionViewModel.provideFactory(OrderRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorScheduledSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ordersState.collectLatest { result ->
                when {
                    result.isSuccess -> {
                        val orders = result.getOrNull()
                        binding.rvOrders.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        binding.rvOrders.adapter =
                            ScheduledOrdersAdapter(orders ?: emptyList()) {
                                showCancelConfirmation(it)
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

    override fun onResume() {
        super.onResume()
        viewModel.fetchMyOrders("scheduled")
    }

    private fun showCancelConfirmation(orderId: String) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_cancel_confirmation_dialog, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        // Make dialog rounded corners
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)

        // Set up click listeners for the buttons
        dialogView.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            val buttonsLayout = dialogView.findViewById<View>(R.id.buttonsLayout)

            buttonsLayout.visibility = View.GONE

            lifecycleScope.launch {
                try {
                    viewModel.cancelOrder(orderId, "scheduled")
                    dialog.dismiss()
                } catch (e: Exception) {
                    // Handle error
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Failed to cancel scheduled order. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                        buttonsLayout.visibility = View.VISIBLE
                    }
                }
            }
        }

        dialog.show()
    }

}