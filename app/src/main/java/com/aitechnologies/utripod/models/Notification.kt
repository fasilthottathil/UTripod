package com.aitechnologies.utripod.models

import com.google.firebase.Timestamp

data class Notification(
    val notificationId: String = System.currentTimeMillis().toString(),
    var id: String = "",
    var message: String = "",
    var url: String = "",
    var timestamp: Timestamp = Timestamp.now(),
    var viewType: Int = 0
)