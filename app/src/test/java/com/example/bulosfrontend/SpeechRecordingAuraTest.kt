package com.example.bulosfrontend

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechRecordingAuraTest {
    @Test
    fun compactScreenAuraNearlyFillsWidth() {
        assertEquals(338.4f, 360f * SPEECH_AURA_SCREEN_WIDTH_FRACTION, 0.001f)
        assertEquals(352.8f, 360f * SPEECH_AURA_MAX_SCREEN_WIDTH_FRACTION, 0.001f)
    }

    @Test
    fun tallScreenAuraNearlyFillsWidth() {
        assertEquals(387.28f, 412f * SPEECH_AURA_SCREEN_WIDTH_FRACTION, 0.001f)
        assertEquals(403.76f, 412f * SPEECH_AURA_MAX_SCREEN_WIDTH_FRACTION, 0.001f)
    }
}
