package com.tutortoise.tutortoise.presentation.notification


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tutortoise.tutortoise.data.model.NotificationResponse
import com.tutortoise.tutortoise.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NotificationRepository(application)

    private val _notifications = MutableLiveData<List<NotificationResponse>>()
    val notifications: LiveData<List<NotificationResponse>> = _notifications

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getNotifications()
                    .onSuccess { notifications ->
                        _notifications.value = notifications
                    }
                    .onFailure { exception ->
                        _error.value = exception.message
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
                .onSuccess {
                    loadNotifications() // Reload to update UI
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
                .onSuccess {
                    loadNotifications() // Reload to update UI
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }
}