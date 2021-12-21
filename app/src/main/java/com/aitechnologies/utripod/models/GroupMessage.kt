package com.aitechnologies.utripod.models

import com.google.firebase.Timestamp

data class GroupMessage(
    var id: String = System.currentTimeMillis().toString(),
    var username: String = "",
    var profileUrl: String = "",
    var message: String = "",
    var type: Int = 0,
    var timestamp: Timestamp = Timestamp.now(),
    var viewType: Int = 0
)
