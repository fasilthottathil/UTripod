package com.aitechnologies.utripod.ui.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.GroupMessage
import com.aitechnologies.utripod.models.Response
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GroupChatViewModel(
    private val chatsRepository: ChatsRepository,
    private var userRepository: UserRepository,
    private val application: Application
) : ViewModel() {

    val validate = MutableLiveData<Event<Resource<Response>>>()
    val chatListener = MutableLiveData<Event<Resource<List<GroupMessage>>>>()
    var groupMessage = GroupMessage()
    val userProfile = MutableLiveData<List<Users>>()


    @ExperimentalCoroutinesApi
    fun getGroupChatMessages(roomId: String) {
        viewModelScope.launch {
            chatsRepository.getGroupChatMessages(roomId).collect {
                chatListener.value = it
            }
        }
    }

    fun sendGroupMessage(roomId: String) {
        groupMessage.timestamp = Timestamp.now()
        groupMessage.id = System.currentTimeMillis().toString()
        viewModelScope.launch {
            chatsRepository.sendGroupMessage(roomId, groupMessage)
        }
    }

    fun validate() {
        if (groupMessage.message.isBlank() || groupMessage.message.isEmpty()) {
            validate.value = Event(Resource("Empty message", Response(0)))
        } else if (!application.isConnected()) {
            validate.value = Event(Resource("No connection", Response(1)))
        } else {
            validate.value = Event(Resource("Valid", Response(2)))
        }
    }

    fun getUserProfile(username: String) {
        viewModelScope.launch {
            userProfile.value = userRepository.getUserProfile(username)
        }
    }

}

@Suppress("UNCHECKED_CAST")
class GroupChatViewModelProvider(
    private val chatsRepository: ChatsRepository,
    private val userRepository: UserRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupChatViewModel::class.java))
            return GroupChatViewModel(chatsRepository, userRepository ,application) as T
        throw IllegalArgumentException("UNKNOWN ViewModel class")
    }
}