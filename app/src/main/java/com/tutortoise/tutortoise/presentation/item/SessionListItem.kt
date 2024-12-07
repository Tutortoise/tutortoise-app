package com.tutortoise.tutortoise.presentation.item

import com.tutortoise.tutortoise.data.model.OrderResponse

sealed class SessionListItem {
    data class DateHeader(val date: String) : SessionListItem()
    data class SessionItem(val order: OrderResponse) : SessionListItem()
}