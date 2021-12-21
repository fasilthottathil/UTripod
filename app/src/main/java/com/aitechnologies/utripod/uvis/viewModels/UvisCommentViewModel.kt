package com.aitechnologies.utripod.uvis.viewModels

import androidx.lifecycle.*
import com.aitechnologies.utripod.models.PostComment
import com.aitechnologies.utripod.repository.UvisRepository
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UvisCommentViewModel(
    private val uvisRepository: UvisRepository
) : ViewModel() {

    private val statusMessage = MutableLiveData<Event<Resource<List<PostComment>>>>()
    val comments: LiveData<Event<Resource<List<PostComment>>>> get() = statusMessage

    var isDeleted = MutableLiveData<Boolean>()

    @ExperimentalCoroutinesApi
    fun getComments(postId: String) {
        viewModelScope.launch {
            uvisRepository.getComments(postId).collect {
                statusMessage.value = it
            }
        }
    }

    fun addComment(postComment: PostComment) {
        viewModelScope.launch {
            uvisRepository.addComment(postComment)
        }
    }

    fun deleteComment(
        id: String,
        postId: String
    ) {
        viewModelScope.launch {
            uvisRepository.deleteComment(id, postId)
            isDeleted.value = uvisRepository.deleteComment(id, postId) != null
        }
    }

}

@Suppress("UNCHECKED_CAST")
class UvisCommentViewModelProvider(
    private val uvisRepository: UvisRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UvisCommentViewModel::class.java))
            return UvisCommentViewModel(uvisRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel Class")
    }
}