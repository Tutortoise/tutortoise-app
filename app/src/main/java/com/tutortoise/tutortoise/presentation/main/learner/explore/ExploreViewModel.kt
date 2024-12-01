package com.tutortoise.tutortoise.presentation.main.learner.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tutortoise.tutortoise.data.model.SubjectResponse

class ExploreViewModel : ViewModel() {
    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    private val _selectedSubject = MutableLiveData<SubjectResponse?>()
    val selectedSubject: LiveData<SubjectResponse?> = _selectedSubject

    private var hasHandledSubject = false

    fun setSelectedSubject(subject: SubjectResponse) {
        if (!hasHandledSubject) {
            _selectedSubject.value = subject
            hasHandledSubject = true
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearData() {
        _searchQuery.value = ""
        _selectedSubject.value = null
        hasHandledSubject = false
    }
}