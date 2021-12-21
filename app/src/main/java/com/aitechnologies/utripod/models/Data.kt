package com.aitechnologies.utripod.models

import java.io.Serializable

data class Data(
    val administrative_area: Any,
    val confidence: Any,
    val continent: String,
    val country: String,
    val country_code: String,
    val county: String,
    val label: String,
    val latitude: Double,
    val locality: String,
    val longitude: Double,
    val name: String,
    val neighbourhood: Any,
    val number: Any,
    val postal_code: Any,
    val region: String,
    val region_code: String,
    val street: Any,
    val type: String
) : Serializable