package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.SocialLinks
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.UserRepository
import kotlinx.coroutines.launch

class MyProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val users = MutableLiveData<List<Users>>()
    val usernameList = MutableLiveData<ArrayList<String>>()

    val linkList = MutableLiveData<List<SocialLinks>>()

    fun getUserProfile(user: String) {
        viewModelScope.launch {
            users.value = userRepository.getUserProfile(user)
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
class MyProfileViewModelProvider(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyProfileViewModel::class.java))
            return MyProfileViewModel(userRepository) as T
        throw IllegalArgumentException("UNKNOWN viewModel class")
    }
}