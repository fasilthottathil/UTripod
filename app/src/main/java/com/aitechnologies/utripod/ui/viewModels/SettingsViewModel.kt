package com.aitechnologies.utripod.ui.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Response
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val application: Application
) : ViewModel() {

    var isUpdated = MutableLiveData<Boolean>()

    var validate = MutableLiveData<Event<Resource<Response>>>()

    fun updateProfile(users: Users) {
        viewModelScope.launch {
            isUpdated.value = userRepository.updateProfile(users)
        }
    }

    fun validate(users: Users) {

        if (users.name.isEmpty() || users.name.isBlank()) {
            validate.value = Event(Resource("Invalid name", Response(0)))
            return
        } else if (users.phone.length < 10) {
            validate.value = Event(Resource("Invalid phone", Response(1)))
            return
        } else if (users.password.isEmpty() || users.password.isBlank()) {
            validate.value = Event(Resource("Invalid password", Response(2)))
            return
        } else if (!application.isConnected()) {
            validate.value = Event(Resource("No connection", Response(3)))
            return
        }
        validate.value = Event(Resource("Valid", Response(4)))
    }

}

@Suppress("UNCHECKED_CAST")
class SettingsViewModelProvider(
    private val userRepository: UserRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java))
            return SettingsViewModel(userRepository, application) as T
        throw IllegalArgumentException("UNKNOWN ViewModel Class")
    }
}