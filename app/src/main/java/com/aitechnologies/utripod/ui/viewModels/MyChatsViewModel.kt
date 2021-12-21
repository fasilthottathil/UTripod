package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Chats
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MyChatsViewModel(
    private val chatsRepository: ChatsRepository
) : ViewModel() {

    val chatsListener = MutableLiveData<Event<Resource<List<Chats>>>>()

    @ExperimentalCoroutinesApi
    fun getMyChats(username: String) {
        viewModelScope.launch {
            chatsRepository.getMyChats(username).collect {
                chatsListener.value = it
            }
        }
    }

}

@Suppress("UNCHECKED_CAST")
class MyChatsViewModelProvider(
    private val chatsRepository: ChatsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyChatsViewModel::class.java))
            return MyChatsViewModel(chatsRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel Class")
    }
}