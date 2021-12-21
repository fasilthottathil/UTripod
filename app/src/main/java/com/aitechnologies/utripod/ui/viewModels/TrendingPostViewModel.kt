package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.HashTag
import com.aitechnologies.utripod.models.Posts
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.PostRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrendingPostViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private var promotionLoaded = false
    val posts = MutableLiveData<List<Posts>>()
    val statusMessage = MutableLiveData<Event<Boolean>>()
    val sharePost = MutableLiveData<Event<String>>()
    val isDeleted = MutableLiveData<Resource<Posts>>()
    val userProfile = MutableLiveData<List<Users>>()
    val hashtags = MutableLiveData<List<HashTag>>()
    val blockedUsers = MutableLiveData<List<String>>()


    private fun getPromotions(blockedUserList: ArrayList<String>) {
        viewModelScope.launch (Dispatchers.IO){
            val promotions = postRepository.getPromotions()
            promotionLoaded = true
            val myList: ArrayList<Posts> = arrayListOf()
            if (promotions.isNotEmpty()) {
                promotions.forEach {
                    val viewType = when (it.type) {
                        0 -> 3
                        1 -> 4
                        else -> 5
                    }
                    myList.add(
                        Posts(
                            it.id,
                            it.username,
                            it.profileUrl,
                            it.post,
                            it.likes,
                            it.comments,
                            it.shares,
                            it.type,
                            it.description,
                            it.hashTags,
                            it.tags,
                            it.likesList,
                            it.isPublic,
                            it.timestamp,
                            it.profession,
                            viewType
                        )
                    )
                }
                posts.postValue(myList)
            } else {
                posts.postValue(emptyList())
            }
        }
    }

    private fun getTrendingPosts() {
        viewModelScope.launch (Dispatchers.IO){
            posts.postValue(postRepository.getTrendingPosts())
            statusMessage.postValue(Event(true))
        }
    }

    fun getTrendingHashtags() {
        viewModelScope.launch (Dispatchers.IO){
            hashtags.postValue( postRepository.getTrendingHashTags())
        }
    }

    fun blockUser(
        username: String,
        blockedUser: String
    ) {
        viewModelScope.launch {
            userRepository.blockUser(username, blockedUser)
        }
    }

    fun getBlockedUsers(username: String) {
        viewModelScope.launch (Dispatchers.IO){
            blockedUsers.postValue(userRepository.getBlockedUsers(username))
        }
    }

    fun loadPosts(blockedUserList: ArrayList<String>) {
        viewModelScope.launch (Dispatchers.IO){
            if (!promotionLoaded)
                getPromotions(blockedUserList)
            getTrendingPosts()
        }
    }

    fun likePost(posts: Posts) {
        viewModelScope.launch {
            postRepository.likePost(posts)
        }
    }

    fun sharePost(posts: Posts) {
        viewModelScope.launch {
            sharePost.value = Event(postRepository.sharePost(posts))
        }
    }

    fun reportPost(posts: Posts) {
        viewModelScope.launch { postRepository.reportPost(posts) }
    }

    fun deletePost(posts: Posts) {
        viewModelScope.launch {
            isDeleted.value = if (postRepository.deletePost(posts) == null)
                Resource("error", posts)
            else
                Resource("success", posts)
        }
    }

    fun deletePromotion(posts: Posts) {
        viewModelScope.launch {
            isDeleted.value = if (postRepository.deletePromotion(posts) == null)
                Resource("error", posts)
            else
                Resource("success", posts)
        }
    }

    fun getUserProfile(username: String) {
        viewModelScope.launch {
            userProfile.value = userRepository.getUserProfile(username)
        }
    }

}

@Suppress("UNCHECKED_CAST")
class TrendingPostViewModelProvider(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrendingPostViewModel::class.java))
            return TrendingPostViewModel(postRepository, userRepository) as T
        throw IllegalArgumentException("UNKNOWN viewModel Class")
    }
}