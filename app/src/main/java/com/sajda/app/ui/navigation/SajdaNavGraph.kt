package com.sajda.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sajda.app.MainActivity
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.AudioRepository
import com.sajda.app.data.repository.HadithRepository
import com.sajda.app.data.repository.QuranRepository
import com.sajda.app.data.repository.TafsirRepository
import com.sajda.app.domain.model.Surah
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.domain.model.SearchResultType
import com.sajda.app.service.AdhanPlaybackStore
import com.sajda.app.service.AudioPlaybackStore
import com.sajda.app.service.AudioService
import com.sajda.app.ui.component.DockItem
import com.sajda.app.ui.component.FloatingDock
import com.sajda.app.ui.component.FloatingMiniPlayer
import com.sajda.app.ui.screen.*
import com.sajda.app.ui.viewmodel.*
import com.sajda.app.util.AppTranslations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SajdaNavGraph(
    startDestination: Screen,
    preferencesDataStore: PreferencesDataStore,
    quranRepository: QuranRepository,
    hadithRepository: HadithRepository,
    tafsirRepository: TafsirRepository,
    coroutineScope: CoroutineScope,
    audioRepository: AudioRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val settings by preferencesDataStore.settingsFlow.collectAsStateWithLifecycle(
        initialValue = UserSettings()
    )
    val appLanguage = settings.appLanguage
    @Suppress("UNUSED_VARIABLE")
    val translationTick by AppTranslations.updates.collectAsStateWithLifecycle()

    val homeViewModel: HomeViewModel = hiltViewModel()
    val quranViewModel: QuranViewModel = hiltViewModel()
    val prayerTimeViewModel: PrayerTimeViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val appUpdateViewModel: AppUpdateViewModel = hiltViewModel()
    val locationViewModel: LocationManagerViewModel = hiltViewModel()
    val spiritualContentViewModel: SpiritualContentViewModel = hiltViewModel()

    val quranState by quranViewModel.uiState.collectAsStateWithLifecycle()
    val prayerState by prayerTimeViewModel.uiState.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.settings.collectAsStateWithLifecycle()
    val appUpdateState by appUpdateViewModel.appUpdateState.collectAsStateWithLifecycle()
    val spiritualState by spiritualContentViewModel.uiState.collectAsStateWithLifecycle()
    val duaBookmarks by preferencesDataStore.duaBookmarksFlow.collectAsStateWithLifecycle(initialValue = emptySet())
    
    val audioState by AudioPlaybackStore.state.collectAsStateWithLifecycle()
    val adhanPlaybackState by AdhanPlaybackStore.state.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    
    val playSelectedSurah: (Surah) -> Unit = { surah ->
        val audioPath = audioRepository
            .resolveBestAudioFile(surah, settings.selectedQuranReciter)
            ?.absolutePath
        if (audioPath != null) {
            coroutineScope.launch {
                preferencesDataStore.setLastPlayedSurah(surah.number)
            }
            AudioService.play(context, audioPath, surah.transliteration, surah.number)
        }
    }

    val togglePlayback: () -> Unit = {
        if (audioState.isPlaying) {
            AudioService.pause(context)
        } else {
            AudioService.resume(context)
        }
    }

    val currentAudioSurah = quranState.surahList.firstOrNull { it.number == audioState.surahNumber }
    val downloadedSurahList = quranState.surahList.filter { it.downloadedReciterIds.isNotEmpty() }
    val currentAudioIndex = downloadedSurahList.indexOfFirst { it.number == audioState.surahNumber }
    val previousAudioSurah = downloadedSurahList.getOrNull(currentAudioIndex - 1)
    val nextAudioSurah = downloadedSurahList.getOrNull(currentAudioIndex + 1)

    val isTopLevelDestination = currentRoute in listOf(
        Screen.Home::class.qualifiedName,
        Screen.Quran::class.qualifiedName,
        Screen.Adhan::class.qualifiedName,
        Screen.Hadith::class.qualifiedName,
        Screen.Ramadan::class.qualifiedName,
        Screen.Settings::class.qualifiedName
    )

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            modifier = Modifier.fillMaxSize()
        ) {
            composable<Screen.Onboarding> {
                NurAppOnboardingScreen(
                    settings = settingsState,
                    viewModel = settingsViewModel,
                    onFinish = {
                        navController.navigate(Screen.PermissionSetup) {
                            popUpTo(Screen.Onboarding) { inclusive = true }
                        }
                    }
                )
            }

            composable<Screen.PermissionSetup> {
                PermissionSetupScreen(
                    settings = settingsState,
                    viewModel = settingsViewModel,
                    onBack = {
                        settingsViewModel.setOnboardingCompleted(false)
                        navController.popBackStack()
                    },
                    onContinue = {
                        settingsViewModel.completePermissionSetup()
                        navController.navigate(Screen.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable<Screen.Home> {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToQuran = { navController.navigate(Screen.Quran) },
                    onNavigateToPrayer = { navController.navigate(Screen.Adhan) },
                    onOpenBookmarks = { navController.navigate(Screen.Bookmarks) },
                    onOpenCalendar = { navController.navigate(Screen.Calendar) },
                    onOpenQibla = { navController.navigate(Screen.Qibla) },
                    onPlayLastAudio = playSelectedSurah
                )
            }

            composable<Screen.Quran> {
                ModernQuranScreen(
                    viewModel = quranViewModel,
                    settingsViewModel = settingsViewModel,
                    onPlayAudio = playSelectedSurah,
                    onOpenBookmarks = { navController.navigate(Screen.Bookmarks) },
                    onOpenSearch = { navController.navigate(Screen.Search) },
                    onOpenTafsir = { surah, ayat ->
                        navController.navigate(Screen.Tafsir(surah.number, ayat.ayatNumber))
                    }
                )
            }

            composable<Screen.Adhan> {
                PrayerTimeScreen(
                    viewModel = prayerTimeViewModel,
                    onOpenWeeklySchedule = { navController.navigate(Screen.WeeklyPrayer) },
                    onOpenQibla = { navController.navigate(Screen.Qibla) },
                    onOpenLocationSettings = { navController.navigate(Screen.LocationSettings) }
                )
            }

            composable<Screen.Hadith> {
                HadithSearchScreen(
                    settings = settingsState,
                    repository = hadithRepository
                )
            }

            composable<Screen.Ramadan> {
                RamadanModeScreen(
                    settings = settingsState,
                    prayerTime = prayerState.todayPrayerTime,
                    onOpenPrayer = { navController.navigate(Screen.Adhan) },
                    onOpenQuran = { navController.navigate(Screen.Quran) },
                    onOpenPractices = { navController.navigate(Screen.RamadanPractices) },
                    onOpenRamadanDua = { navController.navigate(Screen.RamadanDua) }
                )
            }

            composable<Screen.Settings> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    updateState = appUpdateState,
                    onOpenAdhanSettings = { navController.navigate(Screen.AdhanSettings) },
                    onOpenLocationSettings = { navController.navigate(Screen.LocationSettings) },
                    onOpenLanguageSettings = { navController.navigate(Screen.LanguageSettings) },
                    onOpenUpdateCenter = { navController.navigate(Screen.UpdateCenter) },
                    onOpenAudioManagement = { navController.navigate(Screen.AudioManager) },
                    onOpenSmartReminders = { navController.navigate(Screen.SmartReminder) }
                )
            }

            composable<Screen.Search> {
                SearchScreen(
                    appLanguage = appLanguage,
                    quranRepository = quranRepository,
                    onBack = { navController.popBackStack() },
                    onOpenResult = { result ->
                        coroutineScope.launch {
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
                        navController.navigate(Screen.Quran) {
                            popUpTo(Screen.Home)
                        }
                    }
                )
            }

            composable<Screen.Bookmarks> {
                BookmarksScreen(
                    appLanguage = appLanguage,
                    quranRepository = quranRepository,
                    bookmarks = quranState.bookmarks,
                    onBack = { navController.popBackStack() },
                    onOpenAyat = { bookmark ->
                        coroutineScope.launch {
                            quranRepository.getSurahByNumber(bookmark.surahNumber)?.let { surah ->
                                quranViewModel.openSurah(surah)
                                quranRepository.getAyat(bookmark.surahNumber, bookmark.ayatNumber)?.let { ayat ->
                                    quranViewModel.recordLastRead(ayat)
                                }
                            }
                        }
                        navController.navigate(Screen.Quran) {
                            popUpTo(Screen.Home)
                        }
                    }
                )
            }

            composable<Screen.AudioManager> {
                AudioManagementScreen(
                    appLanguage = appLanguage,
                    settings = settingsState,
                    surahList = quranState.surahList,
                    downloadStates = quranState.downloadStates,
                    onBack = { navController.popBackStack() },
                    onPlay = playSelectedSurah,
                    onDelete = { quranViewModel.deleteAudio(it.number) },
                    onDownload = quranViewModel::downloadAudio,
                    onDeleteAll = quranViewModel::deleteAllAudio,
                    onSetDownloadMode = settingsViewModel::setAudioDownloadMode,
                    onSetWifiOnly = settingsViewModel::setWifiOnlyAudioDownloads
                )
            }

            composable<Screen.DailyDua> {
                DailyDuaScreen(
                    settings = settingsState,
                    spiritualState = spiritualState,
                    onRefresh = { spiritualContentViewModel.refresh(appLanguage) },
                    bookmarkedIds = duaBookmarks,
                    onBack = { navController.popBackStack() },
                    onToggleBookmark = { duaId ->
                        coroutineScope.launch {
                            preferencesDataStore.toggleDuaBookmark(duaId)
                        }
                    }
                )
            }

            composable<Screen.Calendar> {
                IslamicCalendarScreen(
                    appLanguage = appLanguage,
                    displayMode = settingsState.calendarDisplayMode,
                    onDisplayModeChange = settingsViewModel::setCalendarDisplayMode,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Screen.RamadanPractices> {
                RamadanPracticesScreen(
                    appLanguage = appLanguage,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Screen.RamadanDua> {
                RamadanDuaScreen(
                    appLanguage = appLanguage,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Screen.WeeklyPrayer> {
                WeeklyPrayerScheduleScreen(
                    weeklyPrayerTimes = prayerState.weeklyPrayerTimes,
                    monthlyPrayerTimes = prayerState.monthlyPrayerTimes,
                    settings = prayerState.settings,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Screen.SmartReminder> {
                SmartReminderScreen(
                    settings = settingsState,
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Screen.Qibla> {
                QiblaScreen(
                    prayerTime = prayerState.todayPrayerTime,
                    appLanguage = appLanguage,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Screen.AdhanSettings> {
                AdhanSettingsScreen(
                    settings = settingsState,
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Screen.LocationSettings> {
                LocationSettingsScreen(
                    settings = settingsState,
                    viewModel = locationViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Screen.LanguageSettings> {
                LanguageSettingsScreen(
                    settings = settingsState,
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Screen.UpdateCenter> {
                UpdateCenterScreen(
                    settings = settingsState,
                    updateState = appUpdateState,
                    viewModel = appUpdateViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Screen.FullPlayer> {
                FullAudioPlayerScreen(
                    appLanguage = appLanguage,
                    playbackState = audioState,
                    currentSurah = currentAudioSurah,
                    previousSurah = previousAudioSurah,
                    nextSurah = nextAudioSurah,
                    onBack = { navController.popBackStack() },
                    onTogglePlayback = togglePlayback,
                    onPrevious = { previousAudioSurah?.let(playSelectedSurah) },
                    onNext = { nextAudioSurah?.let(playSelectedSurah) },
                    onStop = { AudioService.stop(context) }
                )
            }

            composable<Screen.Tafsir> { backStackEntry ->
                val tafsirArgs = backStackEntry.toRoute<Screen.Tafsir>()
                var surah by remember { androidx.compose.runtime.mutableStateOf<Surah?>(null) }
                var ayat by remember { androidx.compose.runtime.mutableStateOf<com.sajda.app.domain.model.Ayat?>(null) }
                
                LaunchedEffect(tafsirArgs) {
                    surah = quranRepository.getSurahByNumber(tafsirArgs.surahNumber)
                    ayat = quranRepository.getAyat(tafsirArgs.surahNumber, tafsirArgs.ayatNumber)
                }

                if (surah != null && ayat != null) {
                    TafsirScreen(
                        surah = surah!!,
                        ayat = ayat!!,
                        appLanguage = appLanguage,
                        tafsirRepository = tafsirRepository,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        if (isTopLevelDestination && audioState.isActive) {
            FloatingMiniPlayer(
                playbackState = audioState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 92.dp),
                onTogglePlayback = togglePlayback,
                onOpenPlayer = { navController.navigate(Screen.FullPlayer) },
                onStop = { AudioService.stop(context) }
            )
        }

        if (isTopLevelDestination) {
            FloatingDock(
                items = listOf(
                    DockItem(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.tab_home), Icons.Rounded.Home),
                    DockItem(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.qur_an), Icons.Rounded.MenuBook),
                    DockItem(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.tab_adhan), Icons.Rounded.NotificationsActive),
                    DockItem(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.tab_hadith), Icons.Rounded.HistoryEdu),
                    DockItem(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.tab_ramadan), Icons.Rounded.Mosque),
                    DockItem(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.tab_settings), Icons.Rounded.Settings)
                ),
                selectedIndex = when (currentRoute) {
                    Screen.Home::class.qualifiedName -> 0
                    Screen.Quran::class.qualifiedName -> 1
                    Screen.Adhan::class.qualifiedName -> 2
                    Screen.Hadith::class.qualifiedName -> 3
                    Screen.Ramadan::class.qualifiedName -> 4
                    Screen.Settings::class.qualifiedName -> 5
                    else -> 0
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)),
                onSelect = { index ->
                    val destination = when (index) {
                        0 -> Screen.Home
                        1 -> Screen.Quran
                        2 -> Screen.Adhan
                        3 -> Screen.Hadith
                        4 -> Screen.Ramadan
                        5 -> Screen.Settings
                        else -> Screen.Home
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Home) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
