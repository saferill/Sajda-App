package com.sajda.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.viewModels
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.data.repository.QuranRepository
import com.sajda.app.domain.model.Ayat
import com.sajda.app.domain.model.Bookmark
import com.sajda.app.domain.model.QuranSearchResult
import com.sajda.app.domain.model.SearchResultType
import com.sajda.app.domain.model.Surah
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.service.AdzanScheduler
import com.sajda.app.service.AppUpdateWorker
import com.sajda.app.service.AudioPlaybackStore
import com.sajda.app.service.AudioService
import com.sajda.app.service.PrayerScheduleWorker
import com.sajda.app.ui.component.AnimatedSajdaSplashOverlay
import com.sajda.app.ui.component.DockItem
import com.sajda.app.ui.component.FloatingDock
import com.sajda.app.ui.component.FloatingMiniPlayer
import com.sajda.app.ui.component.SajdaScreenBackground
import com.sajda.app.ui.screen.AdhanSettingsScreen
import com.sajda.app.ui.screen.AppearanceSettingsScreen
import com.sajda.app.ui.screen.AudioManagementScreen
import com.sajda.app.ui.screen.BackgroundAudioInfoScreen
import com.sajda.app.ui.screen.BookmarksScreen
import com.sajda.app.ui.screen.DailyDuaScreen
import com.sajda.app.ui.screen.EmptyStateScreen
import com.sajda.app.ui.screen.HadithLibraryScreen
import com.sajda.app.ui.screen.FullAudioPlayerScreen
import com.sajda.app.ui.screen.HomeScreen
import com.sajda.app.ui.screen.IslamicCalendarScreen
import com.sajda.app.ui.screen.LanguageSettingsScreen
import com.sajda.app.ui.screen.LocationSettingsScreen
import com.sajda.app.ui.screen.ModernQuranScreen
import com.sajda.app.ui.screen.MoreHubScreen
import com.sajda.app.ui.screen.NurAppOnboardingScreen
import com.sajda.app.ui.screen.PermissionSetupScreen
import com.sajda.app.ui.screen.PrayerTimeScreen
import com.sajda.app.ui.screen.QiblaScreen
import com.sajda.app.ui.screen.RamadanModeScreen
import com.sajda.app.ui.screen.SearchScreen
import com.sajda.app.ui.screen.SettingsScreen
import com.sajda.app.ui.screen.SmartReminderScreen
import com.sajda.app.ui.screen.TafsirScreen
import com.sajda.app.ui.screen.UpdateCenterScreen
import com.sajda.app.ui.screen.WeeklyPrayerScheduleScreen
import com.sajda.app.ui.screen.WidgetPreviewScreen
import com.sajda.app.ui.screen.WorshipProgressScreen
import com.sajda.app.ui.theme.SajdaAppTheme
import com.sajda.app.util.AdhanSystemHelper
import com.sajda.app.util.DeviceLocationHelper
import com.sajda.app.util.DeviceLocationResult
import com.sajda.app.util.pick
import com.sajda.app.ui.viewmodel.HomeViewModel
import com.sajda.app.ui.viewmodel.QuranViewModel
import com.sajda.app.ui.viewmodel.PrayerTimeViewModel
import com.sajda.app.ui.viewmodel.SettingsViewModel
import com.sajda.app.ui.viewmodel.SpiritualContentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private enum class RootTab {
    HOME,
    QURAN,
    PRAYER,
    QIBLA,
    MORE
}

private sealed interface OverlayDestination {
    data object Search : OverlayDestination
    data object Bookmarks : OverlayDestination
    data object AudioManager : OverlayDestination
    data object DailyDua : OverlayDestination
    data object Hadith : OverlayDestination
    data object Calendar : OverlayDestination
    data object Ramadan : OverlayDestination
    data object WeeklyPrayer : OverlayDestination
    data object WorshipProgress : OverlayDestination
    data object SmartReminder : OverlayDestination
    data object Qibla : OverlayDestination
    data object PermissionSetup : OverlayDestination
    data object Settings : OverlayDestination
    data object AdhanSettings : OverlayDestination
    data object AppearanceSettings : OverlayDestination
    data object LocationSettings : OverlayDestination
    data object LanguageSettings : OverlayDestination
    data object UpdateCenter : OverlayDestination
    data object BackgroundAudioInfo : OverlayDestination
    data object WidgetPreview : OverlayDestination
    data object EmptyState : OverlayDestination
    data object FullPlayer : OverlayDestination
    data class Tafsir(val surah: Surah, val ayat: Ayat) : OverlayDestination
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferencesDataStore: PreferencesDataStore
    @Inject lateinit var quranRepository: QuranRepository
    @Inject lateinit var prayerTimeRepository: PrayerTimeRepository
    @Inject lateinit var adzanScheduler: AdzanScheduler

    private val homeViewModel: HomeViewModel by viewModels()
    private val quranViewModel: QuranViewModel by viewModels()
    private val prayerTimeViewModel: PrayerTimeViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val spiritualContentViewModel: SpiritualContentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (intent?.getBooleanExtra("is_adhan", false) == true) {
            volumeControlStream = android.media.AudioManager.STREAM_ALARM
        } else {
            volumeControlStream = android.media.AudioManager.STREAM_MUSIC
        }

        lifecycleScope.launch {
            quranRepository.seedIfNeeded(this@MainActivity)
            val savedSettings = preferencesDataStore.settingsFlow.first()
            if (savedSettings.autoLocation && DeviceLocationHelper.hasLocationPermission(this@MainActivity)) {
                when (val locationResult = DeviceLocationHelper.getCurrentLocation(this@MainActivity)) {
                    is DeviceLocationResult.Success -> {
                        preferencesDataStore.updateLocation(
                            locationName = locationResult.location.label,
                            latitude = locationResult.location.latitude,
                            longitude = locationResult.location.longitude,
                            automatic = true
                        )
                    }

                    is DeviceLocationResult.Error -> Unit
                }
            }

            val refreshedSettings = preferencesDataStore.settingsFlow.first()
            val prayerTimes = prayerTimeRepository.refreshPrayerTimes(refreshedSettings)
            adzanScheduler.reschedule(prayerTimes, refreshedSettings)
            PrayerScheduleWorker.enqueueImmediate(this@MainActivity)
            PrayerScheduleWorker.ensurePeriodic(this@MainActivity)
            AppUpdateWorker.enqueueImmediate(this@MainActivity)
            AppUpdateWorker.ensurePeriodic(this@MainActivity)
        }

        setContent {
            val settings by preferencesDataStore.settingsFlow.collectAsStateWithLifecycle(
                initialValue = UserSettings()
            )
            val quranState by quranViewModel.uiState.collectAsStateWithLifecycle()
            val prayerState by prayerTimeViewModel.uiState.collectAsStateWithLifecycle()
            val settingsState by settingsViewModel.settings.collectAsStateWithLifecycle()
            val appUpdateState by settingsViewModel.appUpdateState.collectAsStateWithLifecycle()
            val spiritualState by spiritualContentViewModel.uiState.collectAsStateWithLifecycle()
            val duaBookmarks by preferencesDataStore.duaBookmarksFlow.collectAsStateWithLifecycle(initialValue = emptySet())
            val audioState by AudioPlaybackStore.state.collectAsStateWithLifecycle()

            var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
            var overlay by remember { mutableStateOf<OverlayDestination?>(null) }
            var notificationPermissionPrompted by rememberSaveable { mutableStateOf(false) }
            var showSplashOverlay by rememberSaveable { mutableStateOf(true) }
            val requestedTabOrdinal = resolveStartTab(intent).ordinal

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { notificationPermissionPrompted = true }

            androidx.compose.runtime.LaunchedEffect(requestedTabOrdinal) {
                if (selectedTabIndex != requestedTabOrdinal) {
                    selectedTabIndex = requestedTabOrdinal
                }
            }

            androidx.compose.runtime.LaunchedEffect(Unit) {
                delay(720)
                showSplashOverlay = false
            }

            val currentTab = RootTab.entries[selectedTabIndex]
            val currentAudioSurah = quranState.surahList.firstOrNull { it.number == audioState.surahNumber }
            val downloadedSurahList = quranState.surahList.filter { it.localAudioPath != null }
            val currentAudioIndex = downloadedSurahList.indexOfFirst { it.number == audioState.surahNumber }
            val previousAudioSurah = downloadedSurahList.getOrNull(currentAudioIndex - 1)
            val nextAudioSurah = downloadedSurahList.getOrNull(currentAudioIndex + 1)

            BackHandler(enabled = overlay != null) {
                overlay = null
            }

            androidx.compose.runtime.LaunchedEffect(settings.adzanEnabled) {
                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    settings.adzanEnabled &&
                    !notificationPermissionPrompted &&
                    !AdhanSystemHelper.hasNotificationPermission(this@MainActivity)
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            androidx.compose.runtime.LaunchedEffect(settings.appLanguage) {
                spiritualContentViewModel.refresh(settings.appLanguage)
            }

            SajdaAppTheme(darkTheme = settings.darkMode || settings.nightMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SajdaScreenBackground {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (!settingsState.onboardingCompleted) {
                                NurAppOnboardingScreen(
                                    settings = settingsState,
                                    viewModel = settingsViewModel,
                                    onFinish = { }
                                )
                            } else if (overlay == null) {
                                when (currentTab) {
                                    RootTab.HOME -> HomeScreen(
                                        viewModel = homeViewModel,
                                        onNavigateToQuran = { selectedTabIndex = RootTab.QURAN.ordinal },
                                        onNavigateToPrayer = { selectedTabIndex = RootTab.PRAYER.ordinal },
                                        onOpenBookmarks = { overlay = OverlayDestination.Bookmarks },
                                        onOpenHadith = { overlay = OverlayDestination.Hadith },
                                        onOpenCalendar = { overlay = OverlayDestination.Calendar },
                                        onOpenRamadan = { overlay = OverlayDestination.Ramadan },
                                        onOpenQibla = { selectedTabIndex = RootTab.QIBLA.ordinal },
                                        onOpenSearch = { overlay = OverlayDestination.Search },
                                        onOpenReminders = { overlay = OverlayDestination.SmartReminder },
                                        onOpenProgress = { overlay = OverlayDestination.WorshipProgress },
                                        onPlayLastAudio = ::playSurahAudio
                                    )

                                    RootTab.QURAN -> ModernQuranScreen(
                                        viewModel = quranViewModel,
                                        onPlayAudio = ::playSurahAudio,
                                        onOpenBookmarks = { overlay = OverlayDestination.Bookmarks },
                                        onOpenSearch = { overlay = OverlayDestination.Search },
                                        onOpenTafsir = { surah, ayat ->
                                            overlay = OverlayDestination.Tafsir(surah, ayat)
                                        }
                                    )

                                    RootTab.PRAYER -> PrayerTimeScreen(
                                        viewModel = prayerTimeViewModel,
                                        onOpenWeeklySchedule = { overlay = OverlayDestination.WeeklyPrayer },
                                        onOpenQibla = { selectedTabIndex = RootTab.QIBLA.ordinal },
                                        onOpenLocationSettings = { overlay = OverlayDestination.LocationSettings }
                                    )

                                    RootTab.QIBLA -> QiblaScreen(
                                        prayerTime = prayerState.todayPrayerTime,
                                        appLanguage = settingsState.appLanguage
                                    )

                                    RootTab.MORE -> MoreHubScreen(
                                        settings = settingsState,
                                        updateState = appUpdateState,
                                        onOpenHadith = { overlay = OverlayDestination.Hadith },
                                        onOpenCalendar = { overlay = OverlayDestination.Calendar },
                                        onOpenRamadhan = { overlay = OverlayDestination.Ramadan },
                                        onOpenBookmarks = { overlay = OverlayDestination.Bookmarks },
                                        onOpenSettings = { overlay = OverlayDestination.Settings },
                                        onOpenPermissionSetup = { overlay = OverlayDestination.PermissionSetup }
                                    )
                                }
                            } else {
                                when (val destination = overlay) {
                                        OverlayDestination.Search -> SearchScreen(
                                            appLanguage = settingsState.appLanguage,
                                            quranRepository = quranRepository,
                                            onBack = { overlay = null },
                                            onOpenResult = { result ->
                                                openSearchResult(result)
                                                overlay = null
                                                selectedTabIndex = RootTab.QURAN.ordinal
                                            }
                                        )

                                        OverlayDestination.Bookmarks -> BookmarksScreen(
                                            appLanguage = settingsState.appLanguage,
                                            quranRepository = quranRepository,
                                            bookmarks = quranState.bookmarks,
                                            onBack = { overlay = null },
                                            onOpenAyat = { bookmark ->
                                                openBookmark(bookmark)
                                                overlay = null
                                                selectedTabIndex = RootTab.QURAN.ordinal
                                            }
                                        )

                                        OverlayDestination.AudioManager -> AudioManagementScreen(
                                            appLanguage = settingsState.appLanguage,
                                            surahList = quranState.surahList,
                                            downloadStates = quranState.downloadStates,
                                            onBack = { overlay = null },
                                            onPlay = ::playSurahAudio,
                                            onDelete = { quranViewModel.deleteAudio(it.number) },
                                            onDownload = quranViewModel::downloadAudio
                                        )

                                        OverlayDestination.DailyDua -> DailyDuaScreen(
                                            settings = settingsState,
                                            spiritualState = spiritualState,
                                            onRefresh = { spiritualContentViewModel.refresh(settingsState.appLanguage) },
                                            bookmarkedIds = duaBookmarks,
                                            onBack = { overlay = null },
                                            onToggleBookmark = { duaId ->
                                                lifecycleScope.launch {
                                                    preferencesDataStore.toggleDuaBookmark(duaId)
                                                }
                                            }
                                        )

                                        OverlayDestination.Hadith -> HadithLibraryScreen(
                                            settings = settingsState,
                                            spiritualState = spiritualState,
                                            onRefresh = { spiritualContentViewModel.refresh(settingsState.appLanguage) },
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.Calendar -> IslamicCalendarScreen(
                                            appLanguage = settingsState.appLanguage,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.Ramadan -> RamadanModeScreen(
                                            settings = settingsState,
                                            prayerTime = prayerState.todayPrayerTime,
                                            onOpenPrayer = {
                                                selectedTabIndex = RootTab.PRAYER.ordinal
                                                overlay = null
                                            },
                                            onOpenQuran = {
                                                selectedTabIndex = RootTab.QURAN.ordinal
                                                overlay = null
                                            },
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.WeeklyPrayer -> WeeklyPrayerScheduleScreen(
                                            weeklyPrayerTimes = prayerState.weeklyPrayerTimes,
                                            monthlyPrayerTimes = prayerState.monthlyPrayerTimes,
                                            settings = prayerState.settings,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.WorshipProgress -> WorshipProgressScreen(
                                            settings = settingsState,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.SmartReminder -> SmartReminderScreen(
                                            settings = settingsState,
                                            viewModel = settingsViewModel,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.Qibla -> QiblaScreen(
                                            prayerTime = prayerState.todayPrayerTime,
                                            appLanguage = settingsState.appLanguage,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.PermissionSetup -> PermissionSetupScreen(
                                            settings = settingsState,
                                            viewModel = settingsViewModel,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.Settings -> SettingsScreen(
                                            viewModel = settingsViewModel,
                                            updateState = appUpdateState,
                                            onOpenAdhanSettings = { overlay = OverlayDestination.AdhanSettings },
                                            onOpenAppearanceSettings = { overlay = OverlayDestination.AppearanceSettings },
                                            onOpenLocationSettings = { overlay = OverlayDestination.LocationSettings },
                                            onOpenLanguageSettings = { overlay = OverlayDestination.LanguageSettings },
                                            onOpenUpdateCenter = { overlay = OverlayDestination.UpdateCenter },
                                            onOpenAudioManagement = { overlay = OverlayDestination.AudioManager },
                                            onOpenWorshipProgress = { overlay = OverlayDestination.WorshipProgress },
                                            onOpenSmartReminders = { overlay = OverlayDestination.SmartReminder },
                                            onOpenBackgroundAudioInfo = { overlay = OverlayDestination.BackgroundAudioInfo },
                                            onOpenWidgetPreview = { overlay = OverlayDestination.WidgetPreview },
                                            onOpenEmptyState = { overlay = OverlayDestination.EmptyState },
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.AdhanSettings -> AdhanSettingsScreen(
                                            settings = settingsState,
                                            viewModel = settingsViewModel,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.AppearanceSettings -> AppearanceSettingsScreen(
                                            settings = settingsState,
                                            viewModel = settingsViewModel,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.LocationSettings -> LocationSettingsScreen(
                                            settings = settingsState,
                                            viewModel = settingsViewModel,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.LanguageSettings -> LanguageSettingsScreen(
                                            settings = settingsState,
                                            viewModel = settingsViewModel,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.UpdateCenter -> UpdateCenterScreen(
                                            settings = settingsState,
                                            updateState = appUpdateState,
                                            viewModel = settingsViewModel,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.BackgroundAudioInfo -> BackgroundAudioInfoScreen(
                                            appLanguage = settingsState.appLanguage,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.WidgetPreview -> WidgetPreviewScreen(
                                            prayerTime = prayerState.todayPrayerTime,
                                            appLanguage = settingsState.appLanguage,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.EmptyState -> EmptyStateScreen(
                                            appLanguage = settingsState.appLanguage,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.FullPlayer -> FullAudioPlayerScreen(
                                            appLanguage = settingsState.appLanguage,
                                            playbackState = audioState,
                                            currentSurah = currentAudioSurah,
                                            previousSurah = previousAudioSurah,
                                            nextSurah = nextAudioSurah,
                                            onBack = { overlay = null },
                                            onTogglePlayback = { togglePlayback(audioState) },
                                            onPrevious = { previousAudioSurah?.let(::playSurahAudio) },
                                            onNext = { nextAudioSurah?.let(::playSurahAudio) },
                                            onStop = { AudioService.stop(this@MainActivity) }
                                        )

                                        is OverlayDestination.Tafsir -> TafsirScreen(
                                            surah = destination.surah,
                                            ayat = destination.ayat,
                                            appLanguage = settingsState.appLanguage,
                                            onBack = { overlay = null }
                                        )

                                    null -> Unit
                                }
                            }

                            if (settingsState.onboardingCompleted && overlay == null && audioState.isActive) {
                                FloatingMiniPlayer(
                                    playbackState = audioState,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(horizontal = 16.dp, vertical = 92.dp),
                                    onTogglePlayback = { togglePlayback(audioState) },
                                    onOpenPlayer = { overlay = OverlayDestination.FullPlayer },
                                    onStop = { AudioService.stop(this@MainActivity) }
                                )
                            }

                            if (settingsState.onboardingCompleted && overlay == null) {
                                FloatingDock(
                                    items = listOf(
                                        DockItem(settingsState.pick("Beranda", "Home"), Icons.Rounded.Home),
                                        DockItem("Qur'an", Icons.Rounded.MenuBook),
                                        DockItem(settingsState.pick("Adzan", "Adhan"), Icons.Rounded.NotificationsActive),
                                        DockItem(settingsState.pick("Kiblat", "Qibla"), Icons.Rounded.Explore),
                                        DockItem(settingsState.pick("Lainnya", "More"), Icons.Rounded.MoreHoriz)
                                    ),
                                    selectedIndex = selectedTabIndex,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)),
                                    onSelect = {
                                        selectedTabIndex = it
                                        overlay = null
                                    }
                                )
                            }

                            AnimatedSajdaSplashOverlay(
                                visible = showSplashOverlay,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun togglePlayback(audioState: com.sajda.app.domain.model.AudioPlaybackState) {
        if (audioState.isPlaying) {
            AudioService.pause(this)
        } else {
            AudioService.resume(this)
        }
    }

    private fun openBookmark(bookmark: Bookmark) {
        lifecycleScope.launch {
            quranRepository.getSurahByNumber(bookmark.surahNumber)?.let { surah ->
                quranViewModel.openSurah(surah)
                quranRepository.getAyat(bookmark.surahNumber, bookmark.ayatNumber)?.let { ayat ->
                    quranViewModel.recordLastRead(ayat)
                }
            }
        }
    }

    private fun openSearchResult(result: QuranSearchResult) {
        lifecycleScope.launch {
            quranRepository.getSurahByNumber(result.surahNumber)?.let { surah ->
                quranViewModel.openSurah(surah)
                if (result.type == SearchResultType.AYAT) {
                    result.ayatNumber?.let { ayatNumber ->
                        quranRepository.getAyat(result.surahNumber, ayatNumber)?.let { ayat ->
                            quranViewModel.recordLastRead(ayat)
                        }
                    }
                }
            }
        }
    }

    private fun playSurahAudio(surah: Surah) {
        val audioPath = surah.localAudioPath ?: return
        lifecycleScope.launch {
            preferencesDataStore.setLastPlayedSurah(surah.number)
        }
        AudioService.play(this, audioPath, surah.transliteration, surah.number)
    }

    private fun resolveStartTab(intent: android.content.Intent?): RootTab {
        return when (intent?.getStringExtra(com.sajda.app.util.Constants.EXTRA_OPEN_TAB)) {
            "quran" -> RootTab.QURAN
            "prayer" -> RootTab.PRAYER
            "qibla" -> RootTab.QIBLA
            "settings", "more" -> RootTab.MORE
            else -> RootTab.HOME
        }
    }
}
