package com.example.bulosfrontend

import org.junit.Assert.assertEquals
import org.junit.Test

class AppFontSizeTest {
    @Test
    fun missingOrUnknownStoredValueDefaultsToMedium() {
        assertEquals(AppFontSize.MEDIUM, AppFontSize.fromStoredValue(null))
        assertEquals(AppFontSize.MEDIUM, AppFontSize.fromStoredValue("unknown"))
    }

    @Test
    fun everyFontSizeRoundTripsThroughItsStoredName() {
        AppFontSize.entries.forEach { fontSize ->
            assertEquals(fontSize, AppFontSize.fromStoredValue(fontSize.name))
        }
    }

    @Test
    fun fontSizeScaleFactorsMatchTheSupportedPreferences() {
        assertEquals(0.90f, AppFontSize.SMALL.scaleFactor)
        assertEquals(1.00f, AppFontSize.MEDIUM.scaleFactor)
        assertEquals(1.15f, AppFontSize.LARGE.scaleFactor)
    }
}
