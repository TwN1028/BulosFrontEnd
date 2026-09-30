package com.example.bulosfrontend

import androidx.compose.runtime.mutableStateListOf

data class HistoryItem(
    val sourceLang: String,
    val targetLang: String,
    val inputText: String,
    val translatedText: String,
    val timestamp: Long = System.currentTimeMillis()
)

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
