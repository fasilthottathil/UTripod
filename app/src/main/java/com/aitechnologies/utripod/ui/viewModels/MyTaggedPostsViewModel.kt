package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Posts
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.PostRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MyTaggedPostsViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val myPostsListener = MutableLiveData<Event<Resource<List<Posts>>>>()
    val sharePost = MutableLiveData<Event<String>>()
    val isDeleted = MutableLiveData<Resource<Posts>>()
    val userProfile = MutableLiveData<List<Users>>()

    @ExperimentalCoroutinesApi
    fun getTaggedPosts(username: String) {
        viewModelScope.launch {
            postRepository.getTaggedPosts(username).collect {
                myPostsListener.value = it
            }
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
class MyTaggedPostsViewModelProvider(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyTaggedPostsViewModel::class.java))
            return MyTaggedPostsViewModel(postRepository, userRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel class")
    }
}