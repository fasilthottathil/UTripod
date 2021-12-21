package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.PostPromotion
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.models.UvisPromotion
import com.aitechnologies.utripod.repository.PostRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.repository.UvisRepository
import kotlinx.coroutines.launch

class PromotePostViewModel(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val uvisRepository: UvisRepository
) : ViewModel() {

    var users = MutableLiveData<List<Users>>()

    var isAdded = MutableLiveData<Boolean>()

    fun getUsersByRegion(region: String) {
        viewModelScope.launch {
            users.value = userRepository.getUsersByRegion(region)
        }
    }

    fun getUsersByLocation(location: String) {
        viewModelScope.launch {
            users.value = userRepository.getUsersByLocation(location)
        }
    }

    fun promotePost(promotion: PostPromotion) {
        viewModelScope.launch {
            isAdded.value = postRepository.promotePost(promotion) != null
        }
    }

    fun promoteUvis(promotion: UvisPromotion) {
        viewModelScope.launch {
            isAdded.value = uvisRepository.promoteUvis(promotion) != null
        }
    }

    fun getAllUsers() {
        viewModelScope.launch {
            users.value = userRepository.getAllUsers()
        }
    }



}

@Suppress("UNCHECKED_CAST")
class PromotePostViewModelProvider(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val uvisRepository: UvisRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PromotePostViewModel::class.java))
            return PromotePostViewModel(userRepository, postRepository, uvisRepository) as T
        throw IllegalArgumentException("Unknown viewModel class")
    }
}