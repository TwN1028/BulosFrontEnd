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

private val Context.savedTranslationsDataStore by preferencesDataStore(name = "saved_translations")

class SavedTranslationRepository(private val context: Context) {
    private val entriesKey = stringPreferencesKey("entries_v1")

    val entries: Flow<List<HistoryItem>> = context.savedTranslationsDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> pruneExpiredUnsaved(decodeList(preferences[entriesKey].orEmpty())) }

    suspend fun record(
        sourceLang: String,
        targetLang: String,
        inputText: String,
        translatedText: String,
        isSaved: Boolean = false,
    ) {
        context.savedTranslationsDataStore.edit { preferences ->
            val current = pruneExpiredUnsaved(decodeList(preferences[entriesKey].orEmpty()))
            var timestamp = System.currentTimeMillis()
            while (current.any { it.timestamp == timestamp }) timestamp++
            preferences[entriesKey] = encodeList(
                listOf(HistoryItem(sourceLang, targetLang, inputText, translatedText, timestamp, isSaved = isSaved)) + current,
            )
        }
    }

    suspend fun toggleSaved(
        sourceLang: String,
        targetLang: String,
        inputText: String,
        translatedText: String,
    ) {
        context.savedTranslationsDataStore.edit { preferences ->
            val current = pruneExpiredUnsaved(decodeList(preferences[entriesKey].orEmpty())).toMutableList()
            val index = current.indexOfFirst {
                it.matchesTranslation(sourceLang, targetLang, inputText, translatedText)
            }
            if (index >= 0) {
                val entry = current[index]
                current[index] = entry.copy(
                    isSaved = !entry.isSaved,
                    isFavorite = if (entry.isSaved) false else entry.isFavorite,
                )
            } else {
                var timestamp = System.currentTimeMillis()
                while (current.any { it.timestamp == timestamp }) timestamp++
                current.add(0, HistoryItem(sourceLang, targetLang, inputText, translatedText, timestamp, isSaved = true))
            }
            preferences[entriesKey] = encodeList(pruneExpiredUnsaved(current))
        }
    }

    suspend fun ensureSaved(
        sourceLang: String,
        targetLang: String,
        inputText: String,
        translatedText: String,
    ) {
        context.savedTranslationsDataStore.edit { preferences ->
            val current = pruneExpiredUnsaved(decodeList(preferences[entriesKey].orEmpty())).toMutableList()
            val index = current.indexOfFirst {
                it.matchesTranslation(sourceLang, targetLang, inputText, translatedText)
            }
            if (index >= 0) {
                current[index] = current[index].copy(isSaved = true)
            } else {
                var timestamp = System.currentTimeMillis()
                while (current.any { it.timestamp == timestamp }) timestamp++
                current.add(0, HistoryItem(sourceLang, targetLang, inputText, translatedText, timestamp, isSaved = true))
            }
            preferences[entriesKey] = encodeList(pruneExpiredUnsaved(current))
        }
    }

    suspend fun delete(timestamps: Set<Long>) {
        if (timestamps.isEmpty()) return
        context.savedTranslationsDataStore.edit { preferences ->
            preferences[entriesKey] = encodeList(
                pruneExpiredUnsaved(decodeList(preferences[entriesKey].orEmpty()).filterNot { it.timestamp in timestamps }),
            )
        }
    }

    suspend fun removeSaved(timestamps: Set<Long>) {
        if (timestamps.isEmpty()) return
        context.savedTranslationsDataStore.edit { preferences ->
            val updated = decodeList(preferences[entriesKey].orEmpty()).map { entry ->
                if (entry.timestamp in timestamps) entry.copy(isSaved = false, isFavorite = false) else entry
            }
            preferences[entriesKey] = encodeList(pruneExpiredUnsaved(updated))
        }
    }

    suspend fun toggleFavorite(timestamp: Long) {
        updateFavorites(setOf(timestamp)) { entry -> !entry.isFavorite }
    }

    suspend fun addFavorites(timestamps: Set<Long>) {
        updateFavorites(timestamps) { true }
    }

    private suspend fun updateFavorites(
        timestamps: Set<Long>,
        favoriteValue: (HistoryItem) -> Boolean,
    ) {
        if (timestamps.isEmpty()) return
        context.savedTranslationsDataStore.edit { preferences ->
            val current = decodeList(preferences[entriesKey].orEmpty())
            preferences[entriesKey] = encodeList(
                current.map { entry ->
                    if (entry.timestamp in timestamps && entry.isSaved) {
                        entry.copy(isFavorite = favoriteValue(entry))
                    } else {
                        entry
                    }
                },
            )
        }
    }

    suspend fun clearSaved() {
        context.savedTranslationsDataStore.edit { preferences ->
            val updated = decodeList(preferences[entriesKey].orEmpty()).map { entry ->
                if (entry.isSaved) entry.copy(isSaved = false, isFavorite = false) else entry
            }
            preferences[entriesKey] = encodeList(pruneExpiredUnsaved(updated))
        }
    }

    suspend fun pruneExpiredUnsavedEntries() {
        context.savedTranslationsDataStore.edit { preferences ->
            preferences[entriesKey] = encodeList(
                pruneExpiredUnsaved(decodeList(preferences[entriesKey].orEmpty())),
            )
        }
    }

    private fun encodeList(entries: List<HistoryItem>): String = buildString {
        entries.forEach { entry ->
            val encoded = buildString {
                appendField(entry.sourceLang)
                appendField(entry.targetLang)
                appendField(entry.inputText)
                appendField(entry.translatedText)
                appendField(entry.timestamp.toString())
                appendField(entry.isFavorite.toString())
                appendField(entry.isSaved.toString())
            }
            appendField(encoded)
        }
    }

    private fun decodeList(encoded: String): List<HistoryItem> = runCatching {
        val records = readFields(encoded)
        records.mapNotNull { record ->
            val fields = readFields(record)
            if (fields.size !in 5..7) return@mapNotNull null
            val timestamp = fields[4].toLongOrNull() ?: return@mapNotNull null
            HistoryItem(
                sourceLang = fields[0],
                targetLang = fields[1],
                inputText = fields[2],
                translatedText = fields[3],
                timestamp = timestamp,
                isFavorite = fields.getOrNull(5) == "true",
                isSaved = fields.getOrNull(6) != "false",
            )
        }
    }.getOrDefault(emptyList())

    private fun pruneExpiredUnsaved(
        entries: List<HistoryItem>,
        currentTimeMillis: Long = System.currentTimeMillis(),
    ): List<HistoryItem> = entries.filter { entry ->
        entry.isSaved || currentTimeMillis - entry.timestamp < RECENT_TRANSLATION_RETENTION_MILLIS
    }

    private fun StringBuilder.appendField(value: String) {
        append(value.length).append(':').append(value)
    }

    private fun readFields(encoded: String): List<String> {
        val fields = mutableListOf<String>()
        var position = 0
        while (position < encoded.length) {
            val separator = encoded.indexOf(':', position)
            require(separator >= position)
            val length = encoded.substring(position, separator).toInt()
            val start = separator + 1
            val end = start + length
            require(end <= encoded.length)
            fields += encoded.substring(start, end)
            position = end
        }
        return fields
    }
}
