package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AddGroupMembersViewModel(
    private val chatsRepository: ChatsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val users = MutableLiveData<List<Users>>()

    val isAdded = MutableLiveData<Event<Resource<String>>>()

    fun searchUser(user: String) {
        viewModelScope.launch {
            users.value = userRepository.searchUser(user)
        }
    }

    @ExperimentalCoroutinesApi
    fun addGroupMember(
        roomId: String,
        username: String
    ) {
        viewModelScope.launch {
            chatsRepository.addGroupMember(roomId, username).collect {
                isAdded.value = it
            }
        }
    }

}

@Suppress("UNCHECKED_CAST")
class AddGroupMembersViewModelProvider(
    private val chatsRepository: ChatsRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddGroupMembersViewModel::class.java))
            return AddGroupMembersViewModel(chatsRepository, userRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel class")
    }
}