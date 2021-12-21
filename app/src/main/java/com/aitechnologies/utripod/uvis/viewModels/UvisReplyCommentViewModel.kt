package com.aitechnologies.utripod.uvis.viewModels

import androidx.lifecycle.*
import com.aitechnologies.utripod.models.PostComment
import com.aitechnologies.utripod.repository.UvisRepository
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UvisReplyCommentViewModel(
    private val uvisRepository: UvisRepository
) : ViewModel() {

    private val statusMessage = MutableLiveData<Event<Resource<List<PostComment>>>>()
    val comments: LiveData<Event<Resource<List<PostComment>>>> get() = statusMessage

    val isDeleted = MutableLiveData<Boolean>()

    @ExperimentalCoroutinesApi
    fun getReplyComments(commentId: String) {
        viewModelScope.launch {
            uvisRepository.getReplyComments(commentId).collect {
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
            uvisRepository.addReplyComment(postComment, postId, isFirst)
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
                uvisRepository.deleteReplyComment(id, postId, previousCommentId, isFirst) != null
        }
    }

}

@Suppress("UNCHECKED_CAST")
class UvisReplyCommentViewModelProvider(
    private val uvisRepository: UvisRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UvisReplyCommentViewModel::class.java))
            return UvisReplyCommentViewModel(uvisRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel Class")
    }
}