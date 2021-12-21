package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.repository.UserRepository
import kotlinx.coroutines.launch

class SetBioAndImageViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val isAdded = MutableLiveData<Boolean>()

    fun setBioAndImage(
        username: String,
        profileUrl: String,
        bio: String
    ) {
        viewModelScope.launch {
            userRepository.setBioAndImage(username, profileUrl, bio)
            isAdded.value = true
        }
    }

}

@Suppress("UNCHECKED_CAST")
class SetBioAndImageProvider(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SetBioAndImageViewModel::class.java))
            return SetBioAndImageViewModel(userRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel class")
    }
}