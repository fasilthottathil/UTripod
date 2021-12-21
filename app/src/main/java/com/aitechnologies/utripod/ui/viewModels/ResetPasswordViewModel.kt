package com.aitechnologies.utripod.ui.viewModels

import android.app.Application
import androidx.lifecycle.*
import com.aitechnologies.utripod.models.Response
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val userRepository: UserRepository,
    private val application: Application
) : ViewModel() {

    private val statusMessage = MutableLiveData<Event<Resource<Response>>>()

    val message: LiveData<Event<Resource<Response>>> get() = statusMessage

    val validate = MutableLiveData<Event<Resource<Response>>>()

    var phone = ""
    var password = ""
    var passwordConfirm = ""

    fun validate() {
        if (phone.isBlank() || phone.isEmpty() || phone.length < 10) {
            validate.value = Event(Resource("Invalid phone", Response(0)))
            return
        } else if (password.isBlank() || password.isEmpty()) {
            validate.value = Event(Resource("Invalid password", Response(1)))
            return
        } else if (passwordConfirm != password) {
            validate.value = Event(Resource("Password not matches", Response(2)))
            return
        } else if (!application.isConnected()) {
            validate.value = Event(Resource("No connection", Response(3)))
            return
        }
        validate.value = Event(Resource("Valid", Response(4)))
    }

    fun getUserByPhone() {
        viewModelScope.launch {
            if (userRepository.getUserByPhone(phone).isEmpty()) {
                statusMessage.value = Event(Resource("Phone number not found", Response(0)))
            } else {
                statusMessage.value = Event(Resource("Phone number found", Response(1)))
            }
        }
    }

    fun resetPassword() {
        viewModelScope.launch {
            userRepository.resetPassword(phone, password)
            statusMessage.value = Event(Resource("Password changed", Response(2)))
        }
    }

}


@Suppress("UNCHECKED_CAST")
class ResetPasswordViewModelProvider(
    private val userRepository: UserRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResetPasswordViewModel::class.java))
            return ResetPasswordViewModel(userRepository, application) as T
        throw IllegalArgumentException("UNKNOWN ViewModel class")
    }
}