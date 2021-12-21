package com.aitechnologies.utripod.ui.viewModels

import android.app.Application
import androidx.lifecycle.*
import com.aitechnologies.utripod.models.Response
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.RegisterRepository
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerRepository: RegisterRepository,
    private val application: Application
) : ViewModel() {

    companion object {
        const val INVALID_NAME = 0
        const val INVALID_USERNAME = 1
        const val INVALID_PASSWORD = 2
        const val INVALID_PASSWORD_C = 3
        const val INVALID_PHONE = 4
        const val INVALID_LOCATION = 5
        const val NO_CONNECTION = 6
        const val VALID = 7
    }

    private val statusMessage = MutableLiveData<Event<Response>>()
    val message: LiveData<Event<Response>> get() = statusMessage

    val validate = MutableLiveData<Event<Resource<Response>>>()

    var users: Users = Users()

    var password = ""

    fun register() {
        viewModelScope.launch {
            users.profileUrl =
                "https://firebasestorage.googleapis.com/v0/b/utripod-c5add.appspot.com/o/profile%2F${users.username}?alt=media"
            statusMessage.value = Event(registerRepository.register(users))
        }
    }

    fun validate() {
        if (users.name.isEmpty() || users.name.isBlank()) {
            validate.value = Event(Resource("Invalid Name", Response(INVALID_NAME)))
            return
        } else if (users.username.isEmpty() || users.username.isBlank()) {
            validate.value = Event(Resource("Invalid Username", Response(INVALID_USERNAME)))
            return
        } else if (users.password.isEmpty() || users.password.isBlank()) {
            validate.value = Event(Resource("Invalid password", Response(INVALID_PASSWORD)))
            return
        } else if (password.isEmpty() || password.isBlank()) {
            validate.value = Event(Resource("Invalid password", Response(INVALID_PASSWORD_C)))
            return
        } else if (password != users.password) {
            validate.value = Event(Resource("Does not match", Response(INVALID_PASSWORD_C)))
            return
        } else if (users.phone.isEmpty() || users.phone.isBlank()) {
            validate.value = Event(Resource("Invalid phone", Response(INVALID_PHONE)))
            return
        } else if (users.phone.length < 10) {
            validate.value = Event(Resource("Invalid phone", Response(INVALID_PHONE)))
            return
        } else if (users.location.isEmpty() || users.location.isBlank()) {
            validate.value = Event(Resource("Select location", Response(INVALID_LOCATION)))
            return
        } else if (!application.isConnected()) {
            validate.value = Event(Resource("No connection", Response(NO_CONNECTION)))
            return
        }
        validate.value = Event(Resource("Valid", Response(VALID)))
    }

}

@Suppress("UNCHECKED_CAST")
class RegisterProvider(
    private val registerRepository: RegisterRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java))
            return RegisterViewModel(registerRepository, application) as T
        throw IllegalArgumentException("Unknown viewModel class")
    }
}