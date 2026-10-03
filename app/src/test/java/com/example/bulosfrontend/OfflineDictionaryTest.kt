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
    fun ambiguousFuzzyMatchesAreSuggestionsNotTranslations() {
        val result = dictionary.lookup("goat", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertTrue(result is OfflineLookupResult.Suggestions)
        assertEquals(listOf("boat", "coat"), (result as OfflineLookupResult.Suggestions).candidates.map { it.sourceText })
        assertTrue(result.candidates.all { it.similarity == 0.75 && it.editDistance == 1 })
    }

    @Test
    fun unsupportedPairCannotReachDictionaryMatching() {
        val result = dictionary.lookup("beautiful", UiLanguage.ENGLISH, UiLanguage.FILIPINO)

        assertTrue(result is OfflineLookupResult.Unavailable)
    }

    @Test
    fun unsupportedSentenceIsNeverAssembledFromKnownWords() {
        val result = dictionary.lookup("good morning beautiful friend", UiLanguage.ENGLISH, UiLanguage.BULOS)

        assertTrue(result is OfflineLookupResult.Unavailable)
    }

    @Test
    fun emptyCacheExplainsThatNoOfflineDataExists() {
        assertTrue(
            OfflineDictionary(emptyList()).lookup("friend", UiLanguage.ENGLISH, UiLanguage.BULOS) is
                OfflineLookupResult.NoData,
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
}
