package com.example.bulosfrontend

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal class OfflineDictionary(entries: List<Map<String, String>>) {
    private val rows = entries.mapNotNull { rawRow ->
        buildMap {
            rawRow.forEach { (key, value) ->
                languageKey(key)?.let { language ->
                    clean(value).takeIf(String::isNotEmpty)?.let { put(language, it) }
                }
            }
        }.takeIf(Map<UiLanguage, String>::isNotEmpty)
    }
    private val phrases = rows.sortedByDescending { row ->
        row.values.maxOfOrNull { it.split(Regex("\\s+")).size } ?: 0
    }

    fun translate(text: String, source: UiLanguage, target: UiLanguage): String? {
        val input = text.trim()
        if (input.isEmpty()) return ""
        val sentence = Regex("^(.+?)([,.!?]+)?$").find(input)
        val body = clean(sentence?.groupValues?.get(1) ?: input)
        val punctuation = sentence?.groupValues?.getOrNull(2).orEmpty()
        exact(body, source, target)?.let { return it + punctuation }

        var working = input
        val replacements = mutableListOf<String>()
        phrases.forEach { row ->
            val phrase = row[source].orEmpty()
            val translated = row[target].orEmpty()
            if (phrase.contains(' ') && translated.isNotEmpty()) {
                val pattern = Regex(
                    "(?i)(?<![a-zA-Z0-9-])${Regex.escape(phrase)}(?![a-zA-Z0-9-])",
                )
                if (pattern.containsMatchIn(working)) {
                    val marker = "[[P${replacements.size}]]"
                    replacements += translated
                    working = working.replace(pattern, marker)
                }
            }
        }
        var matchedAnything = replacements.isNotEmpty()
        var result = working.split(Regex("\\s+")).joinToString(" ") { token ->
            if (token.startsWith("[[P") && token.endsWith("]]")) return@joinToString token
            exact(token, source, target)?.also { matchedAnything = true } ?: run {
                val word = Regex("^(.+?)([,.!?]+)?$").find(token)
                val core = word?.groupValues?.get(1).orEmpty()
                val suffix = word?.groupValues?.getOrNull(2).orEmpty()
                exact(core, source, target)?.also { matchedAnything = true }?.plus(suffix) ?: token
            }
        }
        replacements.forEachIndexed { index, replacement ->
            result = result.replace("[[P$index]]", replacement)
        }
        return result.takeIf { matchedAnything }
    }

    private fun exact(text: String, source: UiLanguage, target: UiLanguage): String? =
        rows.firstOrNull { it[source].equals(clean(text), ignoreCase = true) }?.get(target)

    companion object {
        private fun clean(value: String): String = value
            .replace('\u00A0', ' ')
            .replace(Regex("\\p{C}"), "")
            .trim()

        private fun languageKey(value: String): UiLanguage? = when (value.trim().lowercase()) {
            "english", "en", "eng" -> UiLanguage.ENGLISH
            "filipino", "tagalog", "fil", "tl", "tag" -> UiLanguage.FILIPINO
            "bulos", "bul" -> UiLanguage.BULOS
            else -> null
        }
    }
}

class OfflineDictionaryManager(private val context: Context) {
    private val dictionaryFile = File(context.filesDir, "custom_dictionary.json")
    @Volatile private var dictionary = OfflineDictionary(emptyList())
    @Volatile var isLoaded = false
        private set

    suspend fun load(): Unit = withContext(Dispatchers.IO) {
        if (!dictionaryFile.exists()) return@withContext
        try {
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            val entries: List<Map<String, String>> = dictionaryFile.reader().use {
                Gson().fromJson(it, type)
            }
            dictionary = OfflineDictionary(entries)
            isLoaded = true
        } catch (_: Exception) {
            isLoaded = false
        }
    }

    suspend fun replace(entries: List<Map<String, String>>) = withContext(Dispatchers.IO) {
        dictionaryFile.writeText(Gson().toJson(entries))
        dictionary = OfflineDictionary(entries)
        isLoaded = true
    }

    fun translate(text: String, source: UiLanguage, target: UiLanguage): String? =
        dictionary.translate(text, source, target)
}
