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

class PostReplyCommentViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val statusMessage = MutableLiveData<Event<Resource<List<PostComment>>>>()
    val comments: LiveData<Event<Resource<List<PostComment>>>> get() = statusMessage

    val isDeleted = MutableLiveData<Boolean>()
    val user = MutableLiveData<List<Users>>()

    @ExperimentalCoroutinesApi
    fun getReplyComments(commentId: String) {
        viewModelScope.launch {
            postRepository.getReplyComments(commentId).collect {
                statusMessage.value = it
            }
        }
    }

    fun addReplyComment(
        postComment: PostComment,
        postId: String,
        isFirst: Boolean
    ) {
        viewModelScope.launch {
            postRepository.addReplyComment(postComment, postId, isFirst)
        }
    }

    fun deletePostReplyComment(
        id: String,
        postId: String,
        previousCommentId: String,
        isFirst: Boolean
    ) {
        viewModelScope.launch {
            isDeleted.value =
                postRepository.deleteReplyComment(id, postId, previousCommentId, isFirst) != null
        }
    }

    fun getUser(username:String){
        viewModelScope.launch {
            user.value = userRepository.getUserProfile(username)
        }
    }

}

@Suppress("UNCHECKED_CAST")
class PostReplyCommentViewModelProvider(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostReplyCommentViewModel::class.java))
            return PostReplyCommentViewModel(postRepository,userRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel Class")
    }
}