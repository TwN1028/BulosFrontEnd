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
    private data class FuzzyCandidate(
        val suggestion: OfflineTranslationSuggestion,
        val isPhrase: Boolean,
    )

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
        ) return OfflineLookupResult.Translation(text, "offline_original_fallback", 1.0)
        if (rows.isEmpty()) return OfflineLookupResult.Translation(text, "offline_original_fallback", 1.0)

        val directResult = lookupDirect(text, source, target)
        if (directResult is OfflineLookupResult.Translation) {
            return directResult
        }

        translateViaFilipino(text, source, target)?.let { return it }
        if (directResult is OfflineLookupResult.Suggestions) return directResult
        return OfflineLookupResult.Translation(text, "offline_original_fallback", 1.0)
    }

    /**
     * Looks up a pair without applying the public supported-pair restriction.  This is used only
     * for the English -> Filipino -> Bulos bridge; it never returns the source text as a match.
     */
    private fun lookupDirect(text: String, source: UiLanguage, target: UiLanguage): OfflineLookupResult? {
        val normalizedInput = TextNormalizer.normalizeForMatching(text)
        if (normalizedInput.isEmpty()) return null
        val punctuation = trailingSentencePunctuation(text)

        exact(normalizedInput, source, target, phrase = true)?.let {
            return OfflineLookupResult.Translation(it + punctuation, "offline_exact_phrase", 1.0)
        }
        exact(normalizedInput, source, target, phrase = false)?.let {
            return OfflineLookupResult.Translation(it + punctuation, "offline_exact_dictionary", 1.0)
        }

        template(text, source, target)?.let {
            return OfflineLookupResult.Translation(it + punctuation, "offline_template_match", 1.0)
        }

        translateCompleteKnownSequence(text, source, target)?.let {
            return OfflineLookupResult.Translation(it + punctuation, "offline_exact_composite", 1.0)
        }

        val candidates = rows.asSequence()
            .mapNotNull { row ->
                val sourceText = row.values[source] ?: return@mapNotNull null
                val translatedText = row.values[target] ?: return@mapNotNull null
                if (sourceText.contains('_')) return@mapNotNull null
                val normalizedCandidate = TextNormalizer.normalizeForMatching(sourceText)
                if (normalizedCandidate.isEmpty()) return@mapNotNull null
                val distance = levenshteinDistance(normalizedInput, normalizedCandidate)
                val longestLength = maxOf(normalizedInput.length, normalizedCandidate.length)
                val similarity = if (longestLength == 0) 1.0 else 1.0 - distance.toDouble() / longestLength
                FuzzyCandidate(
                    suggestion = OfflineTranslationSuggestion(sourceText, translatedText, similarity, distance),
                    isPhrase = row.isPhrase,
                )
            }
            .sortedWith(
                compareByDescending<FuzzyCandidate> { it.suggestion.similarity }
                    .thenBy { it.suggestion.editDistance }
                    .thenBy { it.suggestion.sourceText },
            )
            .toList()

        val best = candidates.firstOrNull()
        val runnerUp = candidates.getOrNull(1)
        if (
            best != null &&
            normalizedInput.length >= MIN_FUZZY_LENGTH &&
            best.suggestion.similarity >= AUTO_TRANSLATE_SIMILARITY &&
            best.suggestion.editDistance <= maximumAutomaticEdits(normalizedInput.length) &&
            (runnerUp == null || best.suggestion.similarity - runnerUp.suggestion.similarity >= AMBIGUITY_MARGIN)
        ) {
            return OfflineLookupResult.Translation(
                best.suggestion.translatedText + punctuation,
                if (best.isPhrase) "offline_strict_fuzzy_phrase" else "offline_strict_fuzzy_dictionary",
                best.suggestion.similarity,
            )
        }

        // Do not surface fuzzy candidate lists in the translation or dictionary experience.
        // Only exact and uniquely safe automatic matches are eligible for translation.
        return null
    }

    /** Fills corresponding underscore placeholders only when the full template matches. */
    private fun template(
        text: String,
        source: UiLanguage,
        target: UiLanguage,
    ): String? = rows.asSequence().mapNotNull { row ->
        val sourceTemplate = row.values[source] ?: return@mapNotNull null
        val targetTemplate = row.values[target] ?: return@mapNotNull null
        if (!sourceTemplate.contains('_') || !targetTemplate.contains('_')) return@mapNotNull null

        val template = TextNormalizer.cleanForDisplay(sourceTemplate)
            .replace(Regex("[,.;:!?…]+$"), "")
        val input = TextNormalizer.cleanForDisplay(text)
            .replace(Regex("[,.;:!?…]+$"), "")
        val pattern = Regex(
            "^" + template.split(Regex("_+")).joinToString("(.+?)") { Regex.escape(it) } + "$",
            RegexOption.IGNORE_CASE,
        )
        val match = pattern.matchEntire(input) ?: return@mapNotNull null
        val captures = match.groupValues.drop(1).map(String::trim)
        if (captures.any(String::isBlank)) return@mapNotNull null

        var captureIndex = 0
        targetTemplate.trim().removeSuffix(".").replace(Regex("_+")) {
            captures.getOrNull(captureIndex++) ?: return@replace it.value
        }.takeIf { captureIndex == captures.size }
    }.distinct().singleOrNull()

    private fun translateViaFilipino(
        text: String,
        source: UiLanguage,
        target: UiLanguage,
    ): OfflineLookupResult.Translation? {
        if (source != UiLanguage.ENGLISH || target != UiLanguage.BULOS) return null

        // Match the backend's safe English -> Filipino -> Bulos fallback.  Work token by
        // token and only use verified exact dictionary rows: a translated Bulos term must never
        // be sent through a second fuzzy pass and numbers must pass through unchanged.
        val tokens = TextNormalizer.cleanForDisplay(text)
            .split(Regex("\\s+"))
            .filter(String::isNotBlank)
        if (tokens.isEmpty()) return null

        var resolvedTokenCount = 0
        val translated = mutableListOf<String>()
        var index = 0
        while (index < tokens.size) {
            // Keep verified direct phrases intact before considering individual words.
            val directPhrase = (tokens.size downTo index + 2).firstNotNullOfOrNull { endExclusive ->
                val phrase = tokens.subList(index, endExclusive).joinToString(" ")
                exact(
                    TextNormalizer.normalizeForMatching(phrase),
                    UiLanguage.ENGLISH,
                    UiLanguage.BULOS,
                    phrase = true,
                )?.let { translation -> endExclusive to translation }
            }
            if (directPhrase != null) {
                val (endExclusive, translation) = directPhrase
                translated += translation + trailingSentencePunctuation(tokens[endExclusive - 1])
                resolvedTokenCount += endExclusive - index
                index = endExclusive
                continue
            }

            val token = tokens[index]
            if (token.matches(Regex("\\d+(?:[.,]\\d+)?"))) {
                translated += token
                index += 1
                continue
            }

            val punctuation = trailingSentencePunctuation(token)
            val normalizedToken = TextNormalizer.normalizeForMatching(token)
            if (normalizedToken.isEmpty()) {
                translated += token
                index += 1
                continue
            }

            val directBulos = exact(normalizedToken, UiLanguage.ENGLISH, UiLanguage.BULOS, phrase = false)
            if (directBulos != null) {
                resolvedTokenCount += 1
                translated += directBulos + punctuation
                index += 1
                continue
            }

            val filipino = exact(normalizedToken, UiLanguage.ENGLISH, UiLanguage.FILIPINO, phrase = false)
            if (filipino == null) {
                translated += token
            } else {
                val bulos = exact(
                    TextNormalizer.normalizeForMatching(filipino),
                    UiLanguage.FILIPINO,
                    UiLanguage.BULOS,
                    phrase = false,
                )
                resolvedTokenCount += 1
                translated += (bulos ?: filipino) + punctuation
            }
            index += 1
        }
        if (resolvedTokenCount == 0) return null

        return OfflineLookupResult.Translation(
            translatedText = translated.joinToString(" "),
            method = "offline_bridge_english_filipino",
            confidence = resolvedTokenCount.toDouble() / tokens.size,
        )
    }

    /**
     * Composes a result only when the whole input is covered by verified exact phrase or word rows.
     * It never translates an unmatched token and does not use fuzzy matching for any phrase segment.
     */
    private fun translateCompleteKnownSequence(
        text: String,
        source: UiLanguage,
        target: UiLanguage,
    ): String? {
        val tokens = TextNormalizer.cleanForDisplay(text).split(Regex("\\s+")).filter(String::isNotBlank)
        if (tokens.size < 2) return null

        fun translateFrom(index: Int): List<String>? {
            if (index == tokens.size) return emptyList()

            for (endExclusive in tokens.size downTo index + 2) {
                val phrase = tokens.subList(index, endExclusive).joinToString(" ")
                val translatedPhrase = exact(
                    TextNormalizer.normalizeForMatching(phrase),
                    source,
                    target,
                    phrase = true,
                ) ?: continue
                val remaining = translateFrom(endExclusive) ?: continue
                return listOf(translatedPhrase) + remaining
            }

            val word = exact(
                TextNormalizer.normalizeForMatching(tokens[index]),
                source,
                target,
                phrase = false,
            ) ?: return null
            val remaining = translateFrom(index + 1) ?: return null
            return listOf(word) + remaining
        }

        return translateFrom(0)?.joinToString(" ")
    }

    fun displayEntries(): List<DictionaryEntry> = rows.map { row ->
        DictionaryEntry(
            bulos = row.values[UiLanguage.BULOS].orEmpty(),
            filipino = row.values[UiLanguage.FILIPINO].orEmpty(),
            english = row.values[UiLanguage.ENGLISH].orEmpty(),
        )
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

    private fun exact(normalizedInput: String, source: UiLanguage, target: UiLanguage, phrase: Boolean): String? {
        val translations = rows.asSequence()
            .filter { row ->
                row.isPhrase == phrase &&
                    row.values[source]?.let(TextNormalizer::normalizeForMatching) == normalizedInput
            }
            .mapNotNull { it.values[target] }
            .distinct()
            .toList()
        // A normalized source key with conflicting target values must never choose an arbitrary first row.
        return translations.singleOrNull()
    }

    companion object {
        const val CATEGORY_KEY = "_category"
        const val AUTO_TRANSLATE_SIMILARITY = 0.88
        const val AMBIGUITY_MARGIN = 0.08
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
        val cachedEntries = if (dictionaryFile.exists()) {
            runCatching { readEntries(dictionaryFile) }.getOrElse {
                isLoaded = false
                return@withContext
            }
        } else {
            emptyList()
        }
        runCatching { mergeVerifiedDictionaryAdditions(cachedEntries, readVerifiedAdditions()) }
            .onSuccess { entries ->
                dictionary = OfflineDictionary(entries)
                isLoaded = entries.isNotEmpty()
            }
            .onFailure { isLoaded = false }
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
