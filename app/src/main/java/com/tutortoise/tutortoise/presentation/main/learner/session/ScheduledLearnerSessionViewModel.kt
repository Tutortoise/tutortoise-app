package com.tutortoise.tutortoise.presentation.main.learner.session

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tutortoise.tutortoise.data.repository.OrderRepository
import com.tutortoise.tutortoise.presentation.item.SessionListItem
import com.tutortoise.tutortoise.utils.SortOrder
import com.tutortoise.tutortoise.utils.groupOrdersByDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScheduledLearnerSessionViewModel(private val orderRepository: OrderRepository) : ViewModel() {
    private val _ordersState =
        MutableStateFlow<Result<List<SessionListItem>>>(Result.success(emptyList()))
    val ordersState: StateFlow<Result<List<SessionListItem>>> = _ordersState

    private var isInitialized = false


    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchMyOrders(status: String) {
        if (isInitialized) return

        viewModelScope.launch {
            try {
                val result = orderRepository.getMyOrders(status)
                _ordersState.value = result.map { orders ->
                    groupOrdersByDate(orders, SortOrder.ASCENDING)
                }
                isInitialized = true
            } catch (e: Exception) {
                _ordersState.value = Result.failure(e)
            }
        }
    }

    companion object {
        fun provideFactory(
            orderRepository: OrderRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ScheduledLearnerSessionViewModel(orderRepository) as T
            }
        }
    }
}

