package com.tutortoise.tutortoise.presentation.main.tutor.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tutortoise.tutortoise.data.repository.OrderRepository
import com.tutortoise.tutortoise.presentation.item.SessionListItem
import com.tutortoise.tutortoise.utils.groupOrdersByDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CompletedTutorSessionViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _ordersState =
        MutableStateFlow<Result<List<SessionListItem>>>(Result.success(emptyList()))
    val ordersState: StateFlow<Result<List<SessionListItem>>> get() = _ordersState

    fun fetchMyOrders(status: String) {
        viewModelScope.launch {
            val result = orderRepository.getMyOrders(status)
            _ordersState.value = result.map { orders -> groupOrdersByDate(orders) }
        }
    }

    companion object {
        fun provideFactory(
            orderRepository: OrderRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CompletedTutorSessionViewModel(orderRepository) as T
            }
        }
    }
}