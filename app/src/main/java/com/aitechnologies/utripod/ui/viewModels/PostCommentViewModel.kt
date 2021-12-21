package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.*
import com.aitechnologies.utripod.models.PostComment
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.PostRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PostCommentViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val statusMessage = MutableLiveData<Event<Resource<List<PostComment>>>>()
    val comments: LiveData<Event<Resource<List<PostComment>>>> get() = statusMessage

    var isDeleted = MutableLiveData<Boolean>()
    val user = MutableLiveData<List<Users>>()

    @ExperimentalCoroutinesApi
    fun getComments(postId: String) {
        viewModelScope.launch {
            postRepository.getComments(postId).collect {
                statusMessage.value = it
            }
        }
    }

    fun addComment(postComment: PostComment) {
        viewModelScope.launch {
            postRepository.addComment(postComment)
        }
    }

    fun deleteComment(
        id: String,
        postId: String
    ) {
        viewModelScope.launch {
            postRepository.deleteComment(id, postId)
            isDeleted.value = postRepository.deleteComment(id, postId) != null
        }
    }

    fun getUser(username:String){
        viewModelScope.launch {
            user.value = userRepository.getUserProfile(username)
        }
    }

}

@Suppress("UNCHECKED_CAST")
class PostCommentViewModelProvider(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostCommentViewModel::class.java))
            return PostCommentViewModel(postRepository,userRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel Class")
    }
}