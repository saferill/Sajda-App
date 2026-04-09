package com.sajda.app.data.model

data class GithubRelease(
    val tag_name: String,
    val name: String,
    val body: String,
    val html_url: String = "",
    val assets: List<GithubAsset>
)
