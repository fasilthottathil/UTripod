package com.aitechnologies.utripod.interfaces

import com.aitechnologies.utripod.models.Location
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationResponse {

    @GET("v1/forward?")
    suspend fun searchLocation(
        @Query("access_key")
        accessKey: String = "b56de8ac88cdf18d5d51bab036d7f140",
        @Query("query")
        query: String
    ): Response<Location>


}