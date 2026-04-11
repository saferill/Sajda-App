package com.sajda.app.ui.viewmodel

import android.app.DownloadManager
import com.sajda.app.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.AppUpdateRepository
import com.sajda.app.data.repository.BackupRepository
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.domain.model.AudioDownloadMode
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.AppUpdateInfo
import com.sajda.app.domain.model.AsrMadhhab
import com.sajda.app.domain.model.CalendarDisplayMode
import com.sajda.app.domain.model.CityPreset
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.PrayerCalculationMethod
import com.sajda.app.domain.model.QuranReciter
import com.sajda.app.domain.model.QuranReadingMode
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.service.AdzanScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
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

data class BackupUiState(
    val isBusy: Boolean = false,
    val message: String? = null,
    val lastFilePath: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val adzanScheduler: AdzanScheduler,
    private val appUpdateRepository: AppUpdateRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {

    val settings = preferencesDataStore.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserSettings()
    )

    private val _appUpdateState = MutableStateFlow(AppUpdateUiState())
    val appUpdateState = _appUpdateState.asStateFlow()

    private val _backupState = MutableStateFlow(BackupUiState())
    val backupState = _backupState.asStateFlow()

    private var cachedUpdateInfo: AppUpdateInfo? = null

    init {
        viewModelScope.launch {
            runCatching {
                settings.collect { latestSettings ->
                    _appUpdateState.update { current ->
                        current.copy(lastCheckedAt = latestSettings.lastUpdateCheckAt)
                    }
                }
            }.onFailure {
                _appUpdateState.update { current ->
                    current.copy(errorMessage = it.message ?: "Gagal memuat pengaturan update")
                }
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setDarkMode(enabled) }
    }

    fun setNightMode(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setNightMode(enabled) }
    }

    fun setFocusMode(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setFocusMode(enabled) }
    }

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch { preferencesDataStore.setAppLanguage(language) }
    }

    fun setShowTranslation(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setShowTranslation(enabled) }
    }

    fun setArabicOnly(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setArabicOnly(enabled) }
    }

    fun setShowTransliteration(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setShowTransliteration(enabled) }
    }

    fun setQuranReadingMode(mode: QuranReadingMode) {
        viewModelScope.launch { preferencesDataStore.setQuranReadingMode(mode) }
    }

    fun setArabicFontSize(size: Int) {
        viewModelScope.launch { preferencesDataStore.setArabicFontSize(size) }
    }

    fun setTranslationFontSize(size: Int) {
        viewModelScope.launch { preferencesDataStore.setTranslationFontSize(size) }
    }

    fun setVerseSpacing(spacing: Int) {
        viewModelScope.launch { preferencesDataStore.setVerseSpacing(spacing) }
    }

    fun setAdzanEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setAdzanEnabled(enabled)
            reschedulePrayerAlarms()
        }
    }

    fun setOverrideSilentMode(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setOverrideSilentMode(enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setVibrationEnabled(enabled) }
    }

    fun setAdzanSound(style: com.sajda.app.domain.model.AdhanStyle) {
        viewModelScope.launch { preferencesDataStore.setAdzanSound(style) }
    }

    fun setFajrAdzanSound(style: com.sajda.app.domain.model.AdhanStyle) {
        viewModelScope.launch { preferencesDataStore.setFajrAdzanSound(style) }
    }

    fun setSelectedQuranReciter(reciter: QuranReciter) {
        viewModelScope.launch { preferencesDataStore.setSelectedQuranReciter(reciter) }
    }

    fun setAudioDownloadMode(mode: AudioDownloadMode) {
        viewModelScope.launch { preferencesDataStore.setAudioDownloadMode(mode) }
    }

    fun setWifiOnlyAudioDownloads(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setWifiOnlyAudioDownloads(enabled) }
    }

    fun setCalendarDisplayMode(mode: CalendarDisplayMode) {
        viewModelScope.launch { preferencesDataStore.setCalendarDisplayMode(mode) }
    }

    fun setPrayerCalculationMethod(method: PrayerCalculationMethod) {
        viewModelScope.launch {
            preferencesDataStore.setPrayerCalculationMethod(method)
            reschedulePrayerAlarms()
        }
    }

    fun setAsrMadhhab(madhhab: AsrMadhhab) {
        viewModelScope.launch {
            preferencesDataStore.setAsrMadhhab(madhhab)
            reschedulePrayerAlarms()
        }
    }

    fun setAutoLocation(enabled: Boolean, refreshSchedule: Boolean = true) {
        viewModelScope.launch {
            preferencesDataStore.setAutoLocation(enabled)
            if (refreshSchedule) {
                reschedulePrayerAlarms()
            }
        }
    }

    fun setLocationPermissionPrompted(prompted: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setLocationPermissionPrompted(prompted)
        }
    }

    fun setLocation(cityPreset: CityPreset) {
        viewModelScope.launch {
            preferencesDataStore.updateLocation(
                locationName = cityPreset.displayName,
                latitude = cityPreset.latitude,
                longitude = cityPreset.longitude,
                automatic = false
            )
            reschedulePrayerAlarms()
        }
    }

    fun setCurrentLocation(locationName: String, latitude: Double, longitude: Double, automatic: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.updateLocation(
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                automatic = automatic
            )
            reschedulePrayerAlarms()
        }
    }

    fun toggleFavoriteLocation(locationName: String) {
        viewModelScope.launch {
            preferencesDataStore.toggleFavoriteLocation(locationName)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { preferencesDataStore.setOnboardingCompleted(true) }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch { preferencesDataStore.setOnboardingCompleted(completed) }
    }

    fun completePermissionSetup() {
        viewModelScope.launch { preferencesDataStore.setPermissionSetupCompleted(true) }
    }

    fun setPermissionSetupCompleted(completed: Boolean) {
        viewModelScope.launch { preferencesDataStore.setPermissionSetupCompleted(completed) }
    }

    fun setAdhanSnoozeMinutes(minutes: Int) {
        viewModelScope.launch {
            preferencesDataStore.setAdhanSnoozeMinutes(minutes)
        }
    }

    fun setAutoUpdateCheckEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setAutoUpdateCheckEnabled(enabled)
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

    fun exportBackup() {
        viewModelScope.launch {
            _backupState.value = BackupUiState(isBusy = true)
            val result = backupRepository.exportToDefaultFile()
            _backupState.value = result.fold(
                onSuccess = { file ->
                    BackupUiState(
                        isBusy = false,
                        message = "Backup tersimpan di ${file.name}",
                        lastFilePath = file.absolutePath
                    )
                },
                onFailure = { error ->
                    BackupUiState(
                        isBusy = false,
                        message = error.message ?: "Backup gagal dibuat"
                    )
                }
            )
        }
    }

    fun restoreBackup() {
        viewModelScope.launch {
            _backupState.value = BackupUiState(isBusy = true)
            val result = backupRepository.restoreFromDefaultFile()
            _backupState.value = result.fold(
                onSuccess = { file ->
                    BackupUiState(
                        isBusy = false,
                        message = "Backup ${file.name} berhasil dipulihkan",
                        lastFilePath = file.absolutePath
                    )
                },
                onFailure = { error ->
                    BackupUiState(
                        isBusy = false,
                        message = error.message ?: "Restore backup gagal"
                    )
                }
            )
            reschedulePrayerAlarms()
        }
    }

    fun setQuranReminder(enabled: Boolean, time: String? = null) {
        viewModelScope.launch { preferencesDataStore.setQuranReminder(enabled, time) }
    }

    fun setMorningDzikirReminder(enabled: Boolean, time: String? = null) {
        viewModelScope.launch { preferencesDataStore.setMorningDzikirReminder(enabled, time) }
    }

    fun setEveningDzikirReminder(enabled: Boolean, time: String? = null) {
        viewModelScope.launch { preferencesDataStore.setEveningDzikirReminder(enabled, time) }
    }

    fun setPrayerEnabled(prayerName: PrayerName, enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setPrayerEnabled(prayerName, enabled)
            reschedulePrayerAlarms()
        }
    }

    fun refreshPrayerSchedule() {
        viewModelScope.launch {
            reschedulePrayerAlarms()
        }
    }

    private suspend fun reschedulePrayerAlarms() {
        val latestSettings = preferencesDataStore.settingsFlow.first()
        val prayerTimes = prayerTimeRepository.refreshPrayerTimes(latestSettings)
        adzanScheduler.reschedule(prayerTimes, latestSettings)
    }

    companion object {
        private const val DOWNLOAD_STATUS_POLL_LIMIT = 900
        private const val DOWNLOAD_STATUS_POLL_INTERVAL_MS = 1_000L
    }
}
