package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.UserRepository
import kotlinx.coroutines.launch

class TagUserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val users = MutableLiveData<List<Users>>()

    val usersSelected = MutableLiveData<List<Users>>()

    val userList: ArrayList<Users> = arrayListOf()
    val usersSelectedList: ArrayList<Users> = arrayListOf()

    fun searchUser(user: String) {
        viewModelScope.launch {
            users.value = userRepository.searchUser(user)
        }
    }

    fun addToSelected(users: Users) {
        if (!usersSelectedList.contains(users)) {
            usersSelectedList.add(users)
            usersSelected.value = usersSelectedList
        }
    }

    fun removeFromSelected(user: Users) {
        usersSelectedList.remove(user)
        usersSelected.value = usersSelectedList
    }

}

@Suppress("UNCHECKED_CAST")
class TagUserViewModelProvider(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TagUserViewModel::class.java))
            return TagUserViewModel(userRepository) as T
        throw IllegalArgumentException("Unknown viewModel class")
    }
}