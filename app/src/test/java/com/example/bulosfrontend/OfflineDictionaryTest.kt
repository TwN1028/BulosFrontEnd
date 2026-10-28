package com.example.bulosfrontend

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
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

    @Before
    fun setUp() {
        BulosGrammarRuleRegistry.clear()
    }

    @After
    fun tearDown() {
        BulosGrammarRuleRegistry.clear()
    }

    @Test
    fun normalizedExactPhrasePrecedesDictionaryLookup() {
        val result = dictionary.lookup("  GOOD   MORNING?  ", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertEquals("Maabig a buklas?", (result as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_exact_phrase", result.method)
    }

    @Test
    fun normalizedExactDictionaryMatchHandlesCaseAndPunctuation() {
        val result = dictionary.lookup("BEAUTIFUL!", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertEquals("Masampat!", (result as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_exact_dictionary", result.method)
    }

    @Test
    fun typoResultsInOriginalTextFallbackDueToDisabledFuzzyMatching() {
        val result = dictionary.lookup("beautifol", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertFallback(result, "beautifol")
    }

    @Test
    fun phraseTypoResultsInOriginalTextFallbackDueToDisabledFuzzyMatching() {
        val result = dictionary.lookup("good mornin", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertFallback(result, "good mornin")
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
            "Maabig a buklas masampat gayyem",
        )
        assertExactComposite(
            "magandang umaga maganda kaibigan",
            UiLanguage.FILIPINO,
            UiLanguage.BULOS,
            "Maabig a buklas masampat gayyem",
        )
        assertExactComposite(
            "maabig a buklas masampat gayyem",
            UiLanguage.BULOS,
            UiLanguage.ENGLISH,
            "Good morning beautiful friend",
        )
        assertExactComposite(
            "maabig a buklas masampat gayyem",
            UiLanguage.BULOS,
            UiLanguage.FILIPINO,
            "Magandang umaga maganda kaibigan",
        )
    }

    @Test
    fun sequenceWithAnUnknownTokenPreservesTheUnknownTokenAndTranslatesVerifiedTerms() {
        val result = dictionary.lookup("good morning beautiful unknown", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertEquals(
            "Maabig a buklas masampat unknown",
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
    fun exactPhraseAndTemplateResultsReturnBeforeRules() {
        val testDict = OfflineDictionary(
            listOf(
                mapOf("_category" to "phrases", "English" to "good morning", "Bulos" to "maabig a buklas"),
                mapOf("_category" to "phrases", "Filipino" to "Ako si ___.", "Bulos" to "Aku i ___."),
            ),
        )
        BulosGrammarRuleRegistry.register(
            BulosGrammarRule(
                id = "rule-phrase-conflict",
                direction = RuleTranslationDirection.ENGLISH_TO_BULOS,
                description = "Phrase conflict rule",
                condition = { it.contains("good morning", ignoreCase = true) },
                transformation = { "rule-transformed" },
                priority = 1,
                isEnabled = true,
            ),
        )

        val phraseResult = testDict.lookup("good morning", UiLanguage.ENGLISH, UiLanguage.BULOS)
        assertEquals("Maabig a buklas", (phraseResult as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_exact_phrase", phraseResult.method)

        val templateResult = testDict.lookup("Ako si Angelo!", UiLanguage.FILIPINO, UiLanguage.BULOS)
        assertEquals("Aku i Angelo!", (templateResult as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_template_match", templateResult.method)
    }

    @Test
    fun enabledGrammarRuleRunsBeforeLocalWordMatchingAndPivot() {
        val testDict = OfflineDictionary(
            listOf(
                mapOf("English" to "coat", "Bulos" to "coat-bul"),
            ),
        )
        BulosGrammarRuleRegistry.register(
            BulosGrammarRule(
                id = "rule-runs-before-local-word",
                direction = RuleTranslationDirection.ENGLISH_TO_BULOS,
                description = "Runs before local word match",
                condition = { it.equals("coat", ignoreCase = true) },
                transformation = { "rule-coat" },
                priority = 1,
                isEnabled = true,
            ),
        )

        val result = testDict.lookup("coat", UiLanguage.ENGLISH, UiLanguage.BULOS)
        assertEquals("rule-coat", (result as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_grammar_rule", result.method)
    }

    @Test
    fun disabledAndWrongDirectionRulesDoNotRun() {
        BulosGrammarRuleRegistry.register(
            BulosGrammarRule(
                id = "disabled-rule",
                direction = RuleTranslationDirection.ENGLISH_TO_BULOS,
                description = "Disabled rule",
                condition = { it.equals("coat", ignoreCase = true) },
                transformation = { "disabled-coat" },
                priority = 1,
                isEnabled = false,
            ),
        )
        BulosGrammarRuleRegistry.register(
            BulosGrammarRule(
                id = "wrong-direction-rule",
                direction = RuleTranslationDirection.FILIPINO_TO_BULOS,
                description = "Wrong direction rule",
                condition = { it.equals("coat", ignoreCase = true) },
                transformation = { "wrong-direction-coat" },
                priority = 1,
                isEnabled = true,
            ),
        )

        val result = dictionary.lookup("coat", UiLanguage.ENGLISH, UiLanguage.BULOS)
        assertEquals("Coat-bul", (result as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_exact_dictionary", result.method)
    }

    @Test
    fun englishToBulosPerformsDirectMatchingBeforePivotWhenIncomplete() {
        val testDict = OfflineDictionary(
            listOf(
                mapOf("English" to "beautiful", "Bulos" to "masampat"),
                mapOf("English" to "rain", "Filipino" to "ulan"),
                mapOf("Filipino" to "ulan", "Bulos" to "oran"),
            ),
        )

        // Complete direct English -> Bulos match
        val directResult = testDict.lookup("beautiful", UiLanguage.ENGLISH, UiLanguage.BULOS)
        assertEquals("Masampat", (directResult as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_exact_dictionary", directResult.method)

        // Incomplete direct match pivots through English -> Filipino -> Bulos
        val pivotedResult = testDict.lookup("rain", UiLanguage.ENGLISH, UiLanguage.BULOS)
        assertEquals("Oran", (pivotedResult as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_bridge_english_filipino", pivotedResult.method)
    }

    @Test
    fun filipinoToBulosDoesNotPivotThroughEnglish() {
        val testDict = OfflineDictionary(
            listOf(
                mapOf("English" to "rain", "Filipino" to "ulan", "Bulos" to "oran"),
            ),
        )

        val result = testDict.lookup("unmapped-filipino-word", UiLanguage.FILIPINO, UiLanguage.BULOS)
        assertEquals("unmapped-filipino-word", (result as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_original_fallback", result.method)
    }

    @Test
    fun fallbackOutputIsNotOverwrittenByALaterStage() {
        val result = dictionary.lookup("unresolved123", UiLanguage.FILIPINO, UiLanguage.BULOS)
        assertEquals("unresolved123", (result as OfflineLookupResult.Translation).translatedText)
        assertEquals("offline_original_fallback", result.method)
        assertEquals(1.0, result.confidence, 0.0)
    }

    @Test
    fun bulosToEnglishExactOutputsForBuotPhraseVariations() {
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        val rows: List<Map<String, String>> = File("src/main/assets/verified_dictionary_additions.json")
            .reader()
            .use { Gson().fromJson(it, type) }
        val fullDict = OfflineDictionary(rows)

        val res1 = fullDict.lookup("buot ka eya", UiLanguage.BULOS, UiLanguage.ENGLISH) as OfflineLookupResult.Translation
        val res2 = fullDict.lookup("buot ko eya", UiLanguage.BULOS, UiLanguage.ENGLISH) as OfflineLookupResult.Translation
        val res3 = fullDict.lookup("buot ku eya", UiLanguage.BULOS, UiLanguage.ENGLISH) as OfflineLookupResult.Translation
        val res4 = fullDict.lookup("Buot ku ikaw", UiLanguage.BULOS, UiLanguage.ENGLISH) as OfflineLookupResult.Translation

        assertEquals("Gusto you he", res1.translatedText)
        assertEquals("Gusto ko he", res2.translatedText)
        assertEquals("Gusto ku he", res3.translatedText)
        assertEquals("I love you.", res4.translatedText)
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
