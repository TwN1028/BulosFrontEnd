package com.example.bulosfrontend

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordedPcmWavValidatorTest {
    @Test fun `valid wav exposes pcm payload only`() {
        val file = wav(ByteArray(3_200))
        val value = RecordedPcmWavValidator.validate(file)
        assertEquals(44, value.pcmOffset); assertEquals(3_200, value.pcmBytes)
    }
    @Test fun `truncated and empty wav are rejected`() {
        val truncated = File.createTempFile("wav", ".wav").apply { writeBytes(PcmWavFile.header(100)); deleteOnExit() }
        assertTrue(runCatching { RecordedPcmWavValidator.validate(truncated) }.isFailure)
    }
    @Test fun `wrong sample rate is rejected`() {
        val bytes = PcmWavFile.header(2).also { it[24] = 0x40; it[25] = 0x1f }
        val file = File.createTempFile("wav", ".wav").apply { writeBytes(bytes + byteArrayOf(0, 0)); deleteOnExit() }
        assertTrue(runCatching { RecordedPcmWavValidator.validate(file) }.isFailure)
    }
    private fun wav(pcm: ByteArray) = File.createTempFile("wav", ".wav").apply { PcmWavFile.write(this, pcm); deleteOnExit() }
}
