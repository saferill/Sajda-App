package com.sajda.app.data.api

import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Url

interface GithubUpdateApi {
    // The Constants.UPDATE_RELEASES_URL is absolute
    @GET
    suspend fun getLatestRelease(@Url url: String): JsonObject
}
