package com.aitechnologies.utripod.uvis.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Response
import com.aitechnologies.utripod.models.Uvis
import com.aitechnologies.utripod.repository.UvisRepository
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.Event
import com.aitechnologies.utripod.util.Resource
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

class AddUvisViewModel(
    private val uvisRepository: UvisRepository,
    private val application: Application
) : ViewModel() {
    val validate = MutableLiveData<Event<Resource<Response>>>()

    val message = MutableLiveData<Event<Resource<Response>>>()

    val uvis = Uvis()

    fun addUvis() {
        viewModelScope.launch {
            message.value = Event(Resource("Loading", Response(2)))
            uvis.timestamp = Timestamp.now()
            if (uvisRepository.addUvis(uvis) != null)
                message.value = Event(Resource("Added", Response(1)))
            else
                message.value = Event(Resource("An error occurred", Response(0)))
        }
    }

    fun validate() {
        if (uvis.description?.isEmpty()!!) {
            validate.value = Event(Resource("Enter description", Response(0)))
            return
        }
        if (!application.isConnected()) {
            validate.value = Event(Resource("No connection", Response(1)))
            return
        }
        validate.value = Event(Resource("Valid", Response(2)))
    }
}

@Suppress("UNCHECKED_CAST")
class AddUvisViewModelProvider(
    private val uvisRepository: UvisRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddUvisViewModel::class.java))
            return AddUvisViewModel(uvisRepository, application) as T
        throw IllegalArgumentException("UNKNOWN ViewModel Class")
    }
}