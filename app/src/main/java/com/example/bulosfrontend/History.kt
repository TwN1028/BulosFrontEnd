package com.example.bulosfrontend

import androidx.compose.runtime.mutableStateListOf

data class HistoryItem(
    val sourceLang: String,
    val targetLang: String,
    val inputText: String,
    val translatedText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isSaved: Boolean = true,
)

internal const val RECENT_TRANSLATION_RETENTION_MILLIS = 7L * 24 * 60 * 60 * 1_000

fun HistoryItem.matchesTranslation(
    sourceLang: String,
    targetLang: String,
    inputText: String,
    translatedText: String,
): Boolean =
    this.sourceLang == sourceLang &&
        this.targetLang == targetLang &&
        this.inputText == inputText &&
        this.translatedText == translatedText

fun mostRecentHistoryItem(items: List<HistoryItem>): HistoryItem? =
    items.maxByOrNull(HistoryItem::timestamp)

internal fun recentHistoryItems(
    items: List<HistoryItem>,
    currentTimeMillis: Long = System.currentTimeMillis(),
): List<HistoryItem> = items
    .filter { currentTimeMillis - it.timestamp < RECENT_TRANSLATION_RETENTION_MILLIS }
    .sortedByDescending(HistoryItem::timestamp)

object HistoryProvider {
    val history = mutableStateListOf<HistoryItem>()

    fun addEntry(
        sourceLang: String,
        targetLang: String,
        inputText: String,
        translatedText: String
    ) {
        history.add(0, HistoryItem(sourceLang, targetLang, inputText, translatedText))
    }
}
