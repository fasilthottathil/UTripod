package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Groups
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MyGroupsViewModel(
    private val chatsRepository: ChatsRepository
) : ViewModel() {

    val idList = MutableLiveData<Event<Resource<List<String>>>>()
    val groupsListener = MutableLiveData<Event<Resource<List<Groups>>>>()

    @ExperimentalCoroutinesApi
    fun getJoinedGroupsIdList(username: String) {
        viewModelScope.launch {
            chatsRepository.getJoinedGroupsIdList(username).collect {
                idList.value = it
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun getJoinedGroups(idList: List<String>) {
        viewModelScope.launch {
            chatsRepository.getJoinedGroups(idList).collect {
                groupsListener.value = it
            }
        }
    }

}

@Suppress("UNCHECKED_CAST")
class MyGroupsViewModelProvider(
    private val chatsRepository: ChatsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyGroupsViewModel::class.java))
            return MyGroupsViewModel(chatsRepository) as T
        throw IllegalArgumentException("UNKNOWN ViewModel Class")
    }
}