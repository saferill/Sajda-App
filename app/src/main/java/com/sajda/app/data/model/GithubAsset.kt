package com.sajda.app.data.model

data class GithubAsset(
    val name: String,
    val browser_download_url: String,
    val size: Long,
    val digest: String? = null
)
