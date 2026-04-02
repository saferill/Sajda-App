package com.sajda.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajda.app.domain.model.Ayat
import com.sajda.app.domain.model.Bookmark
import com.sajda.app.domain.model.Surah
import com.sajda.app.ui.component.ArabicVerseText
import com.sajda.app.ui.component.MetadataChip
import com.sajda.app.ui.component.SajdaTopAction
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.component.SectionHeader
import com.sajda.app.ui.viewmodel.QuranViewModel

@Composable
fun QuranScreen(
    viewModel: QuranViewModel,
    onPlayAudio: (Surah) -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAudioManager: () -> Unit,
    onOpenTafsir: (Surah, Ayat) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var showFocusMode by rememberSaveable(state.selectedSurah?.number) { mutableStateOf(false) }
    val selectedSurah = state.selectedSurah
    val selectedBookmarkMap = remember(state.bookmarks, selectedSurah?.number) {
        state.bookmarks
            .filter { bookmark -> bookmark.surahNumber == selectedSurah?.number }
            .associateBy { it.ayatNumber }
    }

    if (selectedSurah == null) {
        SurahListContent(
            surahList = state.surahList.filter {
                it.transliteration.contains(query, ignoreCase = true) ||
                    it.translation.contains(query, ignoreCase = true) ||
                    it.nameArabic.contains(query)
            },
            query = query,
            onQueryChange = { query = it },
            downloadStates = state.downloadStates,
            onOpenSurah = viewModel::openSurah,
            onDownload = viewModel::downloadAudio,
            onDelete = { viewModel.deleteAudio(it.number) },
            onPlayAudio = onPlayAudio,
            onOpenBookmarks = onOpenBookmarks,
            onOpenSearch = onOpenSearch,
            onOpenAudioManager = onOpenAudioManager
        )
    } else if (showFocusMode || state.focusMode) {
        FocusModeContent(
            surah = selectedSurah,
            ayatList = state.ayatList,
            showTransliteration = state.showTransliteration && !state.arabicOnly,
            arabicFontSize = state.arabicFontSize + 4,
            verseSpacing = state.verseSpacing + 4,
            onExit = { showFocusMode = false },
            onPlayAudio = { onPlayAudio(selectedSurah) },
            onOpenTafsir = { ayat -> onOpenTafsir(selectedSurah, ayat) }
        )
    } else {
        SurahDetailContent(
            surah = selectedSurah,
            ayatList = state.ayatList,
            bookmarks = selectedBookmarkMap,
            downloadProgress = state.downloadStates[selectedSurah.number],
            showTranslation = state.showTranslation,
            arabicOnly = state.arabicOnly,
            showTransliteration = state.showTransliteration,
            arabicFontSize = state.arabicFontSize,
            translationFontSize = state.translationFontSize,
            verseSpacing = state.verseSpacing,
            isLoading = state.isLoading,
            onBack = viewModel::closeSurah,
            onOpenFocusMode = { showFocusMode = true },
            onDownload = { viewModel.downloadAudio(selectedSurah) },
            onDelete = { viewModel.deleteAudio(selectedSurah.number) },
            onPlayAudio = { onPlayAudio(selectedSurah) },
            onRecordLastRead = viewModel::recordLastRead,
            onToggleBookmark = viewModel::toggleBookmark,
            onSaveBookmarkReflection = viewModel::saveBookmarkReflection,
            onOpenTafsir = { ayat -> onOpenTafsir(selectedSurah, ayat) }
        )
    }
}

@Composable
private fun SurahListContent(
    surahList: List<Surah>,
    query: String,
    onQueryChange: (String) -> Unit,
    downloadStates: Map<Int, com.sajda.app.domain.model.AudioDownloadState>,
    onOpenSurah: (Surah) -> Unit,
    onDownload: (Surah) -> Unit,
    onDelete: (Surah) -> Unit,
    onPlayAudio: (Surah) -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAudioManager: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 180.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Qur'an",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "114 surah offline-first",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SajdaTopAction(Icons.Rounded.Bookmark, "Bookmark", onOpenBookmarks)
                    SajdaTopAction(Icons.Rounded.Search, "Cari", onOpenSearch)
                }
            }
        }

        item {
            SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                SectionHeader(
                    eyebrow = "Library",
                    title = "Baca, unduh, dan putar",
                    actionLabel = "Audio",
                    onAction = onOpenAudioManager
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    placeholder = { Text("Cari surah atau arti...") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp)
                )
            }
        }

        items(surahList, key = { it.number }) { surah ->
            val downloadState = downloadStates[surah.number]
            SanctuaryCard(
                modifier = Modifier.clickable { onOpenSurah(surah) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${surah.number}. ${surah.transliteration}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = surah.nameArabic,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${surah.translation} - ${surah.totalVerses} ayat - ${surah.revelationPlace}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        if (surah.isDownloaded) {
                            IconButton(onClick = { onPlayAudio(surah) }) {
                                Icon(Icons.Rounded.Headphones, contentDescription = "Play")
                            }
                            IconButton(onClick = { onDelete(surah) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                            }
                        } else {
                            IconButton(onClick = { onDownload(surah) }) {
                                Icon(Icons.Rounded.Download, contentDescription = "Download")
                            }
                        }
                    }
                }

                if (downloadState?.isDownloading == true) {
                    LinearProgressIndicator(
                        progress = { downloadState.progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    Text(
                        text = "Mengunduh ${downloadState.progress}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    MetadataChip(
                        text = if (surah.isDownloaded) "Downloaded" else "Tap to open",
                        active = surah.isDownloaded
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SurahDetailContent(
    surah: Surah,
    ayatList: List<Ayat>,
    bookmarks: Map<Int, Bookmark>,
    downloadProgress: com.sajda.app.domain.model.AudioDownloadState?,
    showTranslation: Boolean,
    arabicOnly: Boolean,
    showTransliteration: Boolean,
    arabicFontSize: Int,
    translationFontSize: Int,
    verseSpacing: Int,
    isLoading: Boolean,
    onBack: () -> Unit,
    onOpenFocusMode: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onPlayAudio: () -> Unit,
    onRecordLastRead: (Ayat) -> Unit,
    onToggleBookmark: (Ayat) -> Unit,
    onSaveBookmarkReflection: (Ayat, String, String, String) -> Unit,
    onOpenTafsir: (Ayat) -> Unit
) {
    var editingAyatNumber by rememberSaveable(surah.number) { mutableStateOf<Int?>(null) }
    val editingAyat = ayatList.firstOrNull { it.ayatNumber == editingAyatNumber }
    val editingBookmark = editingAyatNumber?.let { bookmarks[it] }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 180.dp),
        verticalArrangement = Arrangement.spacedBy(verseSpacing.dp)
    ) {
        item {
            SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                    Row {
                        Text(
                            text = "Focus",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                .clickable(onClick = onOpenFocusMode)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                        IconButton(onClick = onPlayAudio) {
                            Icon(Icons.Rounded.Headphones, contentDescription = "Play")
                        }
                        if (surah.isDownloaded) {
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                            }
                        } else {
                            IconButton(onClick = onDownload) {
                                Icon(Icons.Rounded.Download, contentDescription = "Download")
                            }
                        }
                    }
                }

                Text(
                    text = surah.transliteration,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = surah.nameArabic,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${surah.translation} - ${surah.totalVerses} ayat",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetadataChip(text = if (arabicOnly) "Arabic only" else "Translation", active = showTranslation && !arabicOnly)
                    if (showTransliteration && !arabicOnly) {
                        MetadataChip(text = "Transliteration", active = true)
                    }
                    MetadataChip(text = "${arabicFontSize}sp arabic", active = false)
                    MetadataChip(text = "${verseSpacing}dp spacing", active = false)
                    MetadataChip(text = if (surah.isDownloaded) "Offline audio" else "Download audio", active = surah.isDownloaded)
                }

                if (downloadProgress?.isDownloading == true) {
                    LinearProgressIndicator(
                        progress = { downloadProgress.progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                }
            }
        }

        if (isLoading && ayatList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            items(ayatList, key = { it.id }) { ayat ->
                val bookmark = bookmarks[ayat.ayatNumber]
                SanctuaryCard(
                    modifier = Modifier.clickable { onRecordLastRead(ayat) },
                    containerColor = bookmarkCardColor(bookmark?.highlightColor)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetadataChip(text = "Ayat ${ayat.ayatNumber}", active = false)
                        Row {
                            IconButton(onClick = { onToggleBookmark(ayat) }) {
                                Icon(
                                    imageVector = if (bookmark != null) {
                                        Icons.Rounded.Bookmark
                                    } else {
                                        Icons.Rounded.BookmarkBorder
                                    },
                                    contentDescription = "Bookmark"
                                )
                            }
                            IconButton(onClick = { editingAyatNumber = ayat.ayatNumber }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Catatan")
                            }
                            IconButton(onClick = { onOpenTafsir(ayat) }) {
                                Icon(Icons.Rounded.Tune, contentDescription = "Tafsir")
                            }
                        }
                    }

                    BookmarkMetaRow(bookmark)

                    ArabicVerseText(
                        text = ayat.textArabic,
                        fontSize = arabicFontSize
                    )

                    if (!arabicOnly && showTransliteration && ayat.transliteration.isNotBlank()) {
                        Text(
                            text = ayat.transliteration,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (!arabicOnly && showTranslation) {
                        Text(
                            text = ayat.translation,
                            fontSize = translationFontSize.sp,
                            lineHeight = (translationFontSize + 8).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!bookmark?.note.isNullOrBlank()) {
                        Text(
                            text = "Catatan pribadi",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = bookmark?.note.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    if (editingAyat != null) {
        val selectedAyat = editingAyat
        AyahReflectionDialog(
            ayat = selectedAyat,
            bookmark = editingBookmark,
            onDismiss = { editingAyatNumber = null },
            onSave = { folderName, note, highlightColor ->
                onSaveBookmarkReflection(selectedAyat, folderName, note, highlightColor)
                editingAyatNumber = null
            }
        )
    }
}

@Composable
private fun FocusModeContent(
    surah: Surah,
    ayatList: List<Ayat>,
    showTransliteration: Boolean,
    arabicFontSize: Int,
    verseSpacing: Int,
    onExit: () -> Unit,
    onPlayAudio: () -> Unit,
    onOpenTafsir: (Ayat) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 180.dp),
        verticalArrangement = Arrangement.spacedBy(verseSpacing.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = surah.transliteration,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Exit",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onExit)
                    )
                    Text(
                        text = "Play",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onPlayAudio)
                    )
                }
            }
        }

        items(ayatList, key = { it.id }) { ayat ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Ayat ${ayat.ayatNumber}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ArabicVerseText(
                    text = ayat.textArabic,
                    textAlign = TextAlign.Center,
                    fontSize = arabicFontSize
                )
                if (showTransliteration && ayat.transliteration.isNotBlank()) {
                    Text(
                        text = ayat.transliteration,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Tafsir ringkas",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onOpenTafsir(ayat) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AyahReflectionDialog(
    ayat: Ayat,
    bookmark: Bookmark?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    val folderOptions = listOf("Favorites", "Tadabbur", "Hafalan", "Murajaah")
    val colorOptions = listOf("", "Mint", "Sand", "Blush", "Sky")
    var selectedFolder by rememberSaveable(bookmark?.id, ayat.id) {
        mutableStateOf(bookmark?.folderName?.ifBlank { "Favorites" } ?: "Favorites")
    }
    var selectedHighlight by rememberSaveable(bookmark?.id, ayat.id) {
        mutableStateOf(bookmark?.highlightColor.orEmpty())
    }
    var note by rememberSaveable(bookmark?.id, ayat.id) {
        mutableStateOf(bookmark?.note.orEmpty())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(selectedFolder, note, selectedHighlight) }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        title = {
            Text(
                text = "Ayat ${ayat.ayatNumber}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Simpan ayat ini ke folder pribadi, beri warna fokus, dan tambahkan catatan tadabbur singkat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Folder",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    folderOptions.forEach { folder ->
                        ChoiceChip(
                            label = folder,
                            selected = selectedFolder == folder,
                            onClick = { selectedFolder = folder }
                        )
                    }
                }
                Text(
                    text = "Highlight",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorOptions.forEach { colorName ->
                        ChoiceChip(
                            label = if (colorName.isBlank()) "Default" else colorName,
                            selected = selectedHighlight == colorName,
                            onClick = { selectedHighlight = colorName }
                        )
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("Tulis catatan pribadi untuk ayat ini") }
                )
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookmarkMetaRow(bookmark: Bookmark?) {
    if (bookmark == null) return

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MetadataChip(text = bookmark.folderName.ifBlank { "Favorites" }, active = true)
        if (bookmark.highlightColor.isNotBlank()) {
            MetadataChip(text = bookmark.highlightColor, active = false)
        }
        if (bookmark.note.isNotBlank()) {
            MetadataChip(text = "Has note", active = false)
        }
    }
}

@Composable
private fun bookmarkCardColor(highlightColor: String?): Color {
    return when (highlightColor) {
        "Mint" -> Color(0xFFE9F6EF)
        "Sand" -> Color(0xFFF8F1E2)
        "Blush" -> Color(0xFFF8E9EA)
        "Sky" -> Color(0xFFEAF1FB)
        else -> MaterialTheme.colorScheme.surfaceContainerLowest
    }
}
