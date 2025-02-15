package com.cwp.jinja_hub.ui.notifications

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cwp.jinja_hub.model.NotificationModel
import com.cwp.jinja_hub.repository.NotificationRepository
import kotlinx.coroutines.launch
import java.util.*

class NotificationsViewModel : ViewModel() {

    private val repository = NotificationRepository()

    // LiveData for loading state.
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData for error messages.
    private val _notificationError = MutableLiveData<String?>()
    val notificationError: LiveData<String?> get() = _notificationError

    // LiveData for notifications from today.
    private val _recentNotifications = MutableLiveData<List<NotificationModel>>()
    val recentNotifications: LiveData<List<NotificationModel>> get() = _recentNotifications

    // LiveData for notifications from earlier days.
    private val _laterNotifications = MutableLiveData<List<NotificationModel>>()
    val laterNotifications: LiveData<List<NotificationModel>> get() = _laterNotifications

    init {
        // Launch loading notifications in a coroutine.
        viewModelScope.launch {
            loadNotifications()
        }
    }

    fun refreshNotifications() {
        loadNotifications()
    }
    private fun loadNotifications() {
        _isLoading.value = true
        try {
            // Fetch the full list of notifications from Firebase.
            val fullNotifications = repository.fetchNotifications(
                onUpdate = { notification ->
                    // Get today's start (midnight) in milliseconds.
                    val todayStart = getTodayStart()

                    // Sort the notifications in ascending order by timestamp.
                    val sortedNotifications = notification.sortedByDescending { it.timestamp }


                    // Split notifications:
                    _recentNotifications.value = sortedNotifications.filter {
                        it.timestamp >= todayStart
                    }
                    _laterNotifications.value = sortedNotifications.filter { it.timestamp < todayStart }
                }
            )
            _isLoading.value = false

        } catch (e: Exception) {
            _notificationError.value = e.message
        } finally {
            _isLoading.value = false
        }
    }

    fun sendNotification(id: String, notification: NotificationModel) {
        viewModelScope.launch {
            val success = repository.sendNotification(id, notification)
            if (success) {
                // Notify UI of success
                _isLoading.value = false
                _notificationError.value = null
            } else {
                // Handle error
                _isLoading.value = false
                _notificationError.value = "Failed to send notification"
            }
        }

    }

    fun markNotificationAsRead(notificationId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val success = repository.markNotificationAsRead(notificationId)
            if (success) {
                _isLoading.value = false
                // Update UI or notify the user that the notification has been marked as read.
            } else {
                _isLoading.value = false
                // Handle the error.
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val success = repository.deleteNotification(notificationId)
            if (success) {
                _isLoading.value = false
                // Update UI or notify the user that the notification has been deleted.
            } else {
                _isLoading.value = false
            }
        }
    }

    fun deleteAllNotifications() {
        _isLoading.value = true
        viewModelScope.launch {
            val success = repository.deleteAllNotifications()
            if (success) {
                _isLoading.value = false
                // Update UI or notify the user that all notifications have been deleted.
            } else {
                _isLoading.value = false
                _notificationError.value = "Failed to delete all notifications"
            }
        }
    }

    // Helper function to get the start of today in milliseconds.
    private fun getTodayStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
