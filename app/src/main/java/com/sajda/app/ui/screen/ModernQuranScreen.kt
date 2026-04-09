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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.sajda.app.domain.model.QuranReadingMode
import com.sajda.app.domain.model.QuranReciter
import com.sajda.app.domain.model.Surah
import com.sajda.app.ui.component.ArabicVerseText
import com.sajda.app.ui.component.MetadataChip
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.component.formatStorageSize
import com.sajda.app.ui.theme.surfaceContainerLow
import com.sajda.app.ui.viewmodel.QuranViewModel
import com.sajda.app.ui.viewmodel.SettingsViewModel
import com.sajda.app.util.audioBundleSizeBytes
import com.sajda.app.util.hasAnyDownloadedAudio
import com.sajda.app.util.hasDownloadedAudioFor
import com.sajda.app.util.isEnglish
import com.sajda.app.util.pick

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
                        FilterChip(state.appLanguage.pick("Semua", "All"), filter == QuranFilter.ALL) { filter = QuranFilter.ALL }
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

            items(filteredSurah, key = { it.number }) { surah ->
                val downloadState = state.downloadStates[surah.number]
                val hasSelectedAudio = surah.hasDownloadedAudioFor(selectedReciter)
                val hasOfflineBundle = surah.hasAnyDownloadedAudio()
                val downloadedReciters = surah.downloadedReciterIds.size
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
                                    text = "${state.appLanguage.pick(surah.translation, surah.englishTranslation.ifBlank { surah.translation })} • ${surah.totalVerses} ${state.appLanguage.pick("ayat", "verses")}",
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
                                    IconButton(onClick = { viewModel.downloadAudio(surah) }) {
                                        Icon(Icons.Rounded.Download, contentDescription = null)
                                    }
                                    Text(
                                        text = "~${formatStorageSize(surah.audioBundleSizeBytes())}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = state.appLanguage.pick(
                            "$downloadedReciters/${QuranReciter.entries.size} qari siap | ${formatStorageSize(surah.audioBundleSizeBytes())}",
                            "$downloadedReciters/${QuranReciter.entries.size} reciters ready | ${formatStorageSize(surah.audioBundleSizeBytes())}"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (hasOfflineBundle) {
                        MetadataChip(
                            text = if (hasSelectedAudio) {
                                state.appLanguage.pick("Semua qari tersimpan", "All reciters saved")
                            } else {
                                state.appLanguage.pick("Audio offline siap diputar", "Offline audio is ready")
                            },
                            active = true
                        )
                    } else {
                        MetadataChip(
                            text = state.appLanguage.pick(
                                "Unduh semua qari • ~${formatStorageSize(surah.audioBundleSizeBytes())}",
                                "Download all reciters • ~${formatStorageSize(surah.audioBundleSizeBytes())}"
                            ),
                            active = false
                        )
                    }

                    if (downloadState?.isDownloading == true) {
                        LinearProgressIndicator(
                            progress = downloadState.progress / 100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = state.appLanguage.pick(
                                "Mengunduh ${downloadState.progress}%",
                                "Downloading ${downloadState.progress}%"
                            ),
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
                                    text = "${surah.totalVerses} ${state.appLanguage.pick("ayat", "verses")}",
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
                                    IconButton(onClick = { viewModel.downloadAudio(surah) }) {
                                        Icon(Icons.Rounded.Download, contentDescription = null)
                                    }
                                    Text(
                                        text = "~${formatStorageSize(surah.audioBundleSizeBytes())}",
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
                            state.appLanguage.pick(
                                "${surah.downloadedReciterIds.size}/${QuranReciter.entries.size} qari offline • ${formatStorageSize(surah.audioBundleSizeBytes())}",
                                "${surah.downloadedReciterIds.size}/${QuranReciter.entries.size} reciters offline • ${formatStorageSize(surah.audioBundleSizeBytes())}"
                            )
                        } else {
                            state.appLanguage.pick(
                                "Sekali unduh langsung semua qari • ~${formatStorageSize(surah.audioBundleSizeBytes())}",
                                "One tap downloads all reciters • ~${formatStorageSize(surah.audioBundleSizeBytes())}"
                            )
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
                        FilterChip(state.appLanguage.pick("Mushaf", "Mushaf"), readerMode == ReaderMode.MUSHAF) {
                            readerMode = ReaderMode.MUSHAF
                        }
                        FilterChip(state.appLanguage.pick("Terjemahan", "Translation"), readerMode == ReaderMode.TRANSLATION) {
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
                            text = state.appLanguage.pick(
                                "Qari aktif belum dipilih, audio offline tetap bisa diputar dari qari yang tersedia.",
                                "Your active reciter is different, but offline audio is still ready from the available reciters."
                            ),
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
                        text = state.appLanguage.pick(
                            "Dengan nama Allah Yang Maha Pengasih lagi Maha Penyayang",
                            "In the name of Allah, Most Compassionate, Most Merciful"
                        ),
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
                            text = state.appLanguage.pick(
                                ayat.translation,
                                ayat.englishTranslation.ifBlank { ayat.translation }
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (readerMode == ReaderMode.TAFSIR) {
                        Text(
                            text = state.appLanguage.pick("Buka tafsir lengkap", "Open full tafsir"),
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
