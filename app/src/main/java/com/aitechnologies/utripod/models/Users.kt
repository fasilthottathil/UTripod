package com.aitechnologies.utripod.models

import java.io.Serializable

data class Users(
    var userId: String = "",
    var username: String = "",
    var name: String = "",
    var password: String = "",
    var profileUrl: String = "",
    var location: String = "",
    var region: String = "",
    var phone: String = "",
    var age: String = "10",
    var isVerified: Boolean = false,
    var isBlocked: Boolean = false,
    var gender: String = "",
    var bio: String = "",
    var followers: Int = 0,
    var following: Int = 0,
    var profession: String = ""
) : Serializable
