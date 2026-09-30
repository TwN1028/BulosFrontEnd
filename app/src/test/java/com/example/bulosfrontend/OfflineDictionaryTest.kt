package com.example.bulosfrontend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfflineDictionaryTest {
    private val dictionary = OfflineDictionary(
        listOf(
            mapOf("English" to "good morning", "Filipino" to "magandang umaga", "Bulos" to "maabig a buklas"),
            mapOf("en" to "friend", "tl" to "kaibigan", "bul" to "gayyem"),
        ),
    )

    @Test
    fun translatesExactPhraseAndPreservesPunctuation() {
        assertEquals(
            "magandang umaga!",
            dictionary.translate("Good morning!", UiLanguage.ENGLISH, UiLanguage.FILIPINO),
        )
    }

    @Test
    fun prefersPhraseThenTranslatesRemainingWords() {
        assertEquals(
            "maabig a buklas gayyem",
            dictionary.translate("good morning friend", UiLanguage.ENGLISH, UiLanguage.BULOS),
        )
    }

    @Test
    fun returnsNullWhenNothingCanBeTranslated() {
        assertNull(dictionary.translate("unknown", UiLanguage.ENGLISH, UiLanguage.BULOS))
    }
}
