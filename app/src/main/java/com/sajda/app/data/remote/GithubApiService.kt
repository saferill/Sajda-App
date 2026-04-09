package com.sajda.app.data.remote

import com.sajda.app.data.model.GithubRelease
import retrofit2.http.GET
import retrofit2.http.Path

interface GithubApiService {

    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GithubRelease
}
