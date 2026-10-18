package com.example.bulosfrontend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineDictionaryTest {
    private val dictionary = OfflineDictionary(
        listOf(
            mapOf(
                "_category" to "common-phrases",
                "English" to "good morning",
                "Filipino" to "magandang umaga",
                "Bulos" to "maabig a buklas",
            ),
            mapOf("en" to "beautiful", "tl" to "maganda", "bul" to "masampat"),
            mapOf("en" to "friend", "tl" to "kaibigan", "bul" to "gayyem"),
            mapOf("en" to "coat", "tl" to "amerikana", "bul" to "coat-bul"),
            mapOf("en" to "boat", "tl" to "bangka", "bul" to "boat-bul"),
        ),
    )

    @Test
    fun normalizedExactPhrasePrecedesDictionaryLookup() {
        val result = dictionary.lookup("  GOOD   MORNING?  ", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertEquals("maabig a buklas?", (result as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_exact_phrase", result.method)
    }

    @Test
    fun normalizedExactDictionaryMatchHandlesCaseAndPunctuation() {
        val result = dictionary.lookup("BEAUTIFUL!", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertEquals("masampat!", (result as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_exact_dictionary", result.method)
    }

    @Test
    fun strictUniqueMinorTypoCanTranslateAutomatically() {
        val result = dictionary.lookup("beautifol", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertEquals("masampat", (result as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_strict_fuzzy_dictionary", result.method)
        assertTrue(result.confidence >= OfflineDictionary.AUTO_TRANSLATE_SIMILARITY)
    }

    @Test
    fun strictUniquePhraseTypoCanTranslateAutomatically() {
        val result = dictionary.lookup("good mornin", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertEquals("maabig a buklas", (result as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_strict_fuzzy_phrase", result.method)
        assertTrue(result.confidence >= OfflineDictionary.AUTO_TRANSLATE_SIMILARITY)
    }

    @Test
    fun ambiguousFuzzyMatchesUseTheOriginalTextInsteadOfSuggestions() {
        val result = dictionary.lookup("goat", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertFallback(result, "goat")
    }

    @Test
    fun unsupportedPairUsesOriginalTextFallback() {
        val result = dictionary.lookup("beautiful", UiLanguage.ENGLISH, UiLanguage.FILIPINO)

        assertFallback(result, "beautiful")
    }

    @Test
    fun completeSequencesUseOnlyVerifiedExactPhraseAndWordRows() {
        assertExactComposite(
            "good morning beautiful friend",
            UiLanguage.ENGLISH,
            UiLanguage.BULOS,
            "maabig a buklas masampat gayyem",
        )
        assertExactComposite(
            "magandang umaga maganda kaibigan",
            UiLanguage.FILIPINO,
            UiLanguage.BULOS,
            "maabig a buklas masampat gayyem",
        )
        assertExactComposite(
            "maabig a buklas masampat gayyem",
            UiLanguage.BULOS,
            UiLanguage.ENGLISH,
            "good morning beautiful friend",
        )
        assertExactComposite(
            "maabig a buklas masampat gayyem",
            UiLanguage.BULOS,
            UiLanguage.FILIPINO,
            "magandang umaga maganda kaibigan",
        )
    }

    @Test
    fun sequenceWithAnUnknownTokenPreservesTheUnknownTokenAndTranslatesVerifiedTerms() {
        val result = dictionary.lookup("good morning beautiful unknown", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertEquals(
            "maabig a buklas masampat unknown",
            (result as OfflineLookupResult.Translation).translatedText,
        )
        assertEquals("offline_bridge_english_filipino", result.method)
        assertEquals(0.75, result.confidence, 0.0)
    }

    @Test
    fun emptyCacheUsesOriginalTextFallback() {
        assertFallback(
            OfflineDictionary(emptyList()).lookup("friend", UiLanguage.ENGLISH, UiLanguage.BULOS),
            "friend",
        )
    }

    @Test
    fun exposesCachedRowsWithoutMetadataAndKeepsVerifiedSpelling() {
        val verified = OfflineDictionary(
            listOf(mapOf("_category" to "words", "English" to "Mother-in-law", "Bulos" to "Ma’abig")),
        )

        assertEquals(
            DictionaryEntry("Ma’abig", "", "Mother-in-law"),
            verified.displayEntries().single(),
        )
    }

    @Test
    fun conflictingNormalizedPhraseKeysUseOriginalTextInsteadOfSuggestions() {
        val conflicted = OfflineDictionary(
            listOf(
                mapOf("_category" to "phrases", "English" to "Source text", "Filipino" to "Pinagmulan", "Bulos" to "Unang salin"),
                mapOf("_category" to "phrases", "English" to "source   text!", "Filipino" to "Pinagmulan", "Bulos" to "Ibang salin"),
                mapOf("_category" to "phrases", "English" to "First English", "Filipino" to "Unang Filipino", "Bulos" to "Parehong Bulos"),
                mapOf("_category" to "phrases", "English" to "Second English", "Filipino" to "Ikalawang Filipino", "Bulos" to "parehong bulos"),
            ),
        )

        assertFallback(conflicted.lookup("SOURCE TEXT", UiLanguage.ENGLISH, UiLanguage.BULOS), "SOURCE TEXT")
        assertFallback(conflicted.lookup("pinagmulan", UiLanguage.FILIPINO, UiLanguage.BULOS), "pinagmulan")
        assertFallback(conflicted.lookup("Parehong Bulos", UiLanguage.BULOS, UiLanguage.ENGLISH), "Parehong Bulos")
        assertFallback(conflicted.lookup("Parehong Bulos", UiLanguage.BULOS, UiLanguage.FILIPINO), "Parehong Bulos")
    }

    @Test
    fun duplicateNormalizedKeysWithTheSameTranslationRemainExactMatches() {
        val duplicates = OfflineDictionary(
            listOf(
                mapOf("_category" to "phrases", "English" to "Good day", "Filipino" to "Magandang araw", "Bulos" to "Masdep a agew"),
                mapOf("_category" to "phrases", "English" to "good  day!", "Filipino" to "Magandang araw", "Bulos" to "Masdep a agew"),
            ),
        )

        val result = duplicates.lookup("GOOD DAY", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertEquals("Masdep a agew", (result as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_exact_phrase", result.method)
    }

    @Test
    fun templatePhrasesPreserveTheCapturedDisplayText() {
        val templates = OfflineDictionary(
            listOf(
                mapOf(
                    "_category" to "phrases",
                    "Filipino" to "Ako si ___.",
                    "Bulos" to "Aku i ___.",
                ),
            ),
        )

        val result = templates.lookup("Ako si Angelo!", UiLanguage.FILIPINO, UiLanguage.BULOS)

        assertEquals("Aku i Angelo!", (result as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_template_match", result.method)
    }

    @Test
    fun englishToFilipinoToBulosBridgeUsesOnlyActualIntermediateTranslations() {
        val bridge = OfflineDictionary(
            listOf(
                mapOf("English" to "rain", "Filipino" to "ulan"),
                mapOf("Filipino" to "ulan", "Bulos" to "oran"),
            ),
        )

        val result = bridge.lookup("rain", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertEquals("oran", (result as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_bridge_english_filipino", result.method)
    }

    @Test
    fun englishToBulosBridgeKeepsNumbersAndUsesFilipinoWithoutRefuzzyingBulos() {
        val bridge = OfflineDictionary(
            listOf(
                mapOf("English" to "iron", "Filipino" to "plantsa", "Bulos" to "palansa"),
                mapOf("English" to "house", "Filipino" to "bahay"),
                // This row must not be reached by re-processing the already translated
                // Bulos result "palansa" as a Filipino input.
                mapOf("Filipino" to "palansa", "Bulos" to "malangsa"),
            ),
        )

        val result = bridge.lookup("the iron house 100", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertEquals(
            "the palansa bahay 100",
            (result as OfflineLookupResult.Translation).translatedText,
        )
        assertEquals("offline_bridge_english_filipino", result.method)
        assertEquals(0.5, result.confidence, 0.0)
    }

    private fun assertExactComposite(
        input: String,
        source: UiLanguage,
        target: UiLanguage,
        expected: String,
    ) {
        val result = dictionary.lookup(input, source, target) as OfflineLookupResult.Translation
        assertEquals(expected, result.translatedText)
        assertEquals("offline_exact_composite", result.method)
    }

    private fun assertFallback(result: OfflineLookupResult, expected: String) {
        assertEquals(expected, (result as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_original_fallback", result.method)
        assertEquals(1.0, result.confidence, 0.0)
    }
}
