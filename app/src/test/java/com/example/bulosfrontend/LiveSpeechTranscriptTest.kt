package com.example.bulosfrontend

import org.junit.Assert.assertEquals
import org.junit.Test

class LiveSpeechTranscriptTest {
    @Test
    fun appendedWordsAnimateWithoutRestartingExistingText() {
        assertEquals(11, unchangedWordPrefixLength("hello world", "hello world again"))
    }

    @Test
    fun interimCorrectionRestartsAtChangedWordOnly() {
        assertEquals(6, unchangedWordPrefixLength("hello word", "hello world"))
    }

    @Test
    fun initialRecognitionAnimatesAllText() {
        assertEquals(0, unchangedWordPrefixLength("", "hello"))
    }

    @Test
    fun emptyCorrectionDoesNotRetainStalePrefix() {
        assertEquals(0, unchangedWordPrefixLength("hello", ""))
    }
}
