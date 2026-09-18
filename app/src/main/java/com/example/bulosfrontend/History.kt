package com.example.bulosfrontend

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.historyDataStore by preferencesDataStore(name = "history_translations")

data class HistoryItem(
    val sourceLang: String,
    val targetLang: String,
    val inputText: String,
    val translatedText: String,
    val timestamp: Long = System.currentTimeMillis(),
)

class HistoryRepository(private val context: Context) {
    private val entriesKey = stringPreferencesKey("history_entries_v1")

    val history: Flow<List<HistoryItem>> = context.historyDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> decodeList(preferences[entriesKey].orEmpty()) }

    suspend fun addEntry(
        sourceLang: String,
        targetLang: String,
        inputText: String,
        translatedText: String,
    ) {
        context.historyDataStore.edit { preferences ->
            val current = decodeList(preferences[entriesKey].orEmpty())
            val duplicate = current.any {
                (it.sourceLang == sourceLang) &&
                    (it.targetLang == targetLang) &&
                    (it.inputText == inputText) &&
                    (it.translatedText == translatedText)
            }
            if (!duplicate) {
                var timestamp = System.currentTimeMillis()
                while (current.any { it.timestamp == timestamp }) timestamp++
                preferences[entriesKey] = encodeList(
                    listOf(HistoryItem(sourceLang, targetLang, inputText, translatedText, timestamp)) + current,
                )
            }
        }
    }

    suspend fun delete(timestamps: Set<Long>) {
        if (timestamps.isEmpty()) return
        context.historyDataStore.edit { preferences ->
            preferences[entriesKey] = encodeList(
                decodeList(preferences[entriesKey].orEmpty()).filterNot { it.timestamp in timestamps },
            )
        }
    }

    suspend fun clear() {
        context.historyDataStore.edit { preferences -> preferences.remove(entriesKey) }
    }

    private fun encodeList(entries: List<HistoryItem>): String = buildString {
        entries.forEach { entry ->
            val encoded = buildString {
                appendField(entry.sourceLang)
                appendField(entry.targetLang)
                appendField(entry.inputText)
                appendField(entry.translatedText)
                appendField(entry.timestamp.toString())
            }
            appendField(encoded)
        }
    }

    private fun decodeList(encoded: String): List<HistoryItem> = runCatching {
        val records = readFields(encoded)
        records.mapNotNull { record ->
            val fields = readFields(record)
            if (fields.size != 5) return@mapNotNull null
            val timestamp = fields[4].toLongOrNull() ?: return@mapNotNull null
            HistoryItem(fields[0], fields[1], fields[2], fields[3], timestamp)
        }
    }.getOrDefault(emptyList())

    private fun StringBuilder.appendField(value: String) {
        append(value.length).append(':').append(value)
    }

    private fun readFields(encoded: String): List<String> {
        val fields = mutableListOf<String>()
        var position = 0
        while (position < encoded.length) {
            val separator = encoded.indexOf(':', position)
            if (separator < position) break
            val lengthStr = encoded.substring(position, separator)
            val length = lengthStr.toIntOrNull() ?: break
            val start = separator + 1
            val end = start + length
            if (end > encoded.length) break
            fields += encoded.substring(start, end)
            position = end
        }
        return fields
    }
}
