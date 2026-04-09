package com.sajda.app.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sajda.app.util.AppUpdateNotifier
import com.sajda.app.utils.UpdateReleaseResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val downloadUrl = inputData.getString(KEY_DOWNLOAD_URL)
        val expectedChecksum = inputData.getString(KEY_CHECKSUM)
        if (downloadUrl.isNullOrBlank()) {
            Log.e(TAG, "Download URL kosong")
            return@withContext Result.failure(errorData("Download URL kosong"))
        }

        try {
            val targetDir = applicationContext.getExternalFilesDir("Downloads")
                ?: return@withContext Result.failure(errorData("Folder download tidak tersedia"))
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val targetFile = File(targetDir, APK_FILE_NAME)
            val connection = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 20_000
                readTimeout = 20_000
                requestMethod = "GET"
                connect()
            }

            if (connection.responseCode !in 200..299) {
                Log.e(TAG, "HTTP error saat download: ${connection.responseCode}")
                return@withContext Result.failure(errorData("HTTP ${connection.responseCode}"))
            }

            val totalBytes = connection.contentLengthLong
            connection.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesCopied = 0L

                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break

                        output.write(buffer, 0, read)
                        bytesCopied += read

                        if (totalBytes > 0L) {
                            val progress = ((bytesCopied * 100) / totalBytes).toInt().coerceIn(0, 100)
                            setProgress(
                                Data.Builder()
                                    .putInt(KEY_PROGRESS, progress)
                                    .putString(KEY_STATUS, "Mengunduh update...")
                                    .build()
                            )
                        }
                    }
                    output.flush()
                }
            }

            if (!expectedChecksum.isNullOrBlank()) {
                val actualChecksum = UpdateReleaseResolver.sha256(targetFile)
                if (!actualChecksum.equals(expectedChecksum, ignoreCase = true)) {
                    Log.e(TAG, "Checksum APK tidak cocok. expected=$expectedChecksum actual=$actualChecksum")
                    targetFile.delete()
                    val message = "Checksum update tidak cocok"
                    if (runAttemptCount < MAX_RETRY_COUNT) {
                        return@withContext Result.retry()
                    }
                    AppUpdateNotifier.notifyUpdateDownloadFailed(applicationContext, message)
                    return@withContext Result.failure(errorData(message))
                }
            }

            Log.d(TAG, "Download update sukses: ${targetFile.absolutePath}")
            Result.success(
                Data.Builder()
                    .putString(KEY_APK_PATH, targetFile.absolutePath)
                    .putInt(KEY_PROGRESS, 100)
                    .putString(KEY_STATUS, "Download selesai")
                    .putString(KEY_CHECKSUM, expectedChecksum.orEmpty())
                    .build()
            )
        } catch (error: Exception) {
            Log.e(TAG, "Download update gagal", error)
            val message = error.message ?: "Download gagal"
            if (runAttemptCount < MAX_RETRY_COUNT) {
                return@withContext Result.retry()
            }
            AppUpdateNotifier.notifyUpdateDownloadFailed(applicationContext, message)
            Result.failure(errorData(message))
        }
    }

    private fun errorData(message: String): Data {
        return Data.Builder()
            .putString(KEY_ERROR, message)
            .putString(KEY_STATUS, message)
            .build()
    }

    companion object {
        private const val TAG = "DownloadUpdateWorker"
        private const val APK_FILE_NAME = "update.apk"
        const val UNIQUE_WORK_NAME = "download_update_worker"
        const val KEY_DOWNLOAD_URL = "download_url"
        const val KEY_PROGRESS = "progress"
        const val KEY_STATUS = "status"
        const val KEY_APK_PATH = "apk_path"
        const val KEY_ERROR = "error"
        const val KEY_CHECKSUM = "checksum"
        private const val MAX_RETRY_COUNT = 2

        fun enqueue(
            context: Context,
            downloadUrl: String,
            checksum: String? = null
        ): OneTimeWorkRequest {
            val request = OneTimeWorkRequestBuilder<DownloadUpdateWorker>()
                .setInputData(
                    Data.Builder()
                        .putString(KEY_DOWNLOAD_URL, downloadUrl)
                        .putString(KEY_CHECKSUM, checksum)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
            return request
        }
    }
}
