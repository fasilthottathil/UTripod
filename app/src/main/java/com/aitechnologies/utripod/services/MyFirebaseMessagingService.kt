package com.aitechnologies.utripod.services

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.ui.activities.MainActivity
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.Constants.CHANNEL_ID
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {
    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        val intent = Intent(this, MainActivity::class.java)

        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .apply {
                setContentTitle("Utripod Notification")
                setContentText(remoteMessage.data["message"].toString())
                setSmallIcon(R.mipmap.ic_launcher)
                setContentIntent(pendingIntent)
                priority = NotificationCompat.PRIORITY_HIGH
            }.build()
        notificationManager.notify(Random.nextInt(3000), builder)

    }


    override fun onNewToken(p0: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(getUsername())
        super.onNewToken(p0)
    }
}