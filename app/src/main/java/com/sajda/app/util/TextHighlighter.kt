package com.sajda.app.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

fun buildHighlightedText(
    text: String,
    query: String,
    highlightColor: Color
): AnnotatedString {
    val trimmedQuery = query.trim()
    if (trimmedQuery.isBlank()) {
        return AnnotatedString(text)
    }

    val startIndex = text.indexOf(trimmedQuery, ignoreCase = true)
    if (startIndex == -1) {
        return AnnotatedString(text)
    }

    val endIndex = startIndex + trimmedQuery.length
    return buildAnnotatedString {
        append(text)
        addStyle(
            SpanStyle(
                color = highlightColor
            ),
            start = startIndex,
            end = endIndex
        )
    }
}
