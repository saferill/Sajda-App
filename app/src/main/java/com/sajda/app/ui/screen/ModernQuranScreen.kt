package com.sajda.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.AudioDownloadMode
import com.sajda.app.domain.model.QuranReadingMode
import com.sajda.app.domain.model.QuranReciter
import com.sajda.app.domain.model.Surah
import com.sajda.app.ui.component.ArabicVerseText
import com.sajda.app.ui.component.MetadataChip
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.component.formatStorageSize
import com.sajda.app.ui.theme.surfaceContainerHigh
import com.sajda.app.ui.theme.surfaceContainerLow
import com.sajda.app.ui.viewmodel.QuranViewModel
import com.sajda.app.ui.viewmodel.SettingsViewModel
import com.sajda.app.util.audioBundleSizeBytes
import com.sajda.app.util.hasAnyDownloadedAudio
import com.sajda.app.util.hasDownloadedAudioFor
import com.sajda.app.util.isEnglish
import com.sajda.app.util.AppTranslations

private enum class QuranFilter { ALL, MAKKIYAH, MADANIYAH }

private enum class ReaderMode { MUSHAF, TRANSLATION, TAFSIR }

@Composable
fun ModernQuranScreen(
    viewModel: QuranViewModel,
    settingsViewModel: SettingsViewModel,
    onPlayAudio: (Surah) -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenTafsir: (Surah, com.sajda.app.domain.model.Ayat) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isEnglish = state.appLanguage.isEnglish()
    val selectedReciter = state.selectedQuranReciter
    var pendingDownloadSurah by remember { mutableStateOf<Surah?>(null) }
    var downloadMode by remember { mutableStateOf(state.audioDownloadMode) }
    var wifiOnlyDownload by remember { mutableStateOf(state.wifiOnlyAudioDownloads) }

    androidx.compose.runtime.LaunchedEffect(pendingDownloadSurah) {
        if (pendingDownloadSurah != null) {
            downloadMode = state.audioDownloadMode
            wifiOnlyDownload = state.wifiOnlyAudioDownloads
        }
    }

    pendingDownloadSurah?.let { surah ->
        AudioDownloadOptionsDialog(
            appLanguage = state.appLanguage,
            selectedReciter = selectedReciter,
            mode = downloadMode,
            wifiOnly = wifiOnlyDownload,
            onModeChange = { downloadMode = it },
            onWifiOnlyChange = { wifiOnlyDownload = it },
            onDismiss = { pendingDownloadSurah = null },
            onConfirm = {
                viewModel.downloadAudio(surah, downloadMode, wifiOnlyDownload)
                pendingDownloadSurah = null
            }
        )
    }
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(QuranFilter.ALL) }
    var readerMode by rememberSaveable(state.selectedSurah?.number) { mutableStateOf(ReaderMode.TRANSLATION) }

    if (state.isLoading && state.surahList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val filteredSurah = remember(state.surahList, query, filter) {
        state.surahList.filter { surah ->
            val matchesQuery = query.isBlank() ||
                surah.transliteration.contains(query, ignoreCase = true) ||
                surah.translation.contains(query, ignoreCase = true) ||
                surah.englishTranslation.contains(query, ignoreCase = true) ||
                surah.nameArabic.contains(query)
            val matchesFilter = when (filter) {
                QuranFilter.ALL -> true
                QuranFilter.MAKKIYAH -> surah.revelationPlace.contains("meccan", true) || surah.revelationPlace.contains("mak", true)
                QuranFilter.MADANIYAH -> surah.revelationPlace.contains("medinan", true) || surah.revelationPlace.contains("mad", true)
            }
            matchesQuery && matchesFilter
        }
    }

    if (state.selectedSurah == null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 150.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Al-Qur'an",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row {
                        IconButton(onClick = onOpenSearch) {
                            Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onOpenBookmarks) {
                            Icon(Icons.Rounded.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            item {
                state.errorMessage?.let { message ->
                    EmptyStateCard(
                        title = if (isEnglish) "Audio message" else "Pesan audio",
                        message = message
                    )
                }
            }

            item {
                SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        placeholder = {
                            Text(
                                if (isEnglish) "Search surah or meaning" else "Cari surah atau arti"
                            )
                        },
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.all), filter == QuranFilter.ALL) { filter = QuranFilter.ALL }
                        FilterChip("Makkiyah", filter == QuranFilter.MAKKIYAH) { filter = QuranFilter.MAKKIYAH }
                        FilterChip("Madaniyah", filter == QuranFilter.MADANIYAH) { filter = QuranFilter.MADANIYAH }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        QuranReciter.entries.forEach { reciter ->
                            ChoiceChip(
                                label = reciter.title,
                                selected = selectedReciter == reciter,
                                onClick = { settingsViewModel.setSelectedQuranReciter(reciter) }
                            )
                        }
                    }
                }
            }

            if (filteredSurah.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = if (isEnglish) "No surah found" else "Surah tidak ditemukan",
                        message = if (isEnglish) {
                            "Try another keyword or open the full search page."
                        } else {
                            "Coba kata kunci lain atau buka halaman pencarian penuh."
                        }
                    )
                }
            }

            items(filteredSurah, key = { it.number }) { surah ->
                val downloadState = state.downloadStates[surah.number]
                val hasSelectedAudio = surah.hasDownloadedAudioFor(selectedReciter)
                val hasOfflineBundle = surah.hasAnyDownloadedAudio()
                val downloadedReciters = surah.downloadedReciterIds.size
                val estimatedSize = surah.audioBundleSizeBytes(
                    mode = state.audioDownloadMode,
                    selectedReciter = selectedReciter
                )
                SanctuaryCard(
                    modifier = Modifier.clickable { viewModel.openSurah(surah) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = surah.number.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = surah.transliteration,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = surah.nameArabic,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${resolveSurahTranslation(state.appLanguage, surah.translation, surah.englishTranslation)} | ${surah.totalVerses} ${androidx.compose.ui.res.stringResource(com.sajda.app.R.string.verses)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row {
                            if (hasOfflineBundle) {
                                IconButton(onClick = { onPlayAudio(surah) }) {
                                    Icon(Icons.Rounded.Headphones, contentDescription = null)
                                }
                                IconButton(onClick = { viewModel.deleteAudio(surah.number) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = null)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { pendingDownloadSurah = surah }) {
                                        Icon(Icons.Rounded.Download, contentDescription = null)
                                    }
                                    Text(
                                        text = "~${formatStorageSize(estimatedSize)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.downloadedreciters_quranreciter_entries),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (hasOfflineBundle) {
                        MetadataChip(
                            text = if (hasSelectedAudio) {
                                androidx.compose.ui.res.stringResource(com.sajda.app.R.string.all_reciters_saved)
                            } else {
                                androidx.compose.ui.res.stringResource(com.sajda.app.R.string.offline_audio_is_ready)
                            },
                            active = true
                        )
                    } else {
                        val sizeLabel = formatStorageSize(estimatedSize)
                        MetadataChip(
                            text = if (state.audioDownloadMode == com.sajda.app.domain.model.AudioDownloadMode.SELECTED_RECITER_ONLY) {
                                androidx.compose.ui.res.stringResource(com.sajda.app.R.string.download_active_reciter_with_size, sizeLabel)
                            } else {
                                androidx.compose.ui.res.stringResource(com.sajda.app.R.string.download_all_reciters_with_size, sizeLabel)
                            },
                            active = false
                        )
                    }

                    if (downloadState?.isDownloading == true) {
                        LinearProgressIndicator(
                            progress = downloadState.progress / 100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.downloading_downloadstate_progress),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    } else {
        val surah = state.selectedSurah ?: return
        val bookmarkMap = remember(state.bookmarks, surah.number) {
            state.bookmarks.filter { it.surahNumber == surah.number }.associateBy { it.ayatNumber }
        }
        val hasSelectedAudio = surah.hasDownloadedAudioFor(selectedReciter)
        val hasOfflineBundle = surah.hasAnyDownloadedAudio()
        val downloadState = state.downloadStates[surah.number]
        val estimatedSize = surah.audioBundleSizeBytes(
            mode = state.audioDownloadMode,
            selectedReciter = selectedReciter
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 150.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = viewModel::closeSurah) {
                                Icon(Icons.Rounded.ChevronLeft, contentDescription = null)
                            }
                            Column {
                                Text(
                                    text = surah.transliteration,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${surah.totalVerses} ${androidx.compose.ui.res.stringResource(com.sajda.app.R.string.verses)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row {
                            if (hasOfflineBundle) {
                                IconButton(onClick = { onPlayAudio(surah) }) {
                                    Icon(Icons.Rounded.Headphones, contentDescription = null)
                                }
                                IconButton(onClick = { viewModel.deleteAudio(surah.number) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = null)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { pendingDownloadSurah = surah }) {
                                        Icon(Icons.Rounded.Download, contentDescription = null)
                                    }
                                    Text(
                                        text = "~${formatStorageSize(estimatedSize)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = surah.nameArabic,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = if (hasOfflineBundle) {
                            androidx.compose.ui.res.stringResource(com.sajda.app.R.string.surah_downloadedreciterids_size_quranrec)
                        } else {
                            run {
                                val sizeLabel = formatStorageSize(estimatedSize)
                                if (state.audioDownloadMode == com.sajda.app.domain.model.AudioDownloadMode.SELECTED_RECITER_ONLY) {
                                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.one_tap_download_active_reciter_with_size, sizeLabel)
                                } else {
                                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.one_tap_download_all_reciters_with_size, sizeLabel)
                                }
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        QuranReciter.entries.forEach { reciter ->
                            ChoiceChip(
                                label = reciter.title,
                                selected = selectedReciter == reciter,
                                onClick = { settingsViewModel.setSelectedQuranReciter(reciter) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(4.dp)
                    ) {
                        FilterChip(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.mushaf), readerMode == ReaderMode.MUSHAF) {
                            readerMode = ReaderMode.MUSHAF
                        }
                        FilterChip(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.translation), readerMode == ReaderMode.TRANSLATION) {
                            readerMode = ReaderMode.TRANSLATION
                        }
                        FilterChip("Tafsir", readerMode == ReaderMode.TAFSIR) {
                            readerMode = ReaderMode.TAFSIR
                        }
                    }

                    if (downloadState?.isDownloading == true) {
                        LinearProgressIndicator(
                            progress = downloadState.progress / 100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (hasOfflineBundle && !hasSelectedAudio) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.your_active_reciter_is_different_but_off),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ArabicVerseText(
                        text = "بِسْمِ ٱللّٰهِ ٱلرَّحْمٰنِ ٱلرَّحِيمِ",
                        fontSize = 30,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.in_the_name_of_allah_most_compassionate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            items(state.ayatList, key = { it.id }) { ayat ->
                val bookmarked = bookmarkMap[ayat.ayatNumber] != null
                SanctuaryCard(
                    modifier = Modifier.clickable { viewModel.recordLastRead(ayat) },
                    containerColor = if (bookmarked) {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.18f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (bookmarked) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.background
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ayat.ayatNumber.toString(),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Row {
                            IconButton(onClick = { viewModel.toggleBookmark(ayat) }) {
                                Icon(
                                    imageVector = if (bookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                    contentDescription = null,
                                    tint = if (bookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onOpenTafsir(surah, ayat) }) {
                                Icon(Icons.Rounded.Tune, contentDescription = null)
                            }
                        }
                    }

                    ArabicVerseText(
                        text = ayat.textArabic,
                        fontSize = if (readerMode == ReaderMode.MUSHAF) state.arabicFontSize + 4 else state.arabicFontSize
                    )

                    if (state.showTransliteration && state.quranReadingMode != QuranReadingMode.ARABIC_ONLY && ayat.transliteration.isNotBlank()) {
                        Text(
                            text = ayat.transliteration,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (readerMode != ReaderMode.MUSHAF && state.quranReadingMode != QuranReadingMode.ARABIC_ONLY) {
                        Text(
                            text = resolveAyatTranslation(
                                appLanguage = state.appLanguage,
                                indonesian = ayat.translation,
                                english = ayat.englishTranslation
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (readerMode == ReaderMode.TAFSIR) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.open_full_tafsir),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onOpenTafsir(surah, ayat) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioDownloadOptionsDialog(
    appLanguage: AppLanguage,
    selectedReciter: QuranReciter,
    mode: AudioDownloadMode,
    wifiOnly: Boolean,
    onModeChange: (AudioDownloadMode) -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isEnglish = appLanguage.isEnglish()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.download_audio)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.choose_a_download_package),
                    style = MaterialTheme.typography.bodyMedium
                )
                DownloadOptionRow(
                    label = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.active_reciter_only_selectedreciter_titl),
                    selected = mode == AudioDownloadMode.SELECTED_RECITER_ONLY,
                    onClick = { onModeChange(AudioDownloadMode.SELECTED_RECITER_ONLY) }
                )
                DownloadOptionRow(
                    label = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.all_reciters),
                    selected = mode == AudioDownloadMode.ALL_RECITERS,
                    onClick = { onModeChange(AudioDownloadMode.ALL_RECITERS) }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.download_via_wi_fi_only),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.avoid_large_downloads_over_mobile_data),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = wifiOnly, onCheckedChange = onWifiOnlyChange)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(if (isEnglish) "Download" else "Unduh")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEnglish) "Cancel" else "Batal")
            }
        }
    )
}

private fun resolveAyatTranslation(
    appLanguage: com.sajda.app.domain.model.AppLanguage,
    indonesian: String,
    english: String
): String {
    val fallbackEnglish = english.ifBlank { indonesian }
    return when (appLanguage) {
        com.sajda.app.domain.model.AppLanguage.INDONESIAN -> indonesian
        com.sajda.app.domain.model.AppLanguage.ENGLISH -> fallbackEnglish
        else -> AppTranslations.translate(fallbackEnglish, appLanguage)
    }
}

private fun resolveSurahTranslation(
    appLanguage: com.sajda.app.domain.model.AppLanguage,
    indonesian: String,
    english: String
): String {
    val fallbackEnglish = english.ifBlank { indonesian }
    return when (appLanguage) {
        com.sajda.app.domain.model.AppLanguage.INDONESIAN -> indonesian
        com.sajda.app.domain.model.AppLanguage.ENGLISH -> fallbackEnglish
        else -> AppTranslations.translate(fallbackEnglish, appLanguage)
    }
}

@Composable
private fun DownloadOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    )
}
