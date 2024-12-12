package com.tutortoise.tutortoise.presentation.main.learner.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tutortoise.tutortoise.data.repository.OrderRepository
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val orderRepository: OrderRepository
) : ViewModel(
) {
    fun reserveOrder(
        tutoriesId: String,
        typeLesson: String,
        sessionTime: String,
        totalHours: Int,
        note: String?
    ) {
        viewModelScope.launch {
            orderRepository.reserveOrder(tutoriesId, typeLesson, sessionTime, totalHours, note)
        }
    }

    companion object {
        fun provideFactory(
            orderRepository: OrderRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PaymentViewModel(orderRepository) as T
            }
        }
    }
}
