package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Groups
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AppLinkViewModel(
    private val chatsRepository: ChatsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val group = MutableLiveData<Event<Resource<List<Groups>>>>()
    val joinedGroups = MutableLiveData<Event<Resource<List<String>>>>()
    val isJoined = MutableLiveData<Event<Resource<String>>>()
    val user = MutableLiveData<List<Users>>()

    @ExperimentalCoroutinesApi
    fun getGroupById(id: String) {
        viewModelScope.launch {
            chatsRepository.getGroupById(id).collect {
                group.value = it
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun getJoinedGroupsIdList(username: String) {
        viewModelScope.launch {
            chatsRepository.getJoinedGroupsIdList(username).collect {
                joinedGroups.value = it
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun joinGroup(
        roomId: String,
        username: String
    ) {
        viewModelScope.launch {
            chatsRepository.addGroupMember(roomId, username).collect {
                isJoined.value = it
            }
        }
    }

    fun getUserProfile(username: String){
        viewModelScope.launch {
            user.value = userRepository.getUserProfile(username)
        }
    }

}

@Suppress("UNCHECKED_CAST")
class AppLinkViewModelProvider(
    private val chatsRepository: ChatsRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppLinkViewModel::class.java))
            return AppLinkViewModel(chatsRepository,userRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel class")
    }
}