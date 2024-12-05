package com.tutortoise.tutortoise.presentation.main.learner.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tutortoise.tutortoise.data.repository.OrderRepository
import com.tutortoise.tutortoise.data.repository.TutoriesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReservationViewModel(
    private val tutoriesRepository: TutoriesRepository,
    private val orderRepository: OrderRepository
) : ViewModel(
) {
    private val _availability = MutableStateFlow<List<String>?>(null)
    val availability: StateFlow<List<String>?> = _availability

    // TODO: Add reservation business logic here
    fun fetchAvailability(tutoriesId: String) {
        viewModelScope.launch {
            val response = tutoriesRepository.fetchAvailability(tutoriesId)
            _availability.value = response?.data
        }
    }

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
            tutoriesRepository: TutoriesRepository,
            orderRepository: OrderRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReservationViewModel(tutoriesRepository, orderRepository) as T
            }
        }
    }
}