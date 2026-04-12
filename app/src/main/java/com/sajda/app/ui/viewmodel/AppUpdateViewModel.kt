package com.sajda.app.ui.viewmodel

import android.app.DownloadManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajda.app.BuildConfig
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.AppUpdateRepository
import com.sajda.app.domain.model.AppUpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppUpdateUiState(
    val currentVersionName: String = BuildConfig.VERSION_NAME,
    val latestVersionName: String = "",
    val releaseName: String = "",
    val notes: String = "",
    val publishedAt: String = "",
    val releasePageUrl: String = "",
    val isChecking: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadId: Long = 0L,
    val canInstallDownloadedUpdate: Boolean = false,
    val errorMessage: String? = null,
    val lastCheckedAt: String = ""
) {
    val hasUpdate: Boolean
        get() = latestVersionName.isNotBlank()
}

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val appUpdateRepository: AppUpdateRepository
) : ViewModel() {

    private val _appUpdateState = MutableStateFlow(AppUpdateUiState())
    val appUpdateState = _appUpdateState.asStateFlow()

    private var cachedUpdateInfo: AppUpdateInfo? = null

    init {
        viewModelScope.launch {
            runCatching {
                val latestSettings = preferencesDataStore.settingsFlow.first()
                _appUpdateState.update { current ->
                    current.copy(lastCheckedAt = latestSettings.lastUpdateCheckAt)
                }
            }.onFailure {
                _appUpdateState.update { current ->
                    current.copy(errorMessage = it.message ?: "Gagal memuat pengaturan update")
                }
            }
        }
    }

    fun checkForUpdates(silent: Boolean = false) {
        viewModelScope.launch {
            _appUpdateState.update {
                it.copy(
                    isChecking = true,
                    isDownloading = false,
                    downloadId = 0L,
                    canInstallDownloadedUpdate = false,
                    errorMessage = null
                )
            }
            val updateInfo = appUpdateRepository.fetchLatestRelease()
            preferencesDataStore.setLastUpdateCheckAt()
            cachedUpdateInfo = updateInfo
            _appUpdateState.update { current ->
                when (updateInfo) {
                    null -> current.copy(
                        latestVersionName = "",
                        releaseName = "",
                        notes = "",
                        publishedAt = "",
                        releasePageUrl = "",
                        isChecking = false,
                        isDownloading = false,
                        downloadId = 0L,
                        canInstallDownloadedUpdate = false,
                        errorMessage = if (silent) null else "Belum ada versi publik yang lebih baru saat ini.",
                        lastCheckedAt = preferencesDataStore.settingsFlow.first().lastUpdateCheckAt
                    )

                    else -> current.copy(
                        latestVersionName = updateInfo.versionName,
                        releaseName = updateInfo.releaseName,
                        notes = updateInfo.notes,
                        publishedAt = updateInfo.publishedAt,
                        releasePageUrl = updateInfo.releasePageUrl,
                        isChecking = false,
                        isDownloading = false,
                        downloadId = 0L,
                        canInstallDownloadedUpdate = false,
                        errorMessage = null,
                        lastCheckedAt = preferencesDataStore.settingsFlow.first().lastUpdateCheckAt
                    )
                }
            }
        }
    }

    fun downloadLatestUpdate() {
        val updateInfo = cachedUpdateInfo ?: run {
            _appUpdateState.update {
                it.copy(errorMessage = "Cek update dulu sebelum mengunduh.")
            }
            return
        }
        viewModelScope.launch {
            _appUpdateState.update {
                it.copy(
                    isDownloading = true,
                    canInstallDownloadedUpdate = false,
                    errorMessage = "Mengunduh update..."
                )
            }
            val downloadId = appUpdateRepository.enqueueUpdateDownload(updateInfo)
            preferencesDataStore.setLastUpdateDownloadId(downloadId)
            _appUpdateState.update { it.copy(downloadId = downloadId) }
            waitForUpdateDownload(downloadId)
        }
    }

    fun installDownloadedUpdate() {
        viewModelScope.launch {
            val downloadId = _appUpdateState.value.downloadId
            if (downloadId <= 0L) {
                _appUpdateState.update { it.copy(errorMessage = "File update belum siap dipasang.") }
                return@launch
            }

            val startedInstaller = appUpdateRepository.installDownloadedUpdate(downloadId)
            if (!startedInstaller) {
                _appUpdateState.update {
                    it.copy(errorMessage = "Izinkan pemasangan dari NurApp, lalu tekan Pasang sekarang lagi.")
                }
            }
        }
    }

    private suspend fun waitForUpdateDownload(downloadId: Long) {
        repeat(DOWNLOAD_STATUS_POLL_LIMIT) {
            val record = appUpdateRepository.getDownloadRecord(downloadId)
            when (record?.status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    _appUpdateState.update {
                        it.copy(
                            isDownloading = false,
                            canInstallDownloadedUpdate = true,
                            errorMessage = "Download selesai. Tekan Pasang sekarang."
                        )
                    }
                    return
                }

                DownloadManager.STATUS_FAILED -> {
                    _appUpdateState.update {
                        it.copy(
                            isDownloading = false,
                            canInstallDownloadedUpdate = false,
                            errorMessage = "Download update gagal. Coba unduh ulang."
                        )
                    }
                    return
                }

                DownloadManager.STATUS_PAUSED -> {
                    _appUpdateState.update {
                        it.copy(errorMessage = "Download dijeda oleh sistem.")
                    }
                }

                DownloadManager.STATUS_PENDING,
                DownloadManager.STATUS_RUNNING,
                null -> {
                    _appUpdateState.update {
                        it.copy(errorMessage = "Mengunduh update...")
                    }
                }
            }
            delay(DOWNLOAD_STATUS_POLL_INTERVAL_MS)
        }

        _appUpdateState.update {
            it.copy(
                isDownloading = false,
                canInstallDownloadedUpdate = false,
                errorMessage = "Download terlalu lama. Cek notifikasi download atau coba lagi."
            )
        }
    }

    companion object {
        private const val DOWNLOAD_STATUS_POLL_LIMIT = 900
        private const val DOWNLOAD_STATUS_POLL_INTERVAL_MS = 1_000L
    }
}
