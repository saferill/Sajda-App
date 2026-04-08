package com.sajda.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sajda.app.data.repository.HadithRepository
import com.sajda.app.domain.model.HadithBook
import com.sajda.app.domain.model.HadithEntry
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.ui.component.SajdaTopAction
import com.sajda.app.ui.component.SajdaTopBar
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.component.SectionHeader
import com.sajda.app.util.isEnglish
import com.sajda.app.util.pick

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HadithSearchScreen(
    settings: UserSettings,
    repository: HadithRepository,
    onBack: (() -> Unit)? = null
) {
    val isEnglish = settings.appLanguage.isEnglish()
    var selectedBook by rememberSaveable { mutableStateOf(HadithBook.BUKHARI) }
    var query by rememberSaveable { mutableStateOf("") }
    var refreshKey by rememberSaveable { mutableIntStateOf(0) }
    var items by remember(selectedBook) { mutableStateOf<List<HadithEntry>>(emptyList()) }
    var isLoading by remember(selectedBook) { mutableStateOf(true) }
    var errorMessage by remember(selectedBook) { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedBook, refreshKey) {
        isLoading = true
        errorMessage = null
        runCatching {
            repository.browse(selectedBook, range = "1-300")
        }.onSuccess {
            items = it
        }.onFailure {
            errorMessage = it.message ?: settings.pick("Gagal memuat hadist", "Failed to load hadith")
            items = emptyList()
        }
        isLoading = false
    }

    val filteredItems = remember(items, query) {
        val keyword = query.trim()
        if (keyword.isBlank()) {
            items
        } else {
            items.filter { item ->
                item.text.contains(keyword, ignoreCase = true) ||
                    item.arabicText.contains(keyword, ignoreCase = true) ||
                    item.reference.contains(keyword, ignoreCase = true) ||
                    item.collection.contains(keyword, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 150.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SajdaTopBar(
                title = settings.pick("Hadist", "Hadith"),
                subtitle = selectedBook.title,
                leading = onBack?.let { backAction ->
                    {
                        SajdaTopAction(
                            icon = Icons.Rounded.ArrowBack,
                            label = settings.pick("Kembali", "Back"),
                            onClick = backAction
                        )
                    }
                }
            )
        }

        item {
            SanctuaryCard {
                SectionHeader(
                    eyebrow = settings.pick("Pencarian Hadist", "Hadith Search"),
                    title = settings.pick("Cari sendiri kitab dan nomor hadist", "Browse books and search hadith yourself")
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    placeholder = {
                        Text(
                            if (isEnglish) {
                                "Search text, Arabic, or hadith number"
                            } else {
                                "Cari isi, teks Arab, atau nomor hadist"
                            }
                        )
                    },
                    singleLine = true
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HadithBook.entries.forEach { book ->
                        ChoiceChip(
                            label = book.title,
                            selected = selectedBook == book,
                            onClick = { selectedBook = book }
                        )
                    }
                }
                Text(
                    text = if (errorMessage != null) {
                        errorMessage.orEmpty()
                    } else {
                        settings.pick(
                            "${filteredItems.size} hadist tampil dari ${selectedBook.title}",
                            "${filteredItems.size} hadiths shown from ${selectedBook.title}"
                        )
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = settings.pick("Muat ulang", "Refresh"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { refreshKey++ }
                )
            }
        }

        if (isLoading) {
            item {
                SanctuaryCard {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = settings.pick("Sedang memuat hadist...", "Loading hadith..."),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (filteredItems.isEmpty()) {
            item {
                SanctuaryCard {
                    Text(
                        text = settings.pick("Hadist tidak ditemukan", "No hadith found"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = settings.pick("Coba kata kunci lain atau ganti kitab hadist.", "Try another keyword or switch to a different hadith collection."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredItems, key = { it.id }) { hadith ->
                HadithEntryCard(settings = settings, hadith = hadith)
            }
        }
    }
}

@Composable
private fun HadithEntryCard(
    settings: UserSettings,
    hadith: HadithEntry
) {
    SanctuaryCard {
        Text(
            text = "${hadith.collection} • ${hadith.reference}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        if (hadith.arabicText.isNotBlank()) {
            Text(
                text = hadith.arabicText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = hadith.text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (hadith.sourceLabel.isNotBlank()) {
            Text(
                text = settings.pick("Sumber: ${hadith.sourceLabel}", "Source: ${hadith.sourceLabel}"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
