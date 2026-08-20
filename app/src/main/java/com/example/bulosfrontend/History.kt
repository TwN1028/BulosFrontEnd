package com.example.bulosfrontend

import androidx.compose.runtime.mutableStateListOf

data class HistoryItem(val sourceLang: String, val targetLang: String, val inputText: String, val translatedText: String, val timestamp: Long = System.currentTimeMillis())

object HistoryProvider {
    val history = mutableStateListOf<HistoryItem>()
    fun addEntry(src: String, tgt: String, input: String, trans: String) = history.add(0, HistoryItem(src, tgt, input, trans))
}
