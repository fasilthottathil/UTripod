package com.aitechnologies.utripod.ui.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Chats
import com.aitechnologies.utripod.models.SocialLinks
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class OthersProfileViewModel(
    private val userRepository: UserRepository,
    private val chatsRepository: ChatsRepository,
    private val application: Application
) : ViewModel() {

    val isFollowing = MutableLiveData<Event<Resource<Boolean>>>()

    val roomId = MutableLiveData<Chats>()
    val linkList = MutableLiveData<List<SocialLinks>>()
    val usernameList = MutableLiveData<ArrayList<String>>()

    @ExperimentalCoroutinesApi
    fun isFollowing(username: String) {
        viewModelScope.launch {
            userRepository.checkIsFollowing(application, username).collect {
                isFollowing.value = it
            }
        }
    }

    fun followOrUnfollow(username: String) {
        viewModelScope.launch {
            userRepository.followOrUnfollow(username, application)
        }
    }

    fun startChat(users: Users) {
        viewModelScope.launch {
            roomId.value = chatsRepository.startChat(users, application)
        }
    }

    fun getSocialLinks(user: String) {
        viewModelScope.launch {
            linkList.value = userRepository.getSocialLinks(user)
        }
    }

    fun getFollowers(username:String){
        viewModelScope.launch {
            usernameList.value = userRepository.getMyFollowers(username)
        }
    }

    fun getFollowings(username:String){
        viewModelScope.launch {
            usernameList.value = userRepository.getMyFollowings(username)
        }
    }

}

@Suppress("UNCHECKED_CAST")
class OthersProfileViewModelProvider(
    private val userRepository: UserRepository,
    private val chatsRepository: ChatsRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OthersProfileViewModel::class.java))
            return OthersProfileViewModel(userRepository, chatsRepository, application) as T
        throw IllegalArgumentException("UNKNOWN ViewModel Class")
    }
}