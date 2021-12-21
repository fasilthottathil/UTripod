package com.aitechnologies.utripod.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.Constants.CHANNEL_ID_OFFLINE
import com.aitechnologies.utripod.util.Constants.CONNECTION
import com.google.firebase.firestore.FirebaseFirestore

class OfflineService : Service() {

    private val firebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        showNotification()
    }

    private fun setOffline() {
        firebaseFirestore.collection(CONNECTION)
            .whereEqualTo("username", getUsername())
            .limit(1)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    firebaseFirestore.collection(CONNECTION)
                        .add(
                            mapOf(
                                "username" to getUsername(),
                                "isOnline" to false
                            )
                        )
                        .addOnSuccessListener {
                            stopForeground(true)
                            stopSelf()
                        }
                        .addOnFailureListener {
                            stopForeground(true)
                            stopSelf()
                        }
                } else {
                    firebaseFirestore.collection(CONNECTION)
                        .document(it.documents[0].id)
                        .update(mapOf("isOnline" to false))
                        .addOnSuccessListener {
                            stopForeground(true)
                            stopSelf()
                        }
                        .addOnFailureListener {
                            stopForeground(true)
                            stopSelf()
                        }
                }
            }
    }

    private fun showNotification() {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID_OFFLINE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Checking for new message")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSound(null)
            .build()
        startForeground(Int.MAX_VALUE, notificationBuilder)
        setOffline()
    }

}