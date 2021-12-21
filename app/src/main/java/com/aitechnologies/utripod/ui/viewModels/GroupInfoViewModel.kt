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

class GroupInfoViewModel(
    private val chatsRepository: ChatsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val groupMembers = MutableLiveData<Event<Resource<ArrayList<String>>>>()
    val users = MutableLiveData<List<Users>>()
    val isCleared = MutableLiveData<Boolean>()

    @ExperimentalCoroutinesApi
    fun getGroupMembers(roomId: String) {
        viewModelScope.launch {
            chatsRepository.getGroupMembers(roomId).collect {
                groupMembers.value = it
            }
        }
    }

    fun getUsersByUsernameList(usernameList: List<String>) {
        viewModelScope.launch {
            users.value = userRepository.getUsersByUsernameList(usernameList)
        }
    }

    fun clearGroupMessages(roomId: String) {
        viewModelScope.launch {
            chatsRepository.clearGroupMessage(roomId)
            isCleared.value = true
        }
    }

}

@Suppress("UNCHECKED_CAST")
class GroupInfoViewModelProvider(
    private val chatsRepository: ChatsRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupInfoViewModel::class.java))
            return GroupInfoViewModel(chatsRepository, userRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel class")
    }
}