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
        ) {
            if (source == UiLanguage.FILIPINO) {
                return OfflineLookupResult.Translation(text, "offline_filipino_original", 1.0)
            }
            return OfflineLookupResult.Translation(text, "offline_original_fallback", 1.0)
        }
        if (rows.isEmpty()) {
            if (source == UiLanguage.FILIPINO) {
                return OfflineLookupResult.Translation(text, "offline_filipino_original", 1.0)
            }
            return OfflineLookupResult.Translation(text, "offline_original_fallback", 1.0)
        }
        val normalizedInput = TextNormalizer.normalizeForMatching(text)
        if (normalizedInput.isEmpty()) {
            return OfflineLookupResult.Translation(text, "offline_original_fallback", 1.0)
        }
        val punctuation = trailingSentencePunctuation(text)
        val cleanedInput = TextNormalizer.cleanForDisplay(text)

        exact(normalizedInput, source, target, phrase = true)?.let {
            return OfflineLookupResult.Translation(it + punctuation, "offline_exact_phrase", 1.0)
        }
        exact(normalizedInput, source, target, phrase = false)?.let {
            return OfflineLookupResult.Translation(it + punctuation, "offline_exact_dictionary", 1.0)
        }

        // --- TIER 3: Subdividing Phrase-Based Chunk Match ---
        var currentText = cleanedInput
        val subdividedResult = subdivideAndTranslatePhrases(currentText, source, target)
        if (subdividedResult != currentText) {
            currentText = subdividedResult
        }

        // --- TIER 4, 5, & 6: Word-by-Word & Fuzzy Matching ---
        val tokens = currentText.split(Regex("\\s+"))
        val translatedTokens = tokens.map { token ->
            if (token.startsWith("[[P") && token.endsWith("]]")) return@map token

            val normToken = TextNormalizer.normalizeForMatching(token)
            if (normToken.isEmpty()) return@map token

            exact(normToken, source, target, phrase = false)?.let { return@map it }

            val wordMatch = Regex("^(.+?)([,.!?]+)?$").find(token)
            if (wordMatch != null) {
                val coreWord = wordMatch.groupValues[1]
                val punct = wordMatch.groupValues[2]
                val coreNorm = TextNormalizer.normalizeForMatching(coreWord)
                exact(coreNorm, source, target, phrase = false)?.let { return@map it + punct }
            }

            findFuzzyWord(normToken, source, target, 0.65)?.let { return@map it }
            findFuzzyWord(normToken, source, target, 0.45)?.let { return@map it }

            token
        }

        val finalResult = translatedTokens.joinToString(" ")
        if (finalResult.isNotBlank() && finalResult != cleanedInput) {
            return OfflineLookupResult.Translation(finalResult + punctuation, "offline_multi_tier_translation", 1.0)
        }

        // --- TIER 7: English -> Filipino -> Target Bridge ---
        if (source == UiLanguage.ENGLISH && target != UiLanguage.FILIPINO) {
            val filipinoResult = lookup(text, UiLanguage.ENGLISH, UiLanguage.FILIPINO)
            val filipinoText = when (filipinoResult) {
                is OfflineLookupResult.Translation -> filipinoResult.translatedText
                else -> text
            }
            if (filipinoText != text) {
                val finalResult = lookup(filipinoText, UiLanguage.FILIPINO, target)
                if (finalResult is OfflineLookupResult.Translation) {
                    return OfflineLookupResult.Translation(finalResult.translatedText + punctuation, "offline_bridge_english_filipino", 1.0)
                }
            }
        }

        return OfflineLookupResult.Translation(text, "offline_original_fallback", 1.0)
    }

    private fun findFuzzyWord(normalizedInput: String, source: UiLanguage, target: UiLanguage, threshold: Double): String? {
        if (normalizedInput.length < 2) return null
        var bestMatch: String? = null
        var highestSimilarity = threshold

        rows.forEach { row ->
            val sourceVal = row.values[source] ?: return@forEach
            val targetVal = row.values[target] ?: return@forEach
            val normCand = TextNormalizer.normalizeForMatching(sourceVal)
            if (normCand.isEmpty()) return@forEach
            val distance = levenshteinDistance(normalizedInput, normCand)
            val maxLen = maxOf(normalizedInput.length, normCand.length)
            if (maxLen == 0) return@forEach
            val similarity = 1.0 - (distance.toDouble() / maxLen.toDouble())
            if (similarity >= highestSimilarity) {
                highestSimilarity = similarity
                bestMatch = targetVal
            }
        }
        return bestMatch
    }

    private fun subdivideAndTranslatePhrases(
        text: String,
        source: UiLanguage,
        target: UiLanguage,
    ): String {
        val words = text.split(Regex("\\s+"))
        if (words.size <= 1) return text

        for (windowSize in words.size downTo 2) {
            for (i in 0..words.size - windowSize) {
                val subPhrase = words.subList(i, i + windowSize).joinToString(" ")
                val normSub = TextNormalizer.normalizeForMatching(subPhrase)
                val match = exact(normSub, source, target, phrase = true)
                    ?: exact(normSub, source, target, phrase = false)
                if (match != null) {
                    val leftPart = if (i > 0) words.subList(0, i).joinToString(" ") else ""
                    val rightPart = if (i + windowSize < words.size) words.subList(i + windowSize, words.size).joinToString(" ") else ""

                    val translatedLeft = if (leftPart.isNotEmpty()) subdivideAndTranslatePhrases(leftPart, source, target) else ""
                    val translatedRight = if (rightPart.isNotEmpty()) subdivideAndTranslatePhrases(rightPart, source, target) else ""

                    return listOfNotNull(
                        translatedLeft.takeIf { it.isNotEmpty() },
                        match,
                        translatedRight.takeIf { it.isNotEmpty() },
                    ).joinToString(" ")
                }
            }
        }
        return text
    }

    fun matchPhraseToBulos(text: String): Pair<String, Double>? {
        val norm = TextNormalizer.normalizeForMatching(text)
        return rows.asSequence()
            .filter { it.isPhrase }
            .mapNotNull { row ->
                val bulos = row.values[UiLanguage.BULOS]?.let(TextNormalizer::normalizeForMatching) ?: return@mapNotNull null
                val sim = 1.0 - (levenshteinDistance(norm, bulos).toDouble() / maxOf(norm.length, bulos.length, 1))
                Pair(row.values[UiLanguage.BULOS] ?: "", sim)
            }
            .maxByOrNull { it.second }
    }

    fun findBulosViaFilipino(text: String): Pair<String, Double>? {
        val norm = TextNormalizer.normalizeForMatching(text)
        return rows.asSequence()
            .mapNotNull { row ->
                val filipino = row.values[UiLanguage.FILIPINO]?.let(TextNormalizer::normalizeForMatching) ?: return@mapNotNull null
                val sim = 1.0 - (levenshteinDistance(norm, filipino).toDouble() / maxOf(norm.length, filipino.length, 1))
                Pair(row.values[UiLanguage.BULOS] ?: "", sim)
            }
            .maxByOrNull { it.second }
    }

    fun findClosestBulosWord(text: String): Pair<String, Double>? {
        val norm = TextNormalizer.normalizeForMatching(text)
        return rows.asSequence()
            .mapNotNull { row ->
                val bulos = row.values[UiLanguage.BULOS]?.let(TextNormalizer::normalizeForMatching) ?: return@mapNotNull null
                val sim = 1.0 - (levenshteinDistance(norm, bulos).toDouble() / maxOf(norm.length, bulos.length, 1))
                Pair(row.values[UiLanguage.BULOS] ?: "", sim)
            }
            .maxByOrNull { it.second }
    }

    fun displayEntries(): List<DictionaryEntry> = rows.map { row ->
        DictionaryEntry(
            bulos = row.values[UiLanguage.BULOS].orEmpty(),
            filipino = row.values[UiLanguage.FILIPINO].orEmpty(),
            english = row.values[UiLanguage.ENGLISH].orEmpty(),
        )
    }

    private fun exact(normalizedInput: String, source: UiLanguage, target: UiLanguage, phrase: Boolean): String? {
        val translations = rows.asSequence()
            .filter { row ->
                row.isPhrase == phrase &&
                    row.values[source]?.let(TextNormalizer::normalizeForMatching) == normalizedInput
            }
            .mapNotNull { it.values[target] }
            .distinct()
            .toList()
        return translations.firstOrNull()
    }

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
    @Volatile private var dictionary: OfflineDictionary
    @Volatile var isLoaded: Boolean

    init {
        val initialAdditions = runCatching { readVerifiedAdditions() }.getOrDefault(emptyList())
        val fallbackEntries = if (initialAdditions.isEmpty()) {
            listOf(
                mapOf("English" to "good morning", "Filipino" to "magandang umaga", "Bulos" to "maabig a buklas"),
                mapOf("English" to "I am ___", "Filipino" to "Ako si ___.", "Bulos" to "Aku i ___."),
                mapOf("English" to "I", "Filipino" to "Ako", "Bulos" to "Aku"),
                mapOf("English" to "is / are", "Filipino" to "si", "Bulos" to "i"),
                mapOf("English" to "how are you", "Filipino" to "kamusta ka", "Bulos" to "magiano kamu di"),
            )
        } else {
            initialAdditions
        }
        dictionary = OfflineDictionary(fallbackEntries)
        isLoaded = fallbackEntries.isNotEmpty()
    }

    suspend fun load(): Unit = withContext(Dispatchers.IO) {
        val cachedEntries = if (dictionaryFile.exists()) {
            runCatching { readEntries(dictionaryFile) }.getOrElse { emptyList() }
        } else {
            emptyList()
        }
        runCatching { mergeVerifiedDictionaryAdditions(cachedEntries, readVerifiedAdditions()) }
            .onSuccess { entries ->
                if (entries.isNotEmpty()) {
                    dictionary = OfflineDictionary(entries)
                    isLoaded = true
                }
            }
    }

    suspend fun replace(entries: List<Map<String, String>>) = withContext(Dispatchers.IO) {
        require(entries.isNotEmpty()) { "Verified dictionary download was empty." }
        val replacement = OfflineDictionary(mergeVerifiedDictionaryAdditions(entries, readVerifiedAdditions()))
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

    fun matchPhraseToBulos(text: String): Pair<String, Double>? = dictionary.matchPhraseToBulos(text)
    fun findBulosViaFilipino(text: String): Pair<String, Double>? = dictionary.findBulosViaFilipino(text)
    fun findClosestBulosWord(text: String): Pair<String, Double>? = dictionary.findClosestBulosWord(text)

    fun entries(): List<DictionaryEntry> = dictionary.displayEntries()

    private fun readEntries(file: File): List<Map<String, String>> {
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        return file.reader().use { Gson().fromJson(it, type) }
    }

    private fun readVerifiedAdditions(): List<Map<String, String>> = runCatching<List<Map<String, String>>> {
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        context.assets.open(VERIFIED_ADDITIONS_ASSET).bufferedReader().use { reader ->
            Gson().fromJson<List<Map<String, String>>>(reader, type)
        }
    }.getOrDefault(emptyList())

    private companion object {
        const val VERIFIED_ADDITIONS_ASSET = "verified_dictionary_additions.json"
    }
}

/**
 * Appends only complete curated rows whose source keys cannot alter an existing first-match lookup.
 * The downloaded cache remains authoritative and is never rewritten with bundled additions.
 */
internal fun mergeVerifiedDictionaryAdditions(
    cachedEntries: List<Map<String, String>>,
    additions: List<Map<String, String>>,
): List<Map<String, String>> {
    val languages = listOf("English", "Filipino", "Bulos")
    fun sourceKeys(entry: Map<String, String>): Set<Pair<String, String>> = languages.mapNotNull { language ->
        entry[language]
            ?.takeIf(String::isNotBlank)
            ?.let(TextNormalizer::normalizeForMatching)
            ?.takeIf(String::isNotBlank)
            ?.let { language to it }
    }.toSet()

    val occupiedSourceKeys = cachedEntries.flatMapTo(mutableSetOf()) { sourceKeys(it) }
    val selected = additions.filter { entry ->
        val complete = languages.all { entry[it]?.isNotBlank() == true && entry[it] != "—" }
        val keys = sourceKeys(entry)
        val accepted = complete && keys.size == languages.size && keys.none(occupiedSourceKeys::contains)
        if (accepted) occupiedSourceKeys.addAll(keys)
        accepted
    }
    return cachedEntries + selected
}
