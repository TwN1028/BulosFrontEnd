package com.example.bulosfrontend

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class TextTranslationHeaderGradientTest {
    @Test
    fun textTranslationGradientEndsInScreenCream() {
        val stops = homeHeaderGradientStops(includeCreamTail = false)

        assertEquals(listOf(0.00f, 0.28f, 0.48f, 0.66f, 0.84f, 1.00f), stops.map { it.first })
        assertEquals(1.00f, stops.last().first, 0f)
        assertEquals(HomeContentCream, stops.last().second)
        assertEquals(Color(0xFFE6E5D6), stops[4].second)
    }
}
