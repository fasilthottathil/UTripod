package com.aitechnologies.utripod.repository

import android.app.Application
import com.aitechnologies.utripod.models.Notification
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.Constants.MY_NOTIFICATIONS
import com.aitechnologies.utripod.util.NotificationUtil.Companion.sendNotification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class NotificationRepository(private val application: Application) {
    private val firebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun getNotifications(username: String): List<Notification> {
        return try {
            firebaseFirestore.collection(MY_NOTIFICATIONS)
                .document(username)
                .collection(MY_NOTIFICATIONS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Notification::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendNotification(
        notification: Notification,
        username: String
    ) {
        if (username != application.getUsername()) {
            firebaseFirestore.collection(MY_NOTIFICATIONS)
                .document(username)
                .collection(MY_NOTIFICATIONS)
                .add(notification)
                .await()

            application.sendNotification(
                username,
                notification.message
            )
        }
    }

    suspend fun sendNotification(
        username: String,
        message:String
    ) {
        if (username != application.getUsername()) {
            application.sendNotification(
                username,
                message
            )
        }
    }


}