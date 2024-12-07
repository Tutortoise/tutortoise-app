package com.tutortoise.tutortoise.presentation.main.tutor.home

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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeTutorViewModel(private val orderRepository: OrderRepository) : ViewModel() {
    private val _scheduledOrders =
        MutableStateFlow<Result<List<SessionListItem>>>(Result.success(emptyList()))
    val scheduledOrders: StateFlow<Result<List<SessionListItem>>> = _scheduledOrders

    private val _scheduledDates = MutableStateFlow<Set<Calendar>>(emptySet())
    val scheduledDates: StateFlow<Set<Calendar>> = _scheduledDates

    fun fetchScheduledOrders() {
        viewModelScope.launch {
            val result = orderRepository.getMyOrders("scheduled")
            _scheduledOrders.value = result.map { orders ->
                groupOrdersByDate(orders, SortOrder.ASCENDING)
            }

            result.getOrNull()?.let { orders ->
                val dateFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val dates = orders.mapNotNull { order ->
                    order.sessionTime?.let { time ->
                        dateFormat.parse(time)?.let { parsedDate ->
                            Calendar.getInstance().apply {
                                timeInMillis =
                                    parsedDate.time
                            }
                        }
                    }
                }.toSet()
                _scheduledDates.value = dates
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