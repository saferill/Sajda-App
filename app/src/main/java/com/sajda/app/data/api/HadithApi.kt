package com.sajda.app.data.api

import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface HadithApi {
    @GET
    suspend fun getHadithBook(
        @Url url: String,
        @Query("range") range: String? = null
    ): JsonObject
}
