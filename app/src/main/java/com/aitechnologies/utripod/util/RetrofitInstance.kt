package com.aitechnologies.utripod.util

import com.aitechnologies.utripod.interfaces.LocationResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        private const val LOCATION_URL = "http://api.positionstack.com"

        fun create(): LocationResponse {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(LOCATION_URL)
                .build()
            return retrofit.create(LocationResponse::class.java)
        }

    }
}