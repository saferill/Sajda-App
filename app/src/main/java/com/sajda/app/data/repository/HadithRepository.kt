package com.sajda.app.data.repository

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.sajda.app.data.api.HadithApi
import com.sajda.app.domain.model.HadithBook
import com.sajda.app.domain.model.HadithEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HadithRepository @Inject constructor(
    private val hadithApi: HadithApi
) {
    suspend fun browse(
        book: HadithBook,
        range: String = "1-40"
    ): List<HadithEntry> = withContext(Dispatchers.IO) {
        fetchBook(book, range)
    }

    suspend fun search(
        query: String,
        book: HadithBook,
        range: String = "1-300"
    ): List<HadithEntry> = withContext(Dispatchers.IO) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return@withContext browse(book)
        val explicitNumber = normalizedQuery.toIntOrNull()
        val effectiveRange = if (explicitNumber != null && explicitNumber > 0) {
            "$explicitNumber-$explicitNumber"
        } else {
            range
        }
        return@withContext fetchBook(book, effectiveRange).filter { entry ->
            entry.reference == normalizedQuery ||
            entry.text.contains(normalizedQuery, ignoreCase = true) ||
                entry.arabicText.contains(normalizedQuery, ignoreCase = true) ||
                entry.reference.contains(normalizedQuery, ignoreCase = true) ||
                entry.collection.contains(normalizedQuery, ignoreCase = true)
        }
    }

    private suspend fun fetchBook(book: HadithBook, range: String): List<HadithEntry> {
        val root = hadithApi.getHadithBook(
            url = "https://api.hadith.gading.dev/books/${book.apiId}",
            range = range
        )
        val data = root.getAsJsonObject("data") ?: return emptyList()
        val hadiths = data.getAsJsonArray("hadiths") ?: return emptyList()
        val collectionName = data.stringValue("name").ifBlank { book.title }
        return hadiths.mapNotNull { element ->
            val item = element.asJsonObjectOrNull() ?: return@mapNotNull null
            val number = item.stringValue("number")
            val text = item.stringValue("id")
            if (text.isBlank()) return@mapNotNull null
            HadithEntry(
                id = "${book.apiId}_${number.ifBlank { text.hashCode().toString() }}",
                category = book.title,
                title = book.title,
                collection = collectionName,
                reference = number,
                narrator = collectionName,
                text = text,
                arabicText = item.stringValue("arab"),
                sourceLabel = "Hadith Gading"
            )
        }
    }

    private fun JsonObject.stringValue(key: String): String {
        val value = get(key) ?: return ""
        if (value.isJsonNull) return ""
        return value.asString
    }

    private fun JsonElement.asJsonObjectOrNull(): JsonObject? {
        return if (isJsonObject) asJsonObject else null
    }
}
