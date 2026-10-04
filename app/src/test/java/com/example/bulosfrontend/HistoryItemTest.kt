package com.example.bulosfrontend

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryItemTest {
    private val item = HistoryItem("English", "Bulos", "head", "ulù", 1L)

    @Test
    fun savedStateMatchesTheCompleteTranslationIdentity() {
        assertTrue(item.matchesTranslation("English", "Bulos", "head", "ulù"))
        assertFalse(item.matchesTranslation("English", "Bulos", "head", "uloan"))
        assertFalse(item.matchesTranslation("Bulos", "English", "head", "ulù"))
    }

    @Test
    fun favoriteStateDoesNotChangeSavedTranslationIdentity() {
        assertTrue(item.copy(isFavorite = true).matchesTranslation("English", "Bulos", "head", "ulù"))
    }
}
