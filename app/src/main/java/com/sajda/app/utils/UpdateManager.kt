package com.sajda.app.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.sajda.app.data.model.UpdateInfo
import com.sajda.app.data.remote.GithubApiService
import com.sajda.app.util.AppUpdateNotifier
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class UpdateManager(private val context: Context) {

    private val githubApiService: GithubApiService by lazy {
        Retrofit.Builder()
            .baseUrl(GITHUB_BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GithubApiService::class.java)
    }

    // Cek versi terbaru dari GitHub Releases.
    suspend fun checkForUpdate(): UpdateInfo? {
        return try {
            val release = githubApiService.getLatestRelease(GITHUB_OWNER, GITHUB_REPO)
            val latestVersion = release.tag_name.removePrefix("v").trim()
            val currentVersion = getCurrentVersion()
            val apkAsset = release.assets.firstOrNull { asset ->
                asset.name.endsWith(".apk", ignoreCase = true)
            }

            if (apkAsset == null) {
                Log.e(TAG, "Asset APK tidak ditemukan di GitHub Releases")
                AppUpdateNotifier.notifyReleaseAssetMissing(context)
                return null
            }

            if (!UpdateReleaseResolver.isNewerVersion(latestVersion, currentVersion)) {
                Log.d(TAG, "Versi terbaru sama atau lebih rendah, skip download")
                return null
            }

            UpdateInfo(
                latestVersion = latestVersion,
                currentVersion = currentVersion,
                downloadUrl = apkAsset.browser_download_url,
                releaseNotes = release.body,
                fileSize = apkAsset.size,
                releaseUrl = release.html_url,
                checksum = UpdateReleaseResolver.resolveChecksum(apkAsset, release.body)
            )
        } catch (error: Exception) {
            Log.e(TAG, "Gagal cek update", error)
            null
        }
    }

    // Ambil versionName dari aplikasi yang sedang terpasang.
    fun getCurrentVersion(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "0.0.0"
        } catch (error: Exception) {
            Log.e(TAG, "Gagal ambil versionName", error)
            "0.0.0"
        }
    }

    // Trigger installer Android native menggunakan FileProvider.
    fun installApk(apkPath: String) {
        try {
            val apkFile = File(apkPath)
            if (!apkFile.exists()) {
                Log.e(TAG, "File APK tidak ditemukan: $apkPath")
                return
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (error: Exception) {
            Log.e(TAG, "Gagal memasang APK", error)
        }
    }

    fun canRequestPackageInstalls(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()
    }

    fun openInstallPermissionSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    companion object {
        private const val TAG = "UpdateManager"
        private const val GITHUB_BASE_URL = "https://api.github.com/"
        private const val GITHUB_OWNER = "saferill"
        private const val GITHUB_REPO = "Nur-App"
    }
}
