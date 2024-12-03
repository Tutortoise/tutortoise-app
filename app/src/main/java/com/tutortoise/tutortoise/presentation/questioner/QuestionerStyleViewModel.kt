package com.tutortoise.tutortoise.presentation.questioner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tutortoise.tutortoise.data.model.MessageResponse
import com.tutortoise.tutortoise.data.repository.LearnerRepository
import kotlinx.coroutines.flow.flow

class QuestionerStyleViewModel(private val learnerRepository: LearnerRepository) : ViewModel() {
    fun updateLearningStyle(style: String) = flow<Result<MessageResponse>> {
        emit(learnerRepository.updateLearningStyle(style))
    }

    companion object {
        fun provideFactory(
            learnerRepository: LearnerRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuestionerStyleViewModel(learnerRepository) as T
            }
        }
    }
}