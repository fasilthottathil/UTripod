package com.aitechnologies.utripod.ui.viewModels

import android.app.Application
import androidx.lifecycle.*
import com.aitechnologies.utripod.models.Response
import com.aitechnologies.utripod.repository.LoginRepository
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.login
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginRepository: LoginRepository,
    private val application: Application
) : ViewModel() {

    private val statusMessage = MutableLiveData<Event<Resource<Response>>>()

    val message: LiveData<Event<Resource<Response>>> get() = statusMessage

    val validate = MutableLiveData<Event<Resource<Response>>>()

    var username = ""
    var password = ""

    fun login() {
        viewModelScope.launch {
            val querySnapshot = loginRepository.login(username)
            if (querySnapshot != null) {
                if (querySnapshot.isEmpty) {
                    statusMessage.value = Event(Resource("Username not found", Response(1)))
                } else {
                    for (doc in querySnapshot) {
                        if (doc.data["password"] == password) {
                            application.login(
                                username,
                                doc.data["userId"].toString(),
                                doc.data["profileUrl"].toString(),
                                doc.data["profession"].toString()
                            )
                            statusMessage.value = Event(Resource("Login success", Response(3)))
                        } else {
                            statusMessage.value = Event(Resource("Incorrect password", Response(2)))
                        }
                    }
                }

            } else {
                statusMessage.value = Event(Resource("An error occurred", Response(0)))
            }
        }

    }

    fun validate() {
        if (username.isEmpty() || username.isBlank()) {
            validate.value = Event(Resource("Invalid username", Response(0)))
            return
        }
        if (password.isEmpty() || password.isBlank()) {
            validate.value = Event(Resource("Invalid password", Response(1)))
            return
        }

        if (!application.isConnected()) {
            validate.value = Event(Resource("No connection", Response(2)))
            return
        }
        validate.value = Event(Resource("Valid", Response(3)))
    }

}

@Suppress("UNCHECKED_CAST")
class LoginViewModelProvider(
    private val loginRepository: LoginRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java))
            return LoginViewModel(loginRepository, application) as T
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}