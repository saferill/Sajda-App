package com.sajda.app.data.api

import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Url

interface EquranApi {
    @GET
    suspend fun getSurahDetail(@Url url: String): JsonObject

    @GET
    suspend fun getTafsir(@Url url: String): JsonObject
}
