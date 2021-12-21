package com.aitechnologies.utripod.models

import com.google.firebase.Timestamp

data class PostComment(
    var id: String = "",
    var postId: String = "",
    var comment: String = "",
    var username: String = "",
    var replies: Int = 0,
    var timestamp: Timestamp = Timestamp.now(),
    var viewType: Int = 0
)