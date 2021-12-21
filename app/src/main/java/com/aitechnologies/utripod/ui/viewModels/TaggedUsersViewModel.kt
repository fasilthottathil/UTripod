package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Posts
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.PostRepository
import com.aitechnologies.utripod.repository.UserRepository
import kotlinx.coroutines.launch

class TaggedUsersViewModel(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
    ) : ViewModel() {

    val users = MutableLiveData<List<Users>>()
    val updated = MutableLiveData<Boolean>()

    fun getUsersByUsernameList(usernameList: List<String>) {
        viewModelScope.launch {
            users.value = userRepository.getUsersByUsernameList(usernameList)
        }
    }

    fun updateTaggedPost(
        posts: Posts,
        deleteList: ArrayList<String>
    ){
        viewModelScope.launch {
            postRepository.updateTaggedPost(posts, deleteList)
            updated.value = true
        }
    }

}

@Suppress("UNCHECKED_CAST")
class TaggedUsersViewModelProvider(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaggedUsersViewModel::class.java))
            return TaggedUsersViewModel(userRepository,postRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel Class")
    }
}