package com.sajda.app.data.api

import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Url

interface DuaApi {
    @GET
    suspend fun getDuas(@Url url: String): JsonElement
}
