package com.example.bulosfrontend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UiLanguageTest {
    @Test
    fun storedLanguageIdentifiersAreStable() {
        assertEquals(UiLanguage.ENGLISH, UiLanguage.fromStoredValue("ENGLISH"))
        assertEquals(UiLanguage.FILIPINO, UiLanguage.fromStoredValue("FILIPINO"))
        assertEquals(UiLanguage.BULOS, UiLanguage.fromStoredValue("BULOS"))
    }

    @Test
    fun unknownStoredLanguageRequiresSelection() {
        assertNull(UiLanguage.fromStoredValue(null))
        assertNull(UiLanguage.fromStoredValue("unknown"))
    }
}
