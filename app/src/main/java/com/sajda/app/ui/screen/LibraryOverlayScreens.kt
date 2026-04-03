package com.sajda.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sajda.app.data.repository.QuranRepository
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.AudioDownloadState
import com.sajda.app.domain.model.AudioPlaybackState
import com.sajda.app.domain.model.Bookmark
import com.sajda.app.domain.model.QuranSearchResult
import com.sajda.app.domain.model.SearchResultType
import com.sajda.app.domain.model.Surah
import com.sajda.app.ui.component.ArabicVerseText
import com.sajda.app.ui.component.HeroCard
import com.sajda.app.ui.component.MetadataChip
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.component.formatStorageSize
import com.sajda.app.ui.theme.surfaceContainerLow
import com.sajda.app.ui.theme.surfaceContainerLowest
import com.sajda.app.util.pick
import java.io.File

private data class BookmarkEntryUi(
    val bookmark: Bookmark,
    val arabic: String,
    val translation: String,
    val englishTranslation: String
)

@Composable
fun SearchScreen(
    appLanguage: AppLanguage,
    quranRepository: QuranRepository,
    onBack: () -> Unit,
    onOpenResult: (QuranSearchResult) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results by produceState(initialValue = emptyList<QuranSearchResult>(), query) {
        value = quranRepository.search(query)
    }

    OverlayShell(
        title = appLanguage.pick("Cari", "Search"),
        subtitle = appLanguage.pick("Surah dan ayat", "Surah and verses"),
        onBack = onBack
    ) {
        SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Language, contentDescription = null) },
                placeholder = { Text(appLanguage.pick("Cari ayat, arti, atau nama surah", "Search verses, meanings, or surah names")) },
                singleLine = true
            )
        }

        if (results.isEmpty()) {
            EmptyStateCard(
                title = if (query.isBlank()) {
                    appLanguage.pick("Mulai mencari", "Start searching")
                } else {
                    appLanguage.pick("Hasil belum ditemukan", "No results found")
                },
                message = if (query.isBlank()) {
                    appLanguage.pick(
                        "Ketik kata kunci untuk mencari surah atau ayat secara offline.",
                        "Type a keyword to search surahs or verses offline."
                    )
                } else {
                    appLanguage.pick(
                        "Coba kata yang lebih singkat atau lebih umum.",
                        "Try a shorter or more common keyword."
                    )
                }
            )
        } else {
            results.forEach { result ->
                SanctuaryCard(
                    modifier = Modifier.clickable { onOpenResult(result) }
                ) {
                    MetadataChip(
                        text = if (result.type == SearchResultType.SURAH) {
                            appLanguage.pick("Surah", "Surah")
                        } else {
                            appLanguage.pick("Ayat", "Verse")
                        },
                        active = result.type == SearchResultType.SURAH
                    )
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = result.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BookmarksScreen(
    appLanguage: AppLanguage,
    quranRepository: QuranRepository,
    bookmarks: List<Bookmark>,
    onBack: () -> Unit,
    onOpenAyat: (Bookmark) -> Unit
) {
    val entries by produceState(initialValue = emptyList<BookmarkEntryUi>(), bookmarks) {
        value = bookmarks.mapNotNull { bookmark ->
            val ayat = quranRepository.getAyat(bookmark.surahNumber, bookmark.ayatNumber) ?: return@mapNotNull null
            BookmarkEntryUi(
                bookmark = bookmark,
                arabic = ayat.textArabic,
                translation = ayat.translation,
                englishTranslation = ayat.englishTranslation
            )
        }
    }

    OverlayShell(
        title = appLanguage.pick("Bookmark", "Bookmarks"),
        subtitle = appLanguage.pick(
            "${bookmarks.size} ayat tersimpan",
            "${bookmarks.size} saved verses"
        ),
        onBack = onBack
    ) {
        if (entries.isEmpty()) {
            EmptyStateCard(
                title = appLanguage.pick("Belum ada bookmark", "No bookmarks yet"),
                message = appLanguage.pick(
                    "Simpan ayat favorit dari layar Qur'an untuk membangun perpustakaan tadabbur pribadi.",
                    "Save your favorite verses from the Qur'an screen to build a personal reflection library."
                )
            )
        } else {
            entries.forEach { entry ->
                SanctuaryCard(
                    modifier = Modifier.clickable { onOpenAyat(entry.bookmark) },
                    containerColor = bookmarkCardColor(entry.bookmark.highlightColor)
                ) {
                    Text(
                        text = "${entry.bookmark.surahName} - ${appLanguage.pick("Ayat", "Verse")} ${entry.bookmark.ayatNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    BookmarkMetaRow(entry.bookmark, appLanguage)
                    ArabicVerseText(text = entry.arabic, fontSize = 22)
                    Text(
                        text = if (appLanguage == AppLanguage.ENGLISH) {
                            entry.englishTranslation.ifBlank { entry.translation }
                        } else {
                            entry.translation
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (entry.bookmark.note.isNotBlank()) {
                        Text(
                            text = entry.bookmark.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookmarkMetaRow(bookmark: Bookmark, appLanguage: AppLanguage) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MetadataChip(text = bookmark.folderName.ifBlank { appLanguage.pick("Favorit", "Favorites") }, active = true)
        if (bookmark.highlightColor.isNotBlank()) {
            MetadataChip(text = bookmark.highlightColor, active = false)
        }
    }
}

@Composable
private fun bookmarkCardColor(highlightColor: String): Color {
    return when (highlightColor) {
        "Mint" -> Color(0xFFE9F6EF)
        "Sand" -> Color(0xFFF8F1E2)
        "Blush" -> Color(0xFFF8E9EA)
        "Sky" -> Color(0xFFEAF1FB)
        else -> MaterialTheme.colorScheme.surfaceContainerLowest
    }
}

@Composable
fun AudioManagementScreen(
    appLanguage: AppLanguage,
    surahList: List<Surah>,
    downloadStates: Map<Int, AudioDownloadState>,
    onBack: () -> Unit,
    onPlay: (Surah) -> Unit,
    onDelete: (Surah) -> Unit,
    onDownload: (Surah) -> Unit
) {
    val downloaded = surahList.filter { it.localAudioPath != null }
    val storageUsage = downloaded.sumOf { surah ->
        surah.localAudioPath?.let { path -> File(path).takeIf { it.exists() }?.length() } ?: 0L
    }

    OverlayShell(
        title = appLanguage.pick("Manajemen Audio", "Audio Management"),
        subtitle = appLanguage.pick("Murattal per surah", "Murattal by surah"),
        onBack = onBack
    ) {
        SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
            Text(
                text = formatStorageSize(storageUsage),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = appLanguage.pick(
                    "${downloaded.size} surah tersimpan offline. Anda bebas menghapus atau mengunduh ulang kapan saja.",
                    "${downloaded.size} surahs are stored offline. You can delete or download them again at any time."
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        surahList.forEach { surah ->
            val state = downloadStates[surah.number]
            SanctuaryCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = surah.transliteration,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${if (appLanguage == AppLanguage.ENGLISH) surah.englishTranslation.ifBlank { surah.translation } else surah.translation} - ${surah.totalVerses} ${appLanguage.pick("ayat", "verses")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row {
                        if (surah.localAudioPath != null) {
                            IconButton(onClick = { onPlay(surah) }) {
                                Icon(Icons.Rounded.Headphones, contentDescription = appLanguage.pick("Putar", "Play"))
                            }
                            IconButton(onClick = { onDelete(surah) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = appLanguage.pick("Hapus", "Delete"))
                            }
                        } else {
                            IconButton(onClick = { onDownload(surah) }) {
                                Icon(Icons.Rounded.Download, contentDescription = appLanguage.pick("Unduh", "Download"))
                            }
                        }
                    }
                }
                if (state?.isDownloading == true) {
                    LinearProgressIndicator(
                        progress = state.progress / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                MetadataChip(
                    text = when {
                        state?.isDownloading == true -> appLanguage.pick("Mengunduh ${state.progress}%", "Downloading ${state.progress}%")
                        surah.localAudioPath != null -> appLanguage.pick("Siap offline", "Offline ready")
                        else -> appLanguage.pick("Belum diunduh", "Not downloaded")
                    },
                    active = surah.localAudioPath != null
                )
            }
        }
    }
}

@Composable
fun FullAudioPlayerScreen(
    appLanguage: AppLanguage,
    playbackState: AudioPlaybackState,
    currentSurah: Surah?,
    previousSurah: Surah?,
    nextSurah: Surah?,
    onBack: () -> Unit,
    onTogglePlayback: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onStop: () -> Unit
) {
    OverlayShell(
        title = appLanguage.pick("Sedang Diputar", "Now Playing"),
        subtitle = currentSurah?.transliteration ?: appLanguage.pick("Murattal Sajda", "Sajda Murattal"),
        onBack = onBack
    ) {
        HeroCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = playbackState.title.ifBlank { appLanguage.pick("Murattal Sajda", "Sajda Murattal") },
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${playbackState.elapsedLabel} / ${playbackState.durationLabel}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f)
                )
                LinearProgressIndicator(
                    progress = playbackState.progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevious, enabled = previousSurah != null) {
                        Icon(Icons.Rounded.SkipPrevious, contentDescription = appLanguage.pick("Sebelumnya", "Previous"), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onTogglePlayback) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                            contentDescription = appLanguage.pick("Putar atau jeda", "Toggle playback"),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    IconButton(onClick = onNext, enabled = nextSurah != null) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = appLanguage.pick("Berikutnya", "Next"), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                Text(
                    text = appLanguage.pick("Hentikan audio", "Stop playback"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.clickable(onClick = onStop)
                )
            }
        }

        SanctuaryCard {
            Text(
                text = appLanguage.pick(
                    "Audio latar tetap aktif saat aplikasi ditutup, dengan kontrol putar/jeda dan pindah surah dari player ini.",
                    "Background audio stays active when the app is closed, with play/pause and next-surah controls from this player."
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
