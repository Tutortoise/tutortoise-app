package com.tutortoise.tutortoise.presentation.notification

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.tutortoise.tutortoise.data.model.NotificationResponse
import com.tutortoise.tutortoise.databinding.ActivityNotificationBinding
import com.tutortoise.tutortoise.presentation.notification.adapter.NotificationAdapter

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.btnBack.setOnClickListener { finish() }

        adapter = NotificationAdapter { notification ->
            if (!notification.isRead) {
                viewModel.markAsRead(notification.id)
            }
            // Handle notification click (e.g., navigate to relevant screen)
            handleNotificationClick(notification)
        }

        binding.recyclerNotif.apply {
            layoutManager = LinearLayoutManager(this@NotificationActivity)
            adapter = this@NotificationActivity.adapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadNotifications()
        }
    }

    private fun observeViewModel() {
        viewModel.notifications.observe(this) { notifications ->
            adapter.submitList(notifications)
            binding.emptyState.visibility = if (notifications.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun handleNotificationClick(notification: NotificationResponse) {
        when (notification.type) {
            "order_accepted" -> {
                // Navigate to order details
                val orderId = notification.data?.get("orderId") as? String
                if (orderId != null) {
                    // Navigate to order details
                }
            }

            "order_declined" -> {
                // Navigate to order details
                val orderId = notification.data?.get("orderId") as? String
                if (orderId != null) {
                    // Navigate to order details
                }
            }

            "new_order" -> {
                // Navigate to order details
                val orderId = notification.data?.get("orderId") as? String
                if (orderId != null) {
                    // Navigate to order details
                }
            }
        }
    }
}
