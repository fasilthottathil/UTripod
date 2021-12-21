package com.aitechnologies.utripod.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class UvisModel(
    var id: String? = "",
    var username: String? = "",
    var profileUrl: String? = "",
    var url: String? = "",
    var likes: Int? = 0,
    var comments: Int? = 0,
    var shares: Int? = 0,
    var description: String? = "",
    var hashTags: String? = "",
    var tags: String? = "",
    var likesList: String? = "",
    var isPublic: Boolean? = true,
    @ServerTimestamp
    var timestamp: Timestamp? = Timestamp.now(),
    var profession:String = "",
    var viewType: Int? = 0
)

