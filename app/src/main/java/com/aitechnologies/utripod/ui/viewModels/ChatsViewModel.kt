package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Chats
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChatsViewModel(
    private val userRepository: UserRepository,
    private val chatsRepository: ChatsRepository
):ViewModel() {


    val activeUsers = MutableLiveData<Event<Resource<List<Chats>>>>()


    fun getFollowings(username:String){
        viewModelScope.launch (Dispatchers.IO){
            getChatsByUsernameList(username,userRepository.getMyFollowings(username))
        }
    }


    private fun getChatsByUsernameList(username: String, list: ArrayList<String>){
        if (list.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) @ExperimentalCoroutinesApi {
                userRepository.getOnlineUsers(list).collect {
                    if (it.isNotEmpty()){
                        chatsRepository.getMyChatsByIdList(username, it).collect { event ->
                            activeUsers.postValue(event)
                        }
                    }else{
                        activeUsers.postValue(Event(Resource("no users")))
                    }
                }
            }
        }
        else{
            activeUsers.postValue(Event(Resource("no users")))
        }
    }

}

@Suppress("UNCHECKED_CAST")
class ChatsProvider(
    private val userRepository: UserRepository,
    private val chatsRepository: ChatsRepository
):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatsViewModel::class.java))
            return ChatsViewModel(userRepository,chatsRepository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}