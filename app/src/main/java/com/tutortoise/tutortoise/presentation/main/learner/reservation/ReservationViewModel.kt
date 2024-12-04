package com.tutortoise.tutortoise.presentation.main.learner.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tutortoise.tutortoise.data.repository.TutoriesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReservationViewModel(
    private val tutoriesRepository: TutoriesRepository
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

    companion object {
        fun provideFactory(
            tutoriesRepository: TutoriesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReservationViewModel(tutoriesRepository) as T
            }
        }
    }
}