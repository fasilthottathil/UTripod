package com.aitechnologies.utripod.ui.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.isLogin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(private val application: Application) : ViewModel() {

    val isLogin = MutableLiveData<Boolean>()

    fun isLogin() {
        viewModelScope.launch {
            delay(1500)
            isLogin.value = application.isLogin()
        }
    }

}

@Suppress("UNCHECKED_CAST")
class SplashViewModelProvider(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java))
            return SplashViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}