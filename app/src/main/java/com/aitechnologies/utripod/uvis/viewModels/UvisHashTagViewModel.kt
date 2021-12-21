package com.aitechnologies.utripod.uvis.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.models.Uvis
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.repository.UvisRepository
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UvisHashTagViewModel(
    private val uvisRepository: UvisRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val uvis = MutableLiveData<List<Uvis>>()
    val statusMessage = MutableLiveData<Event<Boolean>>()
    val sharePost = MutableLiveData<Event<String>>()
    val isDeleted = MutableLiveData<Resource<Uvis>>()
    val userProfile = MutableLiveData<List<Users>>()
    val followings = MutableLiveData<List<String>>()

    fun getPostByHashtags(hashtag: String) {
        viewModelScope.launch @ExperimentalCoroutinesApi {
            uvisRepository.getUvisByHashtags(hashtag).collect {
                uvis.value = it
            }
        }
    }

    fun getFollowings(
        username: String
    ){
        viewModelScope.launch {
            followings.value = userRepository.getMyFollowings(username)
        }
    }

    fun followOrUnfollow(
        username: String,
        application: Application
    ){
        viewModelScope.launch {
            userRepository.followOrUnfollow(username, application)
        }
    }

    fun getPostById(id: String) {
        viewModelScope.launch @ExperimentalCoroutinesApi {
            uvisRepository.getUvisById(id).collect {
                uvis.value = it
            }
        }
    }

    fun likeUvis(uvis: Uvis) {
        viewModelScope.launch {
            uvisRepository.likeUvis(uvis)
        }
    }

    fun shareUvis(uvis: Uvis) {
        viewModelScope.launch {
            sharePost.value = Event(uvisRepository.shareUvis(uvis))
        }
    }

    fun reportUvis(uvis: Uvis) {
        viewModelScope.launch { uvisRepository.reportUvis(uvis) }
    }

    fun deletePost(uvis: Uvis) {
        viewModelScope.launch {
            isDeleted.value = if (uvisRepository.deleteUvis(uvis) == null)
                Resource("error", uvis)
            else
                Resource("success", uvis)
        }
    }

    fun deletePromotion(uvis: Uvis) {
        viewModelScope.launch {
            isDeleted.value = if (uvisRepository.deletePromotion(uvis) == null)
                Resource("error", uvis)
            else
                Resource("success", uvis)
        }
    }

    fun getUserProfile(username: String) {
        viewModelScope.launch {
            userProfile.value = userRepository.getUserProfile(username)
        }
    }

}

@Suppress("UNCHECKED_CAST")
class UvisHashTagViewModelProvider(
    private val uvisRepository: UvisRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UvisHashTagViewModel::class.java))
            return UvisHashTagViewModel(uvisRepository, userRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel Class")
    }
}