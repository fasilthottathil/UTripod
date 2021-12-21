package com.aitechnologies.utripod.ui.viewModels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.Constants.CONNECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    private val application: Application
):ViewModel() {
    private val firebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun setOnline(){
        viewModelScope.launch (Dispatchers.IO){
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

}

@Suppress("UNCHECKED_CAST")
class MainProvider(
    private val application: Application
):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java))
            return MainViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}