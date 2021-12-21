package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.UserRepository
import kotlinx.coroutines.launch

class ViewUsersViewModel(
    private val userRepository: UserRepository
):ViewModel() {

    val users = MutableLiveData<List<Users>>()

    fun getUsers(usernameList: ArrayList<String>){
        viewModelScope.launch {
            users.value = userRepository.getUsersByUsernameList(usernameList)
        }
    }

}

@Suppress("UNCHECKED_CAST")
class ViewUsersProvider(
    private val userRepository: UserRepository
):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewUsersViewModel::class.java))
            return ViewUsersViewModel(userRepository) as T
        throw IllegalArgumentException("Unknown viewModel class")
    }
}