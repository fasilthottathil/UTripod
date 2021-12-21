package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.UserRepository
import kotlinx.coroutines.launch

class BlockedUsersViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val blockedUsers = MutableLiveData<List<String>>()
    val isUnblocked = MutableLiveData<Boolean>()
    val users = MutableLiveData<List<Users>>()


    fun getBlockedUsers(username: String) {
        viewModelScope.launch {
            blockedUsers.value = userRepository.getBlockedUsers(username)
        }
    }

    fun unblockUser(
        username: String,
        blockedUser: String
    ) {
        viewModelScope.launch {
            userRepository.unblockUser(username, blockedUser)
            isUnblocked.value = true
        }
    }

    fun getUserByIdList(usernameList: List<String>) {
        viewModelScope.launch {
            users.value = userRepository.getUsersByUsernameList(usernameList)
        }
    }

}

@Suppress("UNCHECKED_CAST")
class BlockedUsersProvider(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BlockedUsersViewModel::class.java))
            return BlockedUsersViewModel(userRepository) as T
        throw IllegalAccessError("unknown viewModel class")
    }
}