package com.example.bulosfrontend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DictionaryScreenTest {
    private val entries = listOf(
        DictionaryEntry(bulos = "ulù", filipino = "ulo", english = "head"),
        DictionaryEntry(bulos = "uloan", filipino = "unan", english = "pillow"),
        DictionaryEntry(bulos = "maulù", filipino = "matigas ang ulo", english = "stubborn"),
        DictionaryEntry(bulos = "ulù", filipino = "pangulo", english = "leader"),
    )

    @Test
    fun verifiedFieldsMapToTheirOwnLanguages() {
        val entry = entries.first()
        assertEquals("ulù", entry.value(UiLanguage.BULOS))
        assertEquals("ulo", entry.value(UiLanguage.FILIPINO))
        assertEquals("head", entry.value(UiLanguage.ENGLISH))
    }

    @Test
    fun dictionarySelectorsUseExactlyTheFourSupportedPairs() {
        assertEquals(listOf(UiLanguage.BULOS), dictionaryTargetOptions(UiLanguage.ENGLISH))
        assertEquals(listOf(UiLanguage.BULOS), dictionaryTargetOptions(UiLanguage.FILIPINO))
        assertEquals(
            listOf(UiLanguage.ENGLISH, UiLanguage.FILIPINO),
            dictionaryTargetOptions(UiLanguage.BULOS),
        )
    }

    @Test
    fun emptyQuerySortsAlphabeticallyBySelectedSource() {
        assertEquals(
            listOf("head", "leader", "pillow", "stubborn"),
            dictionaryResults(entries, UiLanguage.ENGLISH, "").map { it.english },
        )
        assertEquals(
            listOf("matigas ang ulo", "pangulo", "ulo", "unan"),
            dictionaryResults(entries, UiLanguage.FILIPINO, "  ").map { it.filipino },
        )
    }

    @Test
    fun searchRanksExactThenPrefixThenSubstringAndAlphabetizesTies() {
        val results = dictionaryResults(entries, UiLanguage.BULOS, "ul")

        assertEquals(listOf("pillow", "head", "leader", "stubborn"), results.map { it.english })
    }

    @Test
    fun searchUsesOnlySelectedSourceFieldAndUpdatesWhenSourceChanges() {
        assertEquals(listOf("head"), dictionaryResults(entries, UiLanguage.ENGLISH, "head").map { it.english })
        assertTrue(dictionaryResults(entries, UiLanguage.BULOS, "head").isEmpty())
    }

    @Test
    fun missingValuesRemainMissingAndDuplicateMeaningsRemainSeparate() {
        val missing = DictionaryEntry(bulos = "", filipino = "salita", english = "word")
        assertEquals("", missing.value(UiLanguage.BULOS))
        assertEquals(2, dictionaryResults(entries, UiLanguage.BULOS, "ulù").count { it.bulos == "ulù" })
    }
}
