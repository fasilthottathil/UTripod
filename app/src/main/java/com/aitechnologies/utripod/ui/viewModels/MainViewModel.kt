package com.aitechnologies.utripod.ui.viewModels

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.Constants.CONNECTION
import com.aitechnologies.utripod.util.Constants.USERS
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainViewModel(
    private val application: Application
) : ViewModel() {
    private val firebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val _blocked = MutableLiveData<Boolean>()
    val blocked get() = _blocked

    init {
        checkBlocked()
    }

    fun setOnline() {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseFirestore.collection(CONNECTION)
                .whereEqualTo("username", application.getUsername())
                .limit(1)
                .get()
                .addOnSuccessListener {
                    if (it.isEmpty) {
                        firebaseFirestore.collection(CONNECTION)
                            .add(
                                mapOf(
                                    "username" to application.getUsername(),
                                    "isOnline" to true
                                )
                            )
                    } else {
                        firebaseFirestore.collection(CONNECTION)
                            .document(it.documents[0].id)
                            .update(mapOf("isOnline" to true))
                    }
                }
        }
    }

    private fun checkBlocked() {
        viewModelScope.launch {
            firebaseFirestore.collection(USERS)
                .whereEqualTo("username", application.getUsername())
                .limit(1)
                .get()
                .addOnSuccessListener {
                    _blocked.value = it.toObjects(Users::class.java)[0].isBlocked
                }
        }
    }

}

@Suppress("UNCHECKED_CAST")
class MainProvider(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java))
            return MainViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}