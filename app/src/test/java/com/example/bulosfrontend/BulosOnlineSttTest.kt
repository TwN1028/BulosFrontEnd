package com.example.bulosfrontend

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BulosOnlineSttTest {
    @Test
    fun `online Bulos never uses backend route as per local Vosk requirement`() {
        assertFalse(BulosOnlineStt.shouldUseBackend("Bulos", canUseOnlineServices = true))
        assertFalse(BulosOnlineStt.shouldUseBackend("Bulos", canUseOnlineServices = false))
        assertFalse(BulosOnlineStt.shouldUseBackend("English", canUseOnlineServices = true))
        assertFalse(BulosOnlineStt.shouldUseBackend("Filipino", canUseOnlineServices = true))
    }

    @Test
    fun `non-empty backend error is a failure even with HTTP success`() {
        assertTrue(runCatching { BulosOnlineStt.recognizedText(response("ignored", "model unavailable")) }.isFailure)
    }

    @Test
    fun `empty backend text is rejected`() {
        assertTrue(runCatching { BulosOnlineStt.recognizedText(response(" ")) }.isFailure)
    }

    @Test
    fun `valid backend text is normalized for existing transcript flow`() {
        assertEquals("Hello Bulos", BulosOnlineStt.recognizedText(response("  Hello   Bulos  ")))
    }

    private fun response(text: String, error: String? = null) = BackendSttTranscriptionResponse(
        text = text,
        segments = listOf(JsonParser.parseString("{}")),
        language = "bul",
        languageProbability = 0.9,
        duration = 1.0,
        error = error,
    )
}
