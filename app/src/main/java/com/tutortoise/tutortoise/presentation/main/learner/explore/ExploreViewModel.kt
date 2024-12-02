package com.tutortoise.tutortoise.presentation.main.learner.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tutortoise.tutortoise.data.model.CategoryResponse

class ExploreViewModel : ViewModel() {
    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    private val _selectedCategory = MutableLiveData<CategoryResponse?>()
    val selectedCategory: LiveData<CategoryResponse?> = _selectedCategory

    private var hasHandledCategory = false

    fun setSelectedCategory(category: CategoryResponse) {
        if (!hasHandledCategory) {
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
    }
}