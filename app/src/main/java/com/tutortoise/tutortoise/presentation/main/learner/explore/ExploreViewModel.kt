package com.tutortoise.tutortoise.presentation.main.learner.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tutortoise.tutortoise.data.model.CategoryResponse


class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}

class ExploreViewModel : ViewModel() {
    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    private val _selectedCategory = MutableLiveData<CategoryResponse?>()
    val selectedCategory: LiveData<CategoryResponse?> = _selectedCategory

    private var hasHandledCategory = false
    private var isNavigatingFromCategory = false

    private val _initialLoadTrigger = MutableLiveData<Event<Unit>>()
    val initialLoadTrigger: LiveData<Event<Unit>> = _initialLoadTrigger

    init {
        _initialLoadTrigger.value = Event(Unit)
    }

    fun setSelectedCategory(category: CategoryResponse) {
        if (!hasHandledCategory) {
            isNavigatingFromCategory = true
            _selectedCategory.value = category
            hasHandledCategory = true
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearData() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        hasHandledCategory = false
        isNavigatingFromCategory = false
    }

    fun isNavigatingFromCategory(): Boolean = isNavigatingFromCategory
}