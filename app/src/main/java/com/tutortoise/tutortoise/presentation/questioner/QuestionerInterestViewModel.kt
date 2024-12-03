package com.tutortoise.tutortoise.presentation.questioner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tutortoise.tutortoise.data.model.CategoryResponse
import com.tutortoise.tutortoise.data.model.MessageResponse
import com.tutortoise.tutortoise.data.repository.CategoryRepository
import com.tutortoise.tutortoise.data.repository.LearnerRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class QuestionerInterestViewModel(
    private val learnerRepository: LearnerRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _categories = MutableLiveData<List<CategoryResponse>>()
    val categories: LiveData<List<CategoryResponse>> = _categories

    fun updateInterests(interests: List<String>) = flow<Result<MessageResponse>> {
        emit(learnerRepository.updateInterests(interests))
    }

    fun fetchCategories() {
        viewModelScope.launch {
            val response = categoryRepository.fetchCategories()
            response?.data?.let {
                _categories.value = it
            }
        }
    }

    companion object {
        fun provideFactory(
            learnerRepository: LearnerRepository,
            categoryRepository: CategoryRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuestionerInterestViewModel(learnerRepository, categoryRepository) as T
            }
        }
    }
}