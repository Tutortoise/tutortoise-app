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
import java.util.TimeZone

class HomeTutorViewModel(private val orderRepository: OrderRepository) : ViewModel() {
    private val _scheduledOrders =
        MutableStateFlow<Result<List<SessionListItem>>>(Result.success(emptyList()))
    val scheduledOrders: StateFlow<Result<List<SessionListItem>>> = _scheduledOrders

    private val _scheduledDates = MutableStateFlow<Set<Calendar>>(emptySet())
    val scheduledDates: StateFlow<Set<Calendar>> = _scheduledDates

    private val _selectedDate = MutableStateFlow<Calendar?>(
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    )
    
    private var allOrders: List<SessionListItem> = emptyList()

    init {
        fetchScheduledOrders()
    }

    fun setSelectedDate(date: Calendar?) {
        _selectedDate.value = date
        if (date == null) {
            _scheduledOrders.value = Result.success(allOrders)
        } else {
            val filtered = filterOrdersByDate(date)
            if (filtered.isEmpty()) {
                _scheduledOrders.value = Result.success(
                    listOf(
                        SessionListItem.DateHeader(
                            SimpleDateFormat(
                                "EEE, d MMM yyyy",
                                Locale.getDefault()
                            ).format(date.time)
                        )
                    )
                )
            } else {
                _scheduledOrders.value = Result.success(filtered)
            }
        }
    }

    private fun filterOrdersByDate(date: Calendar): List<SessionListItem> {
        return allOrders.filter { item ->
            when (item) {
                is SessionListItem.DateHeader -> false
                is SessionListItem.SessionItem -> {
                    val dateFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getTimeZone("UTC")

                    val sessionDate = Calendar.getInstance().apply {
                        dateFormat.parse(item.order.sessionTime)?.let { parsedDate ->
                            timeInMillis = parsedDate.time
                        }
                        timeZone = TimeZone.getDefault()
                    }

                    val normalizedSelectedDate = Calendar.getInstance().apply {
                        set(Calendar.YEAR, date.get(Calendar.YEAR))
                        set(Calendar.MONTH, date.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    sessionDate.get(Calendar.YEAR) == normalizedSelectedDate.get(Calendar.YEAR) &&
                            sessionDate.get(Calendar.MONTH) == normalizedSelectedDate.get(Calendar.MONTH) &&
                            sessionDate.get(Calendar.DAY_OF_MONTH) == normalizedSelectedDate.get(
                        Calendar.DAY_OF_MONTH
                    )
                }
            }
        }
    }

    fun fetchScheduledOrders() {
        viewModelScope.launch {
            val result = orderRepository.getMyOrders("scheduled")
            allOrders = result.getOrNull()?.let { orders ->
                groupOrdersByDate(orders, SortOrder.ASCENDING)
            } ?: emptyList()

            _selectedDate.value?.let { date ->
                filterOrdersByDate(date)
            } ?: run {
                _scheduledOrders.value = Result.success(allOrders)
            }

            result.getOrNull()?.let { orders ->
                val dateFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")

                val dates = orders.mapNotNull { order ->
                    order.sessionTime?.let { time ->
                        dateFormat.parse(time)?.let { parsedDate ->
                            Calendar.getInstance().apply {
                                timeInMillis = parsedDate.time
                                timeZone = TimeZone.getDefault()
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
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