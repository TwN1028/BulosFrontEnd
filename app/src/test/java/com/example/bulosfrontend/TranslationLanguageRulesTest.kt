package com.example.bulosfrontend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationLanguageRulesTest {
    @Test
    fun everyIdenticalSupportedCombinationIsInvalid() {
        TranslationLanguageRules.supportedLanguages.forEach { language ->
            assertTrue(!TranslationLanguageRules.isValidPair(language, language))
        }
    }

    @Test
    fun everyDistinctSupportedCombinationIsValid() {
        val languages = TranslationLanguageRules.supportedLanguages

        languages.forEach { source ->
            languages.filterNot { it == source }.forEach { target ->
                assertTrue(TranslationLanguageRules.isValidPair(source, target))
            }
        }
    }

    @Test
    fun selectingSourcePreservesSelectionAndMovesMatchingTarget() {
        TranslationLanguageRules.supportedLanguages.forEach { selected ->
            val updated = TranslationLanguageRules.selectSource(
                current = TranslationLanguagePair("English", selected),
                selected = selected,
            )

            assertEquals(selected, updated.source)
            assertTrue(TranslationLanguageRules.isValidPair(updated.source, updated.target))
        }
    }

    @Test
    fun selectingSourcePreservesEveryTargetThatRemainsValid() {
        val languages = TranslationLanguageRules.supportedLanguages

        languages.forEach { currentTarget ->
            languages.filterNot { it == currentTarget }.forEach { selectedSource ->
                val updated = TranslationLanguageRules.selectSource(
                    current = TranslationLanguagePair("English", currentTarget),
                    selected = selectedSource,
                )

                assertEquals(selectedSource, updated.source)
                assertEquals(currentTarget, updated.target)
            }
        }
    }

    @Test
    fun selectingTargetPreservesSelectionAndMovesMatchingSource() {
        TranslationLanguageRules.supportedLanguages.forEach { selected ->
            val updated = TranslationLanguageRules.selectTarget(
                current = TranslationLanguagePair(selected, "Bulos"),
                selected = selected,
            )

            assertEquals(selected, updated.target)
            assertTrue(TranslationLanguageRules.isValidPair(updated.source, updated.target))
        }
    }

    @Test
    fun selectingTargetPreservesEverySourceThatRemainsValid() {
        val languages = TranslationLanguageRules.supportedLanguages

        languages.forEach { currentSource ->
            languages.filterNot { it == currentSource }.forEach { selectedTarget ->
                val updated = TranslationLanguageRules.selectTarget(
                    current = TranslationLanguagePair(currentSource, "Bulos"),
                    selected = selectedTarget,
                )

                assertEquals(currentSource, updated.source)
                assertEquals(selectedTarget, updated.target)
            }
        }
    }

    @Test
    fun changingEnglishSourceToFilipinoKeepsBulosTarget() {
        val updated = TranslationLanguageRules.selectSource(
            current = TranslationLanguagePair("English", "Bulos"),
            selected = "Filipino",
        )

        assertEquals(TranslationLanguagePair("Filipino", "Bulos"), updated)
    }

    @Test
    fun everyUiLanguageMapsToItsTranslationLanguage() {
        assertEquals("English", TranslationLanguageRules.languageForUi(UiLanguage.ENGLISH))
        assertEquals("Filipino", TranslationLanguageRules.languageForUi(UiLanguage.FILIPINO))
        assertEquals("Bulos", TranslationLanguageRules.languageForUi(UiLanguage.BULOS))
    }

    @Test
    fun uiLanguageChangePreservesEveryNonMatchingTarget() {
        UiLanguage.entries.forEach { uiLanguage ->
            val selectedSource = TranslationLanguageRules.languageForUi(uiLanguage)
            TranslationLanguageRules.supportedLanguages
                .filterNot { it == selectedSource }
                .forEach { currentTarget ->
                    val updated = TranslationLanguageRules.selectSource(
                        current = TranslationLanguagePair("English", currentTarget),
                        selected = selectedSource,
                    )

                    assertEquals(selectedSource, updated.source)
                    assertEquals(currentTarget, updated.target)
                }
        }
    }

    @Test
    fun uiLanguageChangeMovesOnlyAConflictingTarget() {
        UiLanguage.entries.forEach { uiLanguage ->
            val selectedSource = TranslationLanguageRules.languageForUi(uiLanguage)
            val updated = TranslationLanguageRules.selectSource(
                current = TranslationLanguagePair("English", selectedSource),
                selected = selectedSource,
            )

            assertEquals(selectedSource, updated.source)
            assertTrue(TranslationLanguageRules.isValidPair(updated.source, updated.target))
        }
    }

    @Test
    fun manualSourceSelectionRemainsUntilTheNextExplicitUiLanguageSync() {
        val originalSource = TranslationState.sourceLanguage
        val originalTarget = TranslationState.targetLanguage
        try {
            TranslationState.sourceLanguage = "English"
            TranslationState.targetLanguage = "Bulos"

            TranslationState.synchronizeSourceWithUiLanguage(UiLanguage.FILIPINO)
            assertEquals("Filipino", TranslationState.sourceLanguage)
            assertEquals("Bulos", TranslationState.targetLanguage)

            TranslationState.selectSourceLanguage("English")
            assertEquals("English", TranslationState.sourceLanguage)
            assertEquals("Bulos", TranslationState.targetLanguage)

            TranslationState.synchronizeSourceWithUiLanguage(UiLanguage.BULOS)
            assertEquals("Bulos", TranslationState.sourceLanguage)
            assertTrue(TranslationState.hasValidLanguagePair())
        } finally {
            TranslationState.sourceLanguage = originalSource
            TranslationState.targetLanguage = originalTarget
        }
    }

    @Test
    fun switchingLanguagesExchangesEveryValidPair() {
        val languages = TranslationLanguageRules.supportedLanguages

        languages.forEach { source ->
            languages.filterNot { it == source }.forEach { target ->
                val swapped = TranslationLanguageRules.swap(TranslationLanguagePair(source, target))

                assertEquals(target, swapped.source)
                assertEquals(source, swapped.target)
            }
        }
    }

    @Test
    fun sharedTranslationStateAppliesTheSameRulesAcrossScreens() {
        val originalSource = TranslationState.sourceLanguage
        val originalTarget = TranslationState.targetLanguage
        try {
            TranslationState.sourceLanguage = "English"
            TranslationState.targetLanguage = "Bulos"
            TranslationState.selectTargetLanguage("English")

            assertEquals("English", TranslationState.targetLanguage)
            assertTrue(TranslationState.hasValidLanguagePair())

            val beforeSwap = TranslationLanguagePair(
                TranslationState.sourceLanguage,
                TranslationState.targetLanguage,
            )
            TranslationState.swapLanguages()
            assertEquals(beforeSwap.target, TranslationState.sourceLanguage)
            assertEquals(beforeSwap.source, TranslationState.targetLanguage)
        } finally {
            TranslationState.sourceLanguage = originalSource
            TranslationState.targetLanguage = originalTarget
        }
    }
}
