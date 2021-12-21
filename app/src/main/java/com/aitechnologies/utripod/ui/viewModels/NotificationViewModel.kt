package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.*
import com.aitechnologies.utripod.models.Notification
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.NotificationRepository
import com.aitechnologies.utripod.repository.UserRepository
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _notification = MutableLiveData<List<Notification>>()
    val notification: LiveData<List<Notification>> get() = _notification
    val userProfile = MutableLiveData<List<Users>>()

    fun getNotifications(username: String) {
        viewModelScope.launch {
            _notification.value = notificationRepository.getNotifications(username)
        }
    }

    fun getUserProfile(username: String) {
        viewModelScope.launch {
            userProfile.value = userRepository.getUserProfile(username)
        }
    }

}

@Suppress("UNCHECKED_CAST")
class NotificationViewModelProvider(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java))
            return NotificationViewModel(notificationRepository, userRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel Class")
    }
}