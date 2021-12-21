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
import kotlinx.coroutines.launch

class EditGroupViewModel(
    private val chatsRepository: ChatsRepository,
    private val application: Application
) : ViewModel() {

    var groups = Groups()
    val validate = MutableLiveData<Event<Resource<Response>>>()
    val isUpdated = MutableLiveData<Boolean>()

    fun editGroup(roomId: String) {
        viewModelScope.launch {
            isUpdated.value = chatsRepository.editGroup(roomId, groups) != null
        }
    }

    fun validate() {
        if (groups.name.isEmpty() || groups.name.isBlank()) {
            validate.value = Event(Resource("Invalid name", Response(0)))
        } else if (groups.description.isEmpty() || groups.description.isBlank()) {
            validate.value = Event(Resource("Invalid description", Response(1)))
        } else if (!application.isConnected()) {
            validate.value = Event(Resource("No connection", Response(2)))
        } else {
            validate.value = Event(Resource("Valid", Response(3)))
        }
    }

}

@Suppress("UNCHECKED_CAST")
class EditGroupViewModelProvider(
    private val chatsRepository: ChatsRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditGroupViewModel::class.java))
            return EditGroupViewModel(chatsRepository, application) as T
        throw IllegalArgumentException("UNKNOWN ViewModel class")
    }
}