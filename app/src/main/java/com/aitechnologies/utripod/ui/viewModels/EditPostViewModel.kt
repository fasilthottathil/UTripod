package com.aitechnologies.utripod.ui.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Posts
import com.aitechnologies.utripod.models.Response
import com.aitechnologies.utripod.repository.PostRepository
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.launch

class EditPostViewModel(
    private val postRepository: PostRepository,
    private val application: Application
) : ViewModel() {

    val validate = MutableLiveData<Event<Resource<Response>>>()

    val message = MutableLiveData<Event<Resource<Response>>>()

    var posts = Posts()

    val isUpdated = MutableLiveData<Boolean>()

    fun updatePost() {
        viewModelScope.launch {
            postRepository.updatePost(posts)
            isUpdated.value = true
        }
    }

    fun validate() {
        if (posts.type == 0) {
            if (posts.post?.isEmpty()!!) {
                validate.value = Event(Resource("Enter data", Response(0)))
                return
            }
        }
        if (!application.isConnected()) {
            validate.value = Event(Resource("No connection", Response(1)))
            return
        }
        validate.value = Event(Resource("Valid", Response(2)))
    }

}


@Suppress("UNCHECKED_CAST")
class EditPostViewModelProvider(
    private val postRepository: PostRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditPostViewModel::class.java))
            return EditPostViewModel(postRepository, application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}