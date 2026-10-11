package com.example.bulosfrontend

import org.junit.Assert.assertEquals
import org.junit.Test

class TextNormalizerTest {
    @Test
    fun cleansDisplayWhitespaceWithoutChangingSpellingOrPunctuation() {
        assertEquals(
            "Magandang umaga , kaibigan !",
            TextNormalizer.cleanForDisplay("  Magandang\u00A0  umaga ,   kaibigan !  "),
        )
    }

    @Test
    fun composesUnicodeWhilePreservingAccentsAndApostrophes() {
        assertEquals("Café ’to", TextNormalizer.cleanForDisplay("Cafe\u0301 ’to"))
    }

    @Test
    fun matchingIgnoresCaseSpacingQuotesAndSentencePunctuation() {
        assertEquals(
            "magandang umaga",
            TextNormalizer.normalizeForMatching("  “MAGANDANG   UMAGA?” "),
        )
    }

    @Test
    fun matchingPreservesMeaningfulApostrophesHyphensAndDiacritics() {
        assertEquals("café ma'abig-a", TextNormalizer.normalizeForMatching("CAFÉ ma’abig‐a"))
    }
}
