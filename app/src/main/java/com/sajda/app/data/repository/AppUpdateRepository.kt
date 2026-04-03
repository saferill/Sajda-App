package com.sajda.app.data.repository

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.google.gson.JsonParser
import com.sajda.app.BuildConfig
import com.sajda.app.domain.model.AppUpdateInfo
import com.sajda.app.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class UpdateDownloadRecord(
    val id: Long,
    val title: String,
    val status: Int
)

class AppUpdateRepository(private val appContext: Context) {

    private val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    suspend fun fetchLatestRelease(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        val connection = (URL(Constants.UPDATE_RELEASES_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Sajda-App/${BuildConfig.VERSION_NAME}")
        }

        runCatching {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) return@withContext null
            if (responseCode !in 200..299) return@withContext null

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JsonParser.parseString(body).asJsonObject
            val tagName = root.get("tag_name")?.asString?.removePrefix("v")?.trim().orEmpty()
            val releaseName = root.get("name")?.asString?.ifBlank { tagName } ?: tagName
            val notes = root.get("body")?.asString.orEmpty().trim()
            val publishedAt = root.get("published_at")?.asString.orEmpty()
            val releasePageUrl = root.get("html_url")?.asString ?: Constants.UPDATE_RELEASES_PAGE_URL
            val asset = root.getAsJsonArray("assets")
                ?.mapNotNull { element -> element.asJsonObject }
                ?.firstOrNull { assetObject ->
                    assetObject.get("name")?.asString?.endsWith(".apk", ignoreCase = true) == true
                }
                ?: return@withContext null

            val downloadUrl = asset.get("browser_download_url")?.asString.orEmpty()
            if (tagName.isBlank() || downloadUrl.isBlank()) return@withContext null
            if (compareVersionNames(tagName, BuildConfig.VERSION_NAME) <= 0) return@withContext null

            AppUpdateInfo(
                versionName = tagName,
                releaseName = releaseName,
                notes = notes,
                downloadUrl = downloadUrl,
                releasePageUrl = releasePageUrl,
                publishedAt = publishedAt
            )
        }.getOrNull().also {
            connection.disconnect()
        }
    }

    fun enqueueUpdateDownload(updateInfo: AppUpdateInfo): Long {
        val sanitizedVersion = updateInfo.versionName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val request = DownloadManager.Request(Uri.parse(updateInfo.downloadUrl))
            .setTitle(Constants.UPDATE_DOWNLOAD_TITLE)
            .setDescription("Version ${updateInfo.versionName}")
            .setMimeType("application/vnd.android.package-archive")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(
                appContext,
                Environment.DIRECTORY_DOWNLOADS,
                "sajda-update-$sanitizedVersion.apk"
            )

        return downloadManager.enqueue(request)
    }

    fun getDownloadRecord(downloadId: Long): UpdateDownloadRecord? {
        val query = DownloadManager.Query().setFilterById(downloadId)
        downloadManager.query(query)?.use { cursor ->
            if (!cursor.moveToFirst()) return null
            val title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE)).orEmpty()
            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            return UpdateDownloadRecord(downloadId, title, status)
        }
        return null
    }

    fun buildInstallIntent(downloadId: Long): Intent? {
        val uri = downloadManager.getUriForDownloadedFile(downloadId) ?: return null
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun buildUnknownSourcesIntent(): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null
        return Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = Uri.parse("package:${appContext.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun canRequestPackageInstalls(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O || appContext.packageManager.canRequestPackageInstalls()
    }

    fun buildReleasePageIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(Constants.UPDATE_RELEASES_PAGE_URL)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun compareVersionNames(remote: String, current: String): Int {
        val remoteParts = remote.trim().split('.', '-', '_').mapNotNull { it.toIntOrNull() }
        val currentParts = current.trim().split('.', '-', '_').mapNotNull { it.toIntOrNull() }
        val maxSize = maxOf(remoteParts.size, currentParts.size)
        for (index in 0 until maxSize) {
            val remoteValue = remoteParts.getOrElse(index) { 0 }
            val currentValue = currentParts.getOrElse(index) { 0 }
            if (remoteValue != currentValue) {
                return remoteValue.compareTo(currentValue)
            }
        }
        return remote.compareTo(current)
    }
}
