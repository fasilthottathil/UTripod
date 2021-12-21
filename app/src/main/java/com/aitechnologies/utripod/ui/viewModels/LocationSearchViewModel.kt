package com.aitechnologies.utripod.ui.viewModels

import androidx.lifecycle.*
import com.aitechnologies.utripod.interfaces.LocationResponse
import com.aitechnologies.utripod.models.Location
import com.aitechnologies.utripod.util.Event
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class LocationSearchViewModel(
    private val locationResponse: LocationResponse
) : ViewModel() {

    val location = MutableLiveData<Location>()

    private val statusMessage = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> get() = statusMessage

    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        statusMessage.value = Event(exception.message.toString())
    }

    fun searchLocation(query: String) {
        viewModelScope.launch(errorHandler) {
            val response = locationResponse.searchLocation(
                "b56de8ac88cdf18d5d51bab036d7f140",
                query
            )

            if (response.isSuccessful) {
                response.body().let {
                    if (it != null)
                        location.value = it
                }
            } else {
                statusMessage.value = Event(response.errorBody().toString())
            }
        }
    }


}

@Suppress("UNCHECKED_cAST")
class LocationSearchProvider(
    private val locationResponse: LocationResponse
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationSearchViewModel::class.java))
            return LocationSearchViewModel(locationResponse) as T
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}