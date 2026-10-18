package com.example.bulosfrontend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationLanguageRulesTest {
    private val supported = setOf(
        TranslationLanguagePair("Bulos", "English"),
        TranslationLanguagePair("Bulos", "Filipino"),
        TranslationLanguagePair("English", "Bulos"),
        TranslationLanguagePair("Filipino", "Bulos"),
    )

    @Test
    fun exactlyFourPairsAreSupported() {
        assertEquals(supported, TranslationLanguageRules.supportedPairs)
        TranslationLanguageRules.supportedLanguages.forEach { source ->
            TranslationLanguageRules.supportedLanguages.forEach { target ->
                assertEquals(
                    TranslationLanguagePair(source, target) in supported,
                    TranslationLanguageRules.isValidPair(source, target),
                )
            }
        }
    }

    @Test
    fun targetOptionsDependOnSource() {
        assertEquals(listOf("English", "Filipino"), TranslationLanguageRules.targetOptions("Bulos"))
        assertEquals(listOf("Bulos"), TranslationLanguageRules.targetOptions("English"))
        assertEquals(listOf("Bulos"), TranslationLanguageRules.targetOptions("Filipino"))
    }

    @Test
    fun sourceChangesCorrectTargetsAndPreservePreferredBulosTarget() {
        assertEquals(
            TranslationLanguagePair("Filipino", "Bulos"),
            TranslationLanguageRules.selectSource(TranslationLanguagePair("Bulos", "English"), "Filipino"),
        )
        assertEquals(
            TranslationLanguagePair("Bulos", "Filipino"),
            TranslationLanguageRules.selectSource(
                TranslationLanguagePair("English", "Bulos"),
                "Bulos",
                preferredBulosTarget = "Filipino",
            ),
        )
        assertEquals(
            TranslationLanguagePair("Bulos", "English"),
            TranslationLanguageRules.selectSource(TranslationLanguagePair("English", "Bulos"), "Bulos"),
        )
    }

    @Test
    fun sourceChangePrefersDifferentCurrentUiLanguageAsTarget() {
        assertEquals(
            TranslationLanguagePair("Bulos", "Filipino"),
            TranslationLanguageRules.selectSource(
                current = TranslationLanguagePair("English", "Bulos"),
                selected = "Bulos",
                preferredTarget = "Filipino",
            ),
        )
        assertEquals(
            TranslationLanguagePair("Bulos", "English"),
            TranslationLanguageRules.selectSource(
                current = TranslationLanguagePair("Filipino", "Bulos"),
                selected = "Bulos",
                preferredTarget = "English",
            ),
        )
        assertEquals(
            TranslationLanguagePair("English", "Bulos"),
            TranslationLanguageRules.selectSource(
                current = TranslationLanguagePair("Bulos", "Filipino"),
                selected = "English",
                preferredTarget = "English",
            ),
        )
    }

    @Test
    fun restoredUnsupportedPairsAreNormalized() {
        assertEquals(
            TranslationLanguagePair("English", "Bulos"),
            TranslationLanguageRules.normalize(TranslationLanguagePair("English", "Filipino")),
        )
        assertEquals(
            TranslationLanguagePair("Filipino", "Bulos"),
            TranslationLanguageRules.normalize(TranslationLanguagePair("Filipino", "English")),
        )
        assertEquals(
            TranslationLanguagePair("Bulos", "Filipino"),
            TranslationLanguageRules.normalize(
                TranslationLanguagePair("Bulos", "Bulos"),
                preferredBulosTarget = "Filipino",
            ),
        )
        assertEquals(
            TranslationLanguagePair("English", "Bulos"),
            TranslationLanguageRules.normalize(TranslationLanguagePair("Unknown", "Unknown")),
        )
    }

    @Test
    fun swapReversesEverySupportedPair() {
        supported.forEach { pair ->
            val swapped = TranslationLanguageRules.swap(pair)
            assertEquals(TranslationLanguagePair(pair.target, pair.source), swapped)
            assertTrue(TranslationLanguageRules.isValidPair(swapped.source, swapped.target))
        }
    }

    @Test
    fun stateRemembersLastBulosTargetAndRepairsInvalidState() {
        val original = currentStatePair()
        try {
            TranslationState.sourceLanguage = "Bulos"
            TranslationState.targetLanguage = "English"
            TranslationState.selectTargetLanguage("Filipino")
            TranslationState.selectSourceLanguage("English")
            assertEquals(TranslationLanguagePair("English", "Bulos"), currentStatePair())
            TranslationState.selectSourceLanguage("Bulos")
            assertEquals(TranslationLanguagePair("Bulos", "Filipino"), currentStatePair())

            TranslationState.sourceLanguage = "English"
            TranslationState.targetLanguage = "Filipino"
            assertFalse(TranslationState.hasValidLanguagePair())
            TranslationState.ensureValidLanguagePair()
            assertEquals(TranslationLanguagePair("English", "Bulos"), currentStatePair())
        } finally {
            TranslationState.sourceLanguage = original.source
            TranslationState.targetLanguage = original.target
            TranslationState.ensureValidLanguagePair()
        }
    }

    @Test
    fun everyUiLanguageMapsToItsTranslationLanguage() {
        assertEquals("English", TranslationLanguageRules.languageForUi(UiLanguage.ENGLISH))
        assertEquals("Filipino", TranslationLanguageRules.languageForUi(UiLanguage.FILIPINO))
        assertEquals("Bulos", TranslationLanguageRules.languageForUi(UiLanguage.BULOS))
    }

    @Test
    fun clearingTextTranslationContentPreservesTheSelectedLanguagePair() {
        val originalPair = currentStatePair()
        val originalInput = TranslationState.textToTranslate
        val originalOutput = TranslationState.translatedText
        try {
            TranslationState.sourceLanguage = "Bulos"
            TranslationState.targetLanguage = "Filipino"
            TranslationState.textToTranslate = "Previous input"
            TranslationState.translatedText = "Previous output"

            TranslationState.clearTextTranslationContent()

            assertEquals(TranslationLanguagePair("Bulos", "Filipino"), currentStatePair())
            assertEquals("", TranslationState.textToTranslate)
            assertEquals("", TranslationState.translatedText)
        } finally {
            TranslationState.sourceLanguage = originalPair.source
            TranslationState.targetLanguage = originalPair.target
            TranslationState.textToTranslate = originalInput
            TranslationState.translatedText = originalOutput
        }
    }

    private fun currentStatePair() =
        TranslationLanguagePair(TranslationState.sourceLanguage, TranslationState.targetLanguage)
}
