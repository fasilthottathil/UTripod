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
import kotlinx.coroutines.launch

class EditUvisViewModel(
    private val uvisRepository: UvisRepository,
    private val application: Application
) : ViewModel() {

    val validate = MutableLiveData<Event<Resource<Response>>>()

    val message = MutableLiveData<Event<Resource<Response>>>()

    var uvis = Uvis()

    val isUpdated = MutableLiveData<Boolean>()

    fun updateUvis() {
        viewModelScope.launch {
            uvisRepository.updateUvis(uvis)
            isUpdated.value = true
        }
    }

    fun validate() {
        if (uvis.description?.isEmpty()!!) {
            validate.value = Event(Resource("Enter data", Response(0)))
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
class EditUvisViewModelProvider(
    private val uvisRepository: UvisRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditUvisViewModel::class.java))
            return EditUvisViewModel(uvisRepository, application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}