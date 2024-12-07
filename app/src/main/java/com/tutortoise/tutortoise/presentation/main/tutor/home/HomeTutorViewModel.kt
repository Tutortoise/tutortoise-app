package com.tutortoise.tutortoise.presentation.main.tutor.home

import android.util.Log
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

class HomeTutorViewModel(private val orderRepository: OrderRepository) : ViewModel() {
    private val _scheduledOrders =
        MutableStateFlow<Result<List<SessionListItem>>>(Result.success(emptyList()))
    val scheduledOrders: StateFlow<Result<List<SessionListItem>>> = _scheduledOrders

    fun fetchScheduledOrders() {
        viewModelScope.launch {
            val result = orderRepository.getMyOrders("scheduled")
            Log.d("HomeTutorViewModel", "Number of scheduled orders: ${result.getOrNull()?.size}")
            _scheduledOrders.value = result.map { orders ->
                groupOrdersByDate(
                    orders,
                    SortOrder.ASCENDING
                )
            }
        }
    }

    companion object {
        fun provideFactory(
            orderRepository: OrderRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeTutorViewModel(orderRepository) as T
            }
        }
    }
}