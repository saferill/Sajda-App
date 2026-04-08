package com.sajda.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.QuranReadingMode
import com.sajda.app.domain.model.Surah
import com.sajda.app.ui.component.ArabicVerseText
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.theme.surfaceContainerLow
import com.sajda.app.ui.theme.surfaceContainerLowest
import com.sajda.app.ui.viewmodel.QuranViewModel

private enum class QuranFilter { ALL, MAKKIYAH, MADANIYAH }

private enum class ReaderMode { MUSHAF, TRANSLATION, TAFSIR }

@Composable
fun ModernQuranScreen(
    viewModel: QuranViewModel,
    onPlayAudio: (Surah) -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenTafsir: (Surah, com.sajda.app.domain.model.Ayat) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isEnglish = state.appLanguage == AppLanguage.ENGLISH
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(QuranFilter.ALL) }
    var readerMode by rememberSaveable(state.selectedSurah?.number) { mutableStateOf(ReaderMode.TRANSLATION) }

    if (state.isLoading && state.surahList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val filtered = remember(state.surahList, query, filter) {
        state.surahList.filter { surah ->
            val queryMatch = query.isBlank() ||
                surah.transliteration.contains(query, true) ||
                surah.translation.contains(query, true) ||
                surah.englishTranslation.contains(query, true) ||
                surah.nameArabic.contains(query)
            val filterMatch = when (filter) {
                QuranFilter.ALL -> true
                QuranFilter.MAKKIYAH -> surah.revelationPlace.contains("meccan", true) || surah.revelationPlace.contains("mak", true)
                QuranFilter.MADANIYAH -> surah.revelationPlace.contains("medinan", true) || surah.revelationPlace.contains("mad", true)
            }
            queryMatch && filterMatch
        }
    }

    if (state.selectedSurah == null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 150.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Al-Qur'an",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
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
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    placeholder = { Text(if (isEnglish) "Search surah or ayah..." else "Cari surah atau ayat...") },
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip("Semua", filter == QuranFilter.ALL) { filter = QuranFilter.ALL }
                    FilterChip("Makkiyah", filter == QuranFilter.MAKKIYAH) { filter = QuranFilter.MAKKIYAH }
                    FilterChip("Madaniyah", filter == QuranFilter.MADANIYAH) { filter = QuranFilter.MADANIYAH }
                }
            }

            items(filtered, key = { it.number }) { surah ->
                SanctuaryCard(
                    modifier = Modifier.clickable { viewModel.openSurah(surah) },
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
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
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = surah.number.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = surah.transliteration,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${if (isEnglish) surah.englishTranslation.ifBlank { surah.translation } else surah.translation} • ${surah.totalVerses} ayat",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = surah.nameArabic,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End
                            )
                            Icon(
                                imageVector = if (surah.isDownloaded) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = null,
                                tint = if (surah.isDownloaded) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
    } else {
        val surah = state.selectedSurah ?: return
        val bookmarkMap = remember(state.bookmarks, surah.number) {
            state.bookmarks.filter { it.surahNumber == surah.number }.associateBy { it.ayatNumber }
        }
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
                        verticalAlignment = Alignment.CenterVertically
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
                                    text = "${surah.totalVerses} ayat",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { onPlayAudio(surah) }) {
                            Icon(Icons.Rounded.Bookmark, contentDescription = null)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .padding(4.dp)
                    ) {
                        FilterChip(if (isEnglish) "Mushaf" else "Mushaf", readerMode == ReaderMode.MUSHAF) {
                            readerMode = ReaderMode.MUSHAF
                        }
                        FilterChip(if (isEnglish) "Translation" else "Terjemahan", readerMode == ReaderMode.TRANSLATION) {
                            readerMode = ReaderMode.TRANSLATION
                        }
                        FilterChip("Tafsir", readerMode == ReaderMode.TAFSIR) {
                            readerMode = ReaderMode.TAFSIR
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "بِسْمِ ٱللّٰهِ ٱلرَّحْمٰنِ ٱلرَّحِيْمِ",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (isEnglish) "In the name of Allah, Most Gracious, Most Merciful" else "Dengan nama Allah Yang Maha Pengasih lagi Maha Penyayang",
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
                        MaterialTheme.colorScheme.surfaceContainerLowest
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
                                    if (bookmarked) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ayat.ayatNumber.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (bookmarked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
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

                    ArabicVerseText(text = ayat.textArabic, fontSize = if (readerMode == ReaderMode.MUSHAF) state.arabicFontSize + 6 else state.arabicFontSize)

                    if (readerMode != ReaderMode.MUSHAF && state.quranReadingMode != QuranReadingMode.ARABIC_ONLY) {
                        Text(
                            text = if (isEnglish) ayat.englishTranslation.ifBlank { ayat.translation } else ayat.translation,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (readerMode == ReaderMode.TAFSIR) {
                        Text(
                            text = if (isEnglish) "Open quick tafsir" else "Buka tafsir ringkas",
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
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}
