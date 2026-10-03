package com.example.bulosfrontend

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** A verified cached dictionary row exposed to the Dictionary UI without altering stored JSON fields. */
data class DictionaryEntry(
    val bulos: String,
    val filipino: String,
    val english: String,
)

data class OfflineTranslationSuggestion(
    val sourceText: String,
    val translatedText: String,
    /** Normalized similarity from 0.0 to 1.0; higher means closer. */
    val similarity: Double,
    /** Levenshtein edit distance; lower means closer. */
    val editDistance: Int,
)

sealed interface OfflineLookupResult {
    data class Translation(val translatedText: String, val method: String, val confidence: Double) : OfflineLookupResult
    data class Suggestions(val candidates: List<OfflineTranslationSuggestion>) : OfflineLookupResult
    data object Unavailable : OfflineLookupResult
    data object NoData : OfflineLookupResult
}

internal class OfflineDictionary(entries: List<Map<String, String>>) {
    private data class Row(val values: Map<UiLanguage, String>, val isPhrase: Boolean)

    private val rows = entries.mapNotNull { rawRow ->
        val values = buildMap {
            rawRow.forEach { (key, value) ->
                languageKey(key)?.let { language ->
                    value.takeIf(String::isNotBlank)?.let { put(language, it) }
                }
            }
        }
        if (values.isEmpty()) return@mapNotNull null
        val category = rawRow[CATEGORY_KEY].orEmpty()
        Row(
            values = values,
            isPhrase = category.contains("phrase", ignoreCase = true) ||
                (category.isBlank() && values.values.any { it.trim().contains(Regex("\\s")) }),
        )
    }

    fun lookup(text: String, source: UiLanguage, target: UiLanguage): OfflineLookupResult {
        if (!TranslationLanguageRules.isValidPair(
                TranslationLanguageRules.languageForUi(source),
                TranslationLanguageRules.languageForUi(target),
            )
        ) return OfflineLookupResult.Unavailable
        if (rows.isEmpty()) return OfflineLookupResult.NoData
        val normalizedInput = TextNormalizer.normalizeForMatching(text)
        if (normalizedInput.isEmpty()) return OfflineLookupResult.Unavailable
        val punctuation = trailingSentencePunctuation(text)

        exact(normalizedInput, source, target, phrase = true)?.let {
            return OfflineLookupResult.Translation(it + punctuation, "offline_exact_phrase", 1.0)
        }
        exact(normalizedInput, source, target, phrase = false)?.let {
            return OfflineLookupResult.Translation(it + punctuation, "offline_exact_dictionary", 1.0)
        }

        val candidates = rows.asSequence()
            .filterNot(Row::isPhrase)
            .mapNotNull { row ->
                val sourceText = row.values[source] ?: return@mapNotNull null
                val translatedText = row.values[target] ?: return@mapNotNull null
                val normalizedCandidate = TextNormalizer.normalizeForMatching(sourceText)
                if (normalizedCandidate.isEmpty()) return@mapNotNull null
                val distance = levenshteinDistance(normalizedInput, normalizedCandidate)
                val longestLength = maxOf(normalizedInput.length, normalizedCandidate.length)
                val similarity = if (longestLength == 0) 1.0 else 1.0 - distance.toDouble() / longestLength
                OfflineTranslationSuggestion(sourceText, translatedText, similarity, distance)
            }
            .sortedWith(
                compareByDescending<OfflineTranslationSuggestion> { it.similarity }
                    .thenBy { it.editDistance }
                    .thenBy { it.sourceText },
            )
            .toList()

        val best = candidates.firstOrNull()
        val runnerUp = candidates.getOrNull(1)
        if (
            best != null &&
            normalizedInput.length >= MIN_FUZZY_LENGTH &&
            best.similarity >= AUTO_TRANSLATE_SIMILARITY &&
            best.editDistance <= maximumAutomaticEdits(normalizedInput.length) &&
            (runnerUp == null || best.similarity - runnerUp.similarity >= AMBIGUITY_MARGIN)
        ) {
            return OfflineLookupResult.Translation(
                best.translatedText + punctuation,
                "offline_strict_fuzzy_dictionary",
                best.similarity,
            )
        }

        val suggestions = candidates.filter { it.similarity >= SUGGESTION_SIMILARITY }.take(MAX_SUGGESTIONS)
        return if (suggestions.isEmpty()) OfflineLookupResult.Unavailable
        else OfflineLookupResult.Suggestions(suggestions)
    }

    fun displayEntries(): List<DictionaryEntry> = rows.map { row ->
        DictionaryEntry(
            bulos = row.values[UiLanguage.BULOS].orEmpty(),
            filipino = row.values[UiLanguage.FILIPINO].orEmpty(),
            english = row.values[UiLanguage.ENGLISH].orEmpty(),
        )
    }

    private fun exact(normalizedInput: String, source: UiLanguage, target: UiLanguage, phrase: Boolean): String? =
        rows.firstOrNull { row ->
            row.isPhrase == phrase &&
                row.values[source]?.let(TextNormalizer::normalizeForMatching) == normalizedInput
        }?.values?.get(target)

    companion object {
        const val CATEGORY_KEY = "_category"
        const val AUTO_TRANSLATE_SIMILARITY = 0.88
        const val SUGGESTION_SIMILARITY = 0.65
        const val AMBIGUITY_MARGIN = 0.08
        const val MAX_SUGGESTIONS = 3
        private const val MIN_FUZZY_LENGTH = 4

        private fun maximumAutomaticEdits(length: Int): Int = when {
            length < 4 -> 0
            length < 8 -> 1
            else -> 2
        }

        private fun trailingSentencePunctuation(text: String): String =
            Regex("[,.;:!?…]+$").find(text.trim())?.value.orEmpty()

        private fun levenshteinDistance(first: String, second: String): Int {
            if (first == second) return 0
            if (first.isEmpty()) return second.length
            if (second.isEmpty()) return first.length
            var previous = IntArray(second.length + 1) { it }
            first.forEachIndexed { firstIndex, firstChar ->
                val current = IntArray(second.length + 1)
                current[0] = firstIndex + 1
                second.forEachIndexed { secondIndex, secondChar ->
                    current[secondIndex + 1] = minOf(
                        current[secondIndex] + 1,
                        previous[secondIndex + 1] + 1,
                        previous[secondIndex] + if (firstChar == secondChar) 0 else 1,
                    )
                }
                previous = current
            }
            return previous[second.length]
        }

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
        runCatching { readEntries(dictionaryFile) }
            .onSuccess { entries ->
                dictionary = OfflineDictionary(entries)
                isLoaded = entries.isNotEmpty()
            }
            .onFailure { isLoaded = false }
    }

    suspend fun replace(entries: List<Map<String, String>>) = withContext(Dispatchers.IO) {
        require(entries.isNotEmpty()) { "Verified dictionary download was empty." }
        val replacement = OfflineDictionary(entries)
        val temporaryFile = File(context.filesDir, "custom_dictionary.json.tmp")
        try {
            temporaryFile.writeText(Gson().toJson(entries))
            readEntries(temporaryFile)
            if (!temporaryFile.renameTo(dictionaryFile)) {
                temporaryFile.copyTo(dictionaryFile, overwrite = true)
                temporaryFile.delete()
            }
            dictionary = replacement
            isLoaded = true
        } catch (error: Exception) {
            temporaryFile.delete()
            throw error
        }
    }

    fun lookup(text: String, source: UiLanguage, target: UiLanguage): OfflineLookupResult =
        dictionary.lookup(text, source, target)

    fun entries(): List<DictionaryEntry> = dictionary.displayEntries()

    private fun readEntries(file: File): List<Map<String, String>> {
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        return file.reader().use { Gson().fromJson(it, type) }
    }
}
