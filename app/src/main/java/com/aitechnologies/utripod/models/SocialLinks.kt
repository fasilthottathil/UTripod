package com.aitechnologies.utripod.models

import java.io.Serializable

data class SocialLinks(
    var linkedin: String? = "",
    var youtube: String? = "",
    var twitter: String? = "",
    var fb: String? = "",
    var insta: String? = "",
) : Serializable
