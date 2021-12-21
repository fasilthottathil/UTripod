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
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

class AddPostViewModel(
    private val postRepository: PostRepository,
    private val application: Application
) : ViewModel() {

    val validate = MutableLiveData<Event<Resource<Response>>>()

    val message = MutableLiveData<Event<Resource<Response>>>()

    val posts = Posts()

    fun addPost() {
        viewModelScope.launch {
            message.value = Event(Resource("Loading", Response(2)))
            posts.timestamp = Timestamp.now()
            if (postRepository.addPost(posts) != null)
                message.value = Event(Resource("Added", Response(1)))
            else
                message.value = Event(Resource("An error occurred", Response(0)))
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
class AddPostProviderFactory(
    private val postRepository: PostRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddPostViewModel::class.java))
            return AddPostViewModel(postRepository, application) as T
        throw IllegalArgumentException("UNKNOWN viewModel class")
    }
}