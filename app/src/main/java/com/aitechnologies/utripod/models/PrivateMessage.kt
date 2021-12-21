package com.aitechnologies.utripod.models

import com.google.firebase.Timestamp

data class PrivateMessage(
    var id: String = System.currentTimeMillis().toString(),
    var username: String = "",
    var message: String = "",
    var type: Int = 0,
    var timestamp: Timestamp = Timestamp.now(),
    var viewType: Int = 0
)
