package com.aitechnologies.utripod.ui.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Groups
import com.aitechnologies.utripod.models.Response
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CreateGroupViewModel(
    private val chatsRepository: ChatsRepository,
    private val application: Application
) : ViewModel() {

    val isCreated = MutableLiveData<Boolean>()
    val validate = MutableLiveData<Event<Resource<Response>>>()

    var groups = Groups()


    @ExperimentalCoroutinesApi
    fun createGroup() {
        viewModelScope.launch {
            chatsRepository.createGroup(groups).collect {
                isCreated.value = it
            }
        }
    }

    fun validate() {
        if (groups.imageUrl.isEmpty()) {
            validate.value = Event(Resource("Select image", Response(0)))
        } else if (groups.name.isEmpty() || groups.name.isBlank()) {
            validate.value = Event(Resource("Invalid name", Response(1)))
        } else if (groups.description.isEmpty() || groups.description.isBlank()) {
            validate.value = Event(Resource("Invalid description", Response(2)))
        } else if (!application.isConnected()) {
            validate.value = Event(Resource("No connection", Response(3)))
        } else {
            validate.value = Event(Resource("Valid", Response(4)))
        }
    }

}

@Suppress("UNCHECKED_CAST")
class CreateGroupViewModelProvider(
    private val chatsRepository: ChatsRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateGroupViewModel::class.java))
            return CreateGroupViewModel(chatsRepository, application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}