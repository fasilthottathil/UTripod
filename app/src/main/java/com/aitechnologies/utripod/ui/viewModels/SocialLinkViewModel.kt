package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.SocialLinks
import com.aitechnologies.utripod.repository.UserRepository
import kotlinx.coroutines.launch

class SocialLinkViewModel(private val userRepository: UserRepository) : ViewModel() {

    val isAdded = MutableLiveData<Boolean>()

    fun addSocialLinks(
        username: String,
        socialLinks: SocialLinks
    ) {
        viewModelScope.launch {
            userRepository.addSocialLinks(socialLinks, username)
            isAdded.value = true
        }
    }

}

@Suppress("UNCHECKED_CAST")
class SocialLinkProvider(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocialLinkViewModel::class.java))
            return SocialLinkViewModel(userRepository) as T
        throw IllegalArgumentException("unknown viewModel class")
    }
}