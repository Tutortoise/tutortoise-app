package com.tutortoise.tutortoise.presentation.main.learner.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tutortoise.tutortoise.data.model.OrderResponse
import com.tutortoise.tutortoise.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScheduledLearnerSessionViewModel(private val orderRepository: OrderRepository) : ViewModel() {

    private val _ordersState =
        MutableStateFlow<Result<List<OrderResponse>>>(Result.success(emptyList()))
    val ordersState: StateFlow<Result<List<OrderResponse>>> get() = _ordersState

    fun fetchMyOrders(status: String) {
        viewModelScope.launch {
            val result = orderRepository.getMyOrders(status)
            _ordersState.value = result
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

