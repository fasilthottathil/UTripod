package com.aitechnologies.utripod.models

import com.google.firebase.Timestamp

data class PostPromotion(
    var pId: String = System.currentTimeMillis().toString(),
    var id: String = "",
    var username: String = "",
    var profileUrl: String = "",
    var post: String = "",
    var likes: Int = 0,
    var comments: Int = 0,
    var shares: Int = 0,
    var type: Int = 0,
    var description: String = "",
    var hashTags: String? = "",
    var tags: String? = "",
    var likesList: String? = "",
    var isPublic: Boolean = true,
    var timestamp: Timestamp = Timestamp.now(),
    var isState: Boolean? = false,
    var region: String? = "",
    var profession:String = "",
    var toDate: Timestamp = Timestamp.now()
)
