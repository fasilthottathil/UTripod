package com.aitechnologies.utripod.repository

import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.Constants.USERS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class LoginRepository {

    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun login(
        username: String,
    ): QuerySnapshot? {
        return if (firebaseAuth.currentUser == null) {
            val authResult = AppUtil.anonymousAuthentication(firebaseAuth)
            if (authResult != null && authResult.user != null) {
                getUser(username)
            } else null
        } else {
            getUser(username)
        }
    }

    private suspend fun getUser(username: String): QuerySnapshot? {
        return firebaseFirestore.collection(USERS)
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .await()
    }


}