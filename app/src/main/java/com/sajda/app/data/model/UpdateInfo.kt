package com.sajda.app.data.model

data class UpdateInfo(
    val latestVersion: String,
    val currentVersion: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val fileSize: Long,
    val releaseUrl: String = "",
    val checksum: String? = null
)
