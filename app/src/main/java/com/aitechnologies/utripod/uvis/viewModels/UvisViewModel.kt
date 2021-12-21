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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

class UvisViewModel(
    private val uvisRepository: UvisRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private var promotionLoaded = false
    val uvis = MutableLiveData<List<Uvis>>()
    val uvisTrending = MutableLiveData<List<Uvis>>()
    val statusMessage = MutableLiveData<Event<Boolean>>()
    val sharePost = MutableLiveData<Event<String>>()
    val isDeleted = MutableLiveData<Resource<Uvis>>()
    val userProfile = MutableLiveData<List<Users>>()
    val followings = MutableLiveData<List<String>>()


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
        viewModelScope.launch @DelicateCoroutinesApi{
            userRepository.followOrUnfollow(username, application)
        }
    }

    private fun getPromotions() {
        viewModelScope.launch {
            val promotions = uvisRepository.getPromotions()
            promotionLoaded = true
            val myList: ArrayList<Uvis> = arrayListOf()
            if (promotions.isNotEmpty()) {
                promotions.forEach {
                    myList.add(
                        Uvis(
                            it.id,
                            it.username,
                            it.profileUrl,
                            it.url,
                            it.likes,
                            it.comments,
                            it.shares,
                            it.description,
                            it.hashTags,
                            it.tags,
                            it.likesList,
                            it.isPublic,
                            it.timestamp,
                            it.profession,
                            1
                        )
                    )
                }
                uvis.value = myList
            } else {
                uvis.value = emptyList()
            }
        }
    }



    fun getMyPromotions() {
        viewModelScope.launch {
            val promotions = uvisRepository.getMyPromotions()
            promotionLoaded = true
            val myList: ArrayList<Uvis> = arrayListOf()
            if (promotions.isNotEmpty()) {
                promotions.forEach {
                    myList.add(
                        Uvis(
                            it.id,
                            it.username,
                            it.profileUrl,
                            it.url,
                            it.likes,
                            it.comments,
                            it.shares,
                            it.description,
                            it.hashTags,
                            it.tags,
                            it.likesList,
                            it.isPublic,
                            it.timestamp,
                            it.profession,
                            1
                        )
                    )
                }
                uvis.value = myList
            } else {
                uvis.value = emptyList()
            }
        }
    }

    private fun getFollowersPosts() {
        viewModelScope.launch {
            uvis.value = uvisRepository.getMyFollowersUvis()
            statusMessage.value = Event(true)
        }
    }

    fun getMyUvis(username: String) {
        viewModelScope.launch {
            uvis.value = uvisRepository.getMyUvis(username)
            statusMessage.value = Event(true)
        }
    }

    fun getTrendingUvis() {
        viewModelScope.launch {
            uvisTrending.value = uvisRepository.getTrendingUvis()
        }
    }

    fun loadPosts() {
        viewModelScope.launch {
            if (!promotionLoaded)
                getPromotions()
            getFollowersPosts()
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
class UvisViewModelProvider(
    private val uvisRepository: UvisRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UvisViewModel::class.java))
            return UvisViewModel(uvisRepository, userRepository) as T
        throw IllegalArgumentException("UNKNOWN viewModel Class")
    }
}