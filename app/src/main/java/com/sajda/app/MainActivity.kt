package com.sajda.app

import android.os.Build
import android.os.Bundle
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.sajda.app.util.AppTranslations
import com.sajda.app.util.pick
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.viewModels
import androidx.work.WorkManager
import androidx.core.view.WindowCompat
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.AudioRepository
import com.sajda.app.data.repository.HadithRepository
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.data.repository.QuranRepository
import com.sajda.app.data.repository.TafsirRepository
import com.sajda.app.databinding.ActivityMainBinding
import com.sajda.app.domain.model.Ayat
import com.sajda.app.domain.model.Bookmark
import com.sajda.app.domain.model.QuranSearchResult
import com.sajda.app.domain.model.SearchResultType
import com.sajda.app.domain.model.Surah
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.service.AdzanManager
import com.sajda.app.service.AdhanPlaybackStore
import com.sajda.app.service.AdzanScheduler
import com.sajda.app.service.AudioPlaybackStore
import com.sajda.app.service.AudioService
import com.sajda.app.service.LocationWorker
import com.sajda.app.service.PrayerScheduleWorker
import com.sajda.app.ui.component.DockItem
import com.sajda.app.ui.component.FloatingDock
import com.sajda.app.ui.component.FloatingMiniPlayer
import com.sajda.app.ui.component.SajdaScreenBackground
import com.sajda.app.ui.component.AnimatedSajdaSplashOverlay
import com.sajda.app.ui.screen.AdhanSettingsScreen
import com.sajda.app.ui.screen.AudioManagementScreen
import com.sajda.app.ui.screen.BookmarksScreen
import com.sajda.app.ui.screen.DailyDuaScreen
import com.sajda.app.ui.screen.HadithSearchScreen
import com.sajda.app.ui.screen.FullAudioPlayerScreen
import com.sajda.app.ui.screen.HomeScreen
import com.sajda.app.ui.screen.IslamicCalendarScreen
import com.sajda.app.ui.screen.LanguageSettingsScreen
import com.sajda.app.ui.screen.LocationSettingsScreen
import com.sajda.app.ui.screen.ModernQuranScreen
import com.sajda.app.ui.screen.NurAppOnboardingScreen
import com.sajda.app.ui.screen.PermissionSetupScreen
import com.sajda.app.ui.screen.PrayerTimeScreen
import com.sajda.app.ui.screen.QiblaScreen
import com.sajda.app.ui.screen.RamadanDuaScreen
import com.sajda.app.ui.screen.RamadanModeScreen
import com.sajda.app.ui.screen.RamadanPracticesScreen
import com.sajda.app.ui.screen.SearchScreen
import com.sajda.app.ui.screen.SettingsScreen
import com.sajda.app.ui.screen.SmartReminderScreen
import com.sajda.app.ui.screen.TafsirScreen
import com.sajda.app.ui.screen.UpdateCenterScreen
import com.sajda.app.ui.screen.WeeklyPrayerScheduleScreen
import com.sajda.app.ui.theme.SajdaAppTheme
import com.sajda.app.util.AppLocaleManager
import com.sajda.app.ui.viewmodel.HomeViewModel
import com.sajda.app.ui.viewmodel.QuranViewModel
import com.sajda.app.ui.viewmodel.PrayerTimeViewModel
import com.sajda.app.ui.viewmodel.SettingsViewModel
import com.sajda.app.ui.viewmodel.SpiritualContentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

private enum class RootTab {
    HOME,
    QURAN,
    ADHAN,
    HADITH,
    RAMADAN,
    SETTINGS
}

private sealed interface OverlayDestination {
    data object Search : OverlayDestination
    data object Bookmarks : OverlayDestination
    data object AudioManager : OverlayDestination
    data object DailyDua : OverlayDestination
    data object Hadith : OverlayDestination
    data object Calendar : OverlayDestination
    data object RamadanPractices : OverlayDestination
    data object RamadanDua : OverlayDestination
    data object WeeklyPrayer : OverlayDestination
    data object SmartReminder : OverlayDestination
    data object Qibla : OverlayDestination
    data object AdhanSettings : OverlayDestination
    data object LocationSettings : OverlayDestination
    data object LanguageSettings : OverlayDestination
    data object UpdateCenter : OverlayDestination
    data object FullPlayer : OverlayDestination
    data class Tafsir(val surah: Surah, val ayat: Ayat) : OverlayDestination
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferencesDataStore: PreferencesDataStore
    @Inject lateinit var audioRepository: AudioRepository
    @Inject lateinit var quranRepository: QuranRepository
    @Inject lateinit var hadithRepository: HadithRepository
    @Inject lateinit var prayerTimeRepository: PrayerTimeRepository
    @Inject lateinit var tafsirRepository: TafsirRepository
    @Inject lateinit var adzanScheduler: AdzanScheduler

    private val requestedTabOrdinalState = mutableIntStateOf(RootTab.HOME.ordinal)
    private lateinit var binding: ActivityMainBinding

    private val homeViewModel: HomeViewModel by viewModels()
    private val quranViewModel: QuranViewModel by viewModels()
    private val prayerTimeViewModel: PrayerTimeViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val spiritualContentViewModel: SpiritualContentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedTabOrdinalState.intValue = resolveStartTab(intent).ordinal
        updateVolumeControlStream(intent)
        cancelAutomaticUpdateChecks()
        LocationWorker.enqueuePeriodic(this)

        lifecycleScope.launch {
            runCatching {
                quranRepository.seedIfNeeded(this@MainActivity)
                runCatching {
                    AdzanManager(this@MainActivity).checkAndUpdateLocation()
                }.onFailure { error ->
                    android.util.Log.e("MainActivity", "Gagal auto update lokasi adzan", error)
                }

                val refreshedSettings = preferencesDataStore.settingsFlow.first()
                val prayerTimes = prayerTimeRepository.getNextDaysPrayerTimes(30)
                    .ifEmpty { prayerTimeRepository.refreshPrayerTimes(refreshedSettings) }
                adzanScheduler.reschedule(prayerTimes, refreshedSettings)
                PrayerScheduleWorker.enqueueImmediate(this@MainActivity)
                PrayerScheduleWorker.ensurePeriodic(this@MainActivity)
            }.onFailure { error ->
                android.util.Log.e("MainActivity", "Startup initialization failed", error)
            }
        }

        binding.composeHost.setContent {
            val settings by preferencesDataStore.settingsFlow.collectAsStateWithLifecycle(
                initialValue = UserSettings()
            )
            val context = LocalContext.current
            val view = LocalView.current
            val quranState by quranViewModel.uiState.collectAsStateWithLifecycle()
            val prayerState by prayerTimeViewModel.uiState.collectAsStateWithLifecycle()
            val settingsState by settingsViewModel.settings.collectAsStateWithLifecycle()
            val appUpdateState by settingsViewModel.appUpdateState.collectAsStateWithLifecycle()
            val spiritualState by spiritualContentViewModel.uiState.collectAsStateWithLifecycle()
            val duaBookmarks by preferencesDataStore.duaBookmarksFlow.collectAsStateWithLifecycle(initialValue = emptySet())
            val audioState by AudioPlaybackStore.state.collectAsStateWithLifecycle()
            val adhanPlaybackState by AdhanPlaybackStore.state.collectAsStateWithLifecycle()
            @Suppress("UNUSED_VARIABLE")
            val translationTick by AppTranslations.updates.collectAsStateWithLifecycle()

            var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
            var overlay by remember { mutableStateOf<OverlayDestination?>(null) }
            var showSplashOverlay by rememberSaveable { mutableStateOf(true) }
            val requestedTabOrdinal = requestedTabOrdinalState.intValue
            val playSelectedSurah: (Surah) -> Unit = { surah ->
                playSurahAudio(surah, settings.selectedQuranReciter)
            }
            val appLanguage = settingsState.appLanguage

            androidx.compose.runtime.LaunchedEffect(requestedTabOrdinal) {
                if (selectedTabIndex != requestedTabOrdinal) {
                    selectedTabIndex = requestedTabOrdinal
                }
            }

            androidx.compose.runtime.LaunchedEffect(Unit) {
                delay(900)
                showSplashOverlay = false
            }

            val currentTab = RootTab.entries[selectedTabIndex]
            val currentAudioSurah = quranState.surahList.firstOrNull { it.number == audioState.surahNumber }
            val downloadedSurahList = quranState.surahList.filter { it.downloadedReciterIds.isNotEmpty() }
            val currentAudioIndex = downloadedSurahList.indexOfFirst { it.number == audioState.surahNumber }
            val previousAudioSurah = downloadedSurahList.getOrNull(currentAudioIndex - 1)
            val nextAudioSurah = downloadedSurahList.getOrNull(currentAudioIndex + 1)

            BackHandler(enabled = overlay != null) {
                overlay = null
            }

            androidx.compose.runtime.LaunchedEffect(settings.appLanguage) {
                AppLocaleManager.apply(settings.appLanguage)
                spiritualContentViewModel.refresh(settings.appLanguage)
            }

            androidx.compose.runtime.LaunchedEffect(adhanPlaybackState.isActive) {
                volumeControlStream = if (adhanPlaybackState.isActive) {
                    android.media.AudioManager.STREAM_ALARM
                } else {
                    android.media.AudioManager.STREAM_MUSIC
                }
            }

            val isDarkTheme = settings.darkMode || settings.nightMode
            SajdaAppTheme(darkTheme = isDarkTheme) {
                val backgroundColor = MaterialTheme.colorScheme.background.toArgb()
                SideEffect {
                    val window = (context as Activity).window
                    window.statusBarColor = backgroundColor
                    window.navigationBarColor = backgroundColor
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
                    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDarkTheme
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SajdaScreenBackground {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val appSetupCompleted = settingsState.onboardingCompleted &&
                                settingsState.permissionSetupCompleted

                            if (!settingsState.onboardingCompleted) {
                                NurAppOnboardingScreen(
                                    settings = settingsState,
                                    viewModel = settingsViewModel,
                                    onFinish = { }
                                )
                            } else if (!settingsState.permissionSetupCompleted) {
                                PermissionSetupScreen(
                                    settings = settingsState,
                                    viewModel = settingsViewModel,
                                    onBack = {
                                        settingsViewModel.setOnboardingCompleted(false)
                                    },
                                    onContinue = {
                                        settingsViewModel.completePermissionSetup()
                                    }
                                )
                            } else if (overlay == null) {
                                when (currentTab) {
                                    RootTab.HOME -> HomeScreen(
                                        viewModel = homeViewModel,
                                        onNavigateToQuran = { selectedTabIndex = RootTab.QURAN.ordinal },
                                        onNavigateToPrayer = { selectedTabIndex = RootTab.ADHAN.ordinal },
                                        onOpenBookmarks = { overlay = OverlayDestination.Bookmarks },
                                        onOpenCalendar = { overlay = OverlayDestination.Calendar },
                                        onOpenQibla = { overlay = OverlayDestination.Qibla },
                                        onPlayLastAudio = playSelectedSurah
                                    )

                                    RootTab.QURAN -> ModernQuranScreen(
                                        viewModel = quranViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onPlayAudio = playSelectedSurah,
                                        onOpenBookmarks = { overlay = OverlayDestination.Bookmarks },
                                        onOpenSearch = { overlay = OverlayDestination.Search },
                                        onOpenTafsir = { surah, ayat ->
                                            overlay = OverlayDestination.Tafsir(surah, ayat)
                                        }
                                    )

                                    RootTab.ADHAN -> PrayerTimeScreen(
                                        viewModel = prayerTimeViewModel,
                                        onOpenWeeklySchedule = { overlay = OverlayDestination.WeeklyPrayer },
                                        onOpenQibla = { overlay = OverlayDestination.Qibla },
                                        onOpenLocationSettings = { overlay = OverlayDestination.LocationSettings }
                                    )

                                    RootTab.HADITH -> HadithSearchScreen(
                                        settings = settingsState,
                                        repository = hadithRepository
                                    )

                                    RootTab.RAMADAN -> RamadanModeScreen(
                                        settings = settingsState,
                                        prayerTime = prayerState.todayPrayerTime,
                                        onOpenPrayer = { selectedTabIndex = RootTab.ADHAN.ordinal },
                                        onOpenQuran = { selectedTabIndex = RootTab.QURAN.ordinal },
                                        onOpenPractices = { overlay = OverlayDestination.RamadanPractices },
                                        onOpenRamadanDua = { overlay = OverlayDestination.RamadanDua }
                                    )

                                    RootTab.SETTINGS -> SettingsScreen(
                                        viewModel = settingsViewModel,
                                        updateState = appUpdateState,
                                        onOpenAdhanSettings = { overlay = OverlayDestination.AdhanSettings },
                                        onOpenLocationSettings = { overlay = OverlayDestination.LocationSettings },
                                        onOpenLanguageSettings = { overlay = OverlayDestination.LanguageSettings },
                                        onOpenUpdateCenter = { overlay = OverlayDestination.UpdateCenter },
                                        onOpenAudioManagement = { overlay = OverlayDestination.AudioManager },
                                        onOpenSmartReminders = { overlay = OverlayDestination.SmartReminder },
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
                                            settings = settingsState,
                                            surahList = quranState.surahList,
                                            downloadStates = quranState.downloadStates,
                                            onBack = { overlay = null },
                                            onPlay = playSelectedSurah,
                                            onDelete = { quranViewModel.deleteAudio(it.number) },
                                            onDownload = quranViewModel::downloadAudio,
                                            onDeleteAll = quranViewModel::deleteAllAudio,
                                            onSetDownloadMode = settingsViewModel::setAudioDownloadMode,
                                            onSetWifiOnly = settingsViewModel::setWifiOnlyAudioDownloads
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

                                        OverlayDestination.Hadith -> HadithSearchScreen(
                                            settings = settingsState,
                                            repository = hadithRepository,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.Calendar -> IslamicCalendarScreen(
                                            appLanguage = settingsState.appLanguage,
                                            displayMode = settingsState.calendarDisplayMode,
                                            onDisplayModeChange = settingsViewModel::setCalendarDisplayMode,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.RamadanPractices -> RamadanPracticesScreen(
                                            appLanguage = settingsState.appLanguage,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.RamadanDua -> RamadanDuaScreen(
                                            appLanguage = settingsState.appLanguage,
                                            onBack = { overlay = null }
                                        )

                                        OverlayDestination.WeeklyPrayer -> WeeklyPrayerScheduleScreen(
                                            weeklyPrayerTimes = prayerState.weeklyPrayerTimes,
                                            monthlyPrayerTimes = prayerState.monthlyPrayerTimes,
                                            settings = prayerState.settings,
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

                                        OverlayDestination.AdhanSettings -> AdhanSettingsScreen(
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

                                        OverlayDestination.FullPlayer -> FullAudioPlayerScreen(
                                            appLanguage = settingsState.appLanguage,
                                            playbackState = audioState,
                                            currentSurah = currentAudioSurah,
                                            previousSurah = previousAudioSurah,
                                            nextSurah = nextAudioSurah,
                                            onBack = { overlay = null },
                                            onTogglePlayback = { togglePlayback(audioState) },
                                            onPrevious = { previousAudioSurah?.let(playSelectedSurah) },
                                            onNext = { nextAudioSurah?.let(playSelectedSurah) },
                                            onStop = { AudioService.stop(this@MainActivity) }
                                        )

                                        is OverlayDestination.Tafsir -> TafsirScreen(
                                            surah = destination.surah,
                                            ayat = destination.ayat,
                                            appLanguage = settingsState.appLanguage,
                                            tafsirRepository = tafsirRepository,
                                            onBack = { overlay = null }
                                        )

                                    null -> Unit
                                }
                            }

                            if (appSetupCompleted && overlay == null && audioState.isActive) {
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

                            if (appSetupCompleted && overlay == null) {
                                FloatingDock(
                                    items = listOf(
                                        DockItem(appLanguage.pick("Beranda", "Home"), Icons.Rounded.Home),
                                        DockItem(appLanguage.pick("Al-Qur'an", "Qur'an"), Icons.Rounded.MenuBook),
                                        DockItem(appLanguage.pick("Adzan", "Adhan"), Icons.Rounded.NotificationsActive),
                                        DockItem(appLanguage.pick("Hadist", "Hadith"), Icons.Rounded.HistoryEdu),
                                        DockItem(appLanguage.pick("Ramadhan", "Ramadan"), Icons.Rounded.Mosque),
                                        DockItem(appLanguage.pick("Pengaturan", "Settings"), Icons.Rounded.Settings)
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
                                modifier = Modifier.align(Alignment.Center)
                            )

                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedTabOrdinalState.intValue = resolveStartTab(intent).ordinal
        updateVolumeControlStream(intent)
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

    private fun playSurahAudio(
        surah: Surah,
        preferredReciter: com.sajda.app.domain.model.QuranReciter
    ) {
        val audioPath = audioRepository
            .resolveBestAudioFile(surah, preferredReciter)
            ?.absolutePath
            ?: return
        lifecycleScope.launch {
            preferencesDataStore.setLastPlayedSurah(surah.number)
        }
        AudioService.play(this, audioPath, surah.transliteration, surah.number)
    }

    private fun updateVolumeControlStream(intent: android.content.Intent?) {
        val isAdhan = intent?.getBooleanExtra("is_adhan", false) == true || AdhanPlaybackStore.state.value.isActive
        volumeControlStream = if (isAdhan) {
            android.media.AudioManager.STREAM_ALARM
        } else {
            android.media.AudioManager.STREAM_MUSIC
        }
        if (isAdhan) {
            intent?.removeExtra("is_adhan")
        }
    }

    private fun cancelAutomaticUpdateChecks() {
        runCatching {
            val workManager = WorkManager.getInstance(this)
            workManager.cancelUniqueWork(com.sajda.app.util.Constants.APP_UPDATE_WORK_NAME)
            workManager.cancelUniqueWork("${com.sajda.app.util.Constants.APP_UPDATE_WORK_NAME}_immediate")
        }.onFailure { error ->
            android.util.Log.e("MainActivity", "Gagal membatalkan update otomatis", error)
        }
    }

    private fun resolveStartTab(intent: android.content.Intent?): RootTab {
        return when (intent?.getStringExtra(com.sajda.app.util.Constants.EXTRA_OPEN_TAB)) {
            "quran" -> RootTab.QURAN
            "prayer" -> RootTab.ADHAN
            "hadith" -> RootTab.HADITH
            "ramadan" -> RootTab.RAMADAN
            "settings", "more" -> RootTab.SETTINGS
            else -> RootTab.HOME
        }
    }
}
