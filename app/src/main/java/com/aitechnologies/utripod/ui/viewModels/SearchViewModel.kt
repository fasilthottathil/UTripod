package com.aitechnologies.utripod.ui.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.UserRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

class SearchViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val users = MutableLiveData<List<Users>>()
    val followersList = MutableLiveData<ArrayList<String>>()

    fun searchUser(user: String) {
        viewModelScope.launch {
            users.value = userRepository.searchUser(user)
        }
    }

    fun getUserByRegion(region: String) {
        viewModelScope.launch {
            users.value = userRepository.getUsersByRegion(region)
        }
    }

    fun getUserByAge(age: String) {
        viewModelScope.launch {
            users.value = userRepository.getUserByAge(age)
        }
    }

    fun getUserByGender(gender: String) {
        viewModelScope.launch {
            users.value = userRepository.getUserByGender(gender)
        }
    }

    fun getUserByProfession(profession: String) {
        viewModelScope.launch {
            users.value = userRepository.getUserByProfession(profession)
        }
    }

    fun getFollowingList(
        username:String
    ){
        viewModelScope.launch {
            followersList.value = userRepository.getMyFollowings(username)
        }
    }

    @DelicateCoroutinesApi
    fun followOrUnfollow(
        username: String,
        application: Application
    ){
        viewModelScope.launch {
            userRepository.followOrUnfollow(username, application)
        }
    }

}

@Suppress("UNCHECKED_CAST")
class SearchViewModelProvider(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java))
            return SearchViewModel(userRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel class")
    }
}