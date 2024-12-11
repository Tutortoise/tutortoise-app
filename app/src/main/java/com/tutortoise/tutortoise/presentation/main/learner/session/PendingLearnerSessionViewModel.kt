package com.tutortoise.tutortoise.presentation.main.learner.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tutortoise.tutortoise.data.repository.OrderRepository
import com.tutortoise.tutortoise.presentation.item.SessionListItem
import com.tutortoise.tutortoise.utils.LoadState
import com.tutortoise.tutortoise.utils.SortOrder
import com.tutortoise.tutortoise.utils.groupOrdersByDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PendingLearnerSessionViewModel(private val orderRepository: OrderRepository) : ViewModel() {
    private val _ordersState = MutableStateFlow<LoadState<List<SessionListItem>>>(LoadState.Loading)
    val ordersState: StateFlow<LoadState<List<SessionListItem>>> = _ordersState

    fun fetchMyOrders(status: String) {
        viewModelScope.launch {
            _ordersState.value = LoadState.Loading
            try {
                val result = orderRepository.getMyOrders(status)
                result.fold(
                    onSuccess = { orders ->
                        val groupedOrders = groupOrdersByDate(
                            orders,
                            SortOrder.ASCENDING,
                            groupByField = { it.createdAt }
                        )
                        _ordersState.value = LoadState.Success(groupedOrders)
                    },
                    onFailure = { error ->
                        _ordersState.value = LoadState.Error(error.message ?: "Unknown error")
                    }
                )
            } catch (e: Exception) {
                _ordersState.value = LoadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    companion object {
        fun provideFactory(
            orderRepository: OrderRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PendingLearnerSessionViewModel(orderRepository) as T
            }
        }
    }
}

