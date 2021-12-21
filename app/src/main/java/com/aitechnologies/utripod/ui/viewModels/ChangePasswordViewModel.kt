package com.aitechnologies.utripod.ui.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Response
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.launch

class ChangePasswordViewModel(
    private val userRepository: UserRepository,
    private val application: Application
) : ViewModel() {

    var isUpdated = MutableLiveData<Boolean>()

    var validate = MutableLiveData<Event<Resource<Response>>>()

    fun updatePassword(users: String, password: String) {
        viewModelScope.launch {
            isUpdated.value = userRepository.updatePassword(users, password)
        }
    }

    fun validate(
        password: String,
        confirmPassword: String
    ) {

        if (password.isEmpty() || password.isBlank()) {
            validate.value = Event(Resource("Invalid password", Response(0)))
            return
        } else if (confirmPassword != password) {
            validate.value = Event(Resource("Password not matches", Response(1)))
            return
        } else if (!application.isConnected()) {
            validate.value = Event(Resource("No connection", Response(2)))
            return
        }
        validate.value = Event(Resource("Valid", Response(3)))
    }

}

@Suppress("UNCHECKED_CAST")
class ChangePasswordViewModelProvider(
    private val userRepository: UserRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChangePasswordViewModel::class.java))
            return ChangePasswordViewModel(userRepository, application) as T
        throw IllegalArgumentException("UNKNOWN ViewModel Class")
    }
}