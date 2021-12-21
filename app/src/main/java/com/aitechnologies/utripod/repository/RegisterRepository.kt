package com.aitechnologies.utripod.repository

import com.aitechnologies.utripod.models.Response
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.Constants.USERS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RegisterRepository {

    companion object {
        const val AUTH_ERROR = 0
        const val REGISTER_SUCCESS = 1
        const val ALREADY_EXIST = 2
    }

    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun register(
        users: Users
    ): Response {
        if (firebaseAuth.currentUser == null) {
            val authResult = AppUtil.anonymousAuthentication(firebaseAuth)
            if (authResult == null || authResult.user == null)
                return Response(AUTH_ERROR)
        }
        val result = firebaseFirestore.collection(USERS)
            .whereEqualTo("username", users.username)
            .limit(1)
            .get()
            .await()
        return if (result.isEmpty) {
            firebaseFirestore.collection(USERS)
                .add(users)
                .await()
            Response(REGISTER_SUCCESS)
        } else {
            Response(ALREADY_EXIST)
        }
    }

}