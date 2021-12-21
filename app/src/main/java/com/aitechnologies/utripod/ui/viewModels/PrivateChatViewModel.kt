package com.aitechnologies.utripod.ui.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.PrivateMessage
import com.aitechnologies.utripod.models.Response
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.repository.NotificationRepository
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PrivateChatViewModel(
    private val chatsRepository: ChatsRepository,
    private val application: Application,
    private var notificationRepository: NotificationRepository
) : ViewModel() {

    val validate = MutableLiveData<Event<Resource<Response>>>()

    val chatListener = MutableLiveData<Event<Resource<List<PrivateMessage>>>>()

    var privateMessage = PrivateMessage()


    @ExperimentalCoroutinesApi
    fun getPrivateChatMessages(roomId: String) {
        viewModelScope.launch {
            chatsRepository.getPrivateChatMessages(roomId).collect {
                chatListener.value = it
            }
        }
    }

    fun sendPrivateChatMessage(
        roomId: String,
        username: String
    ) {
        privateMessage.timestamp = Timestamp.now()
        privateMessage.id = System.currentTimeMillis().toString()
        viewModelScope.launch {
            chatsRepository.sendPrivateMessage(roomId, username, privateMessage)
            when(privateMessage.type){
                0->notificationRepository.sendNotification(username,privateMessage.message)
                1->notificationRepository.sendNotification(username,"$username send an image")
                2->notificationRepository.sendNotification(username,"$username send a video")
            }

        }
    }

    fun validate() {
        if (privateMessage.message.isBlank() || privateMessage.message.isEmpty()) {
            validate.value = Event(Resource("Empty message", Response(0)))
        } else if (!application.isConnected()) {
            validate.value = Event(Resource("No connection", Response(1)))
        } else {
            validate.value = Event(Resource("Valid", Response(2)))
        }
    }

}

@Suppress("UNCHECKED_CAST")
class PrivateChatViewModelProvider(
    private val chatsRepository: ChatsRepository,
    private val application: Application,
    private val notificationRepository: NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrivateChatViewModel::class.java))
            return PrivateChatViewModel(chatsRepository, application, notificationRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel class")
    }
}