package com.example.bulosfrontend

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PcmWavFileTest {
    @Test fun `header is valid mono 16 khz pcm wav`() {
        val header = PcmWavFile.header(320)
        assertEquals("RIFF", header.copyOfRange(0, 4).decodeToString())
        assertEquals("WAVE", header.copyOfRange(8, 12).decodeToString())
        assertEquals(16_000, leInt(header, 24)); assertEquals(1, leShort(header, 22)); assertEquals(16, leShort(header, 34)); assertEquals(320, leInt(header, 40))
    }

    @Test fun `writing pcm creates header plus exact payload`() {
        val file = File.createTempFile("pcm-wav", ".wav").apply { deleteOnExit() }
        val pcm = ByteArray(640) { it.toByte() }
        PcmWavFile.write(file, pcm)
        assertEquals(PcmWavFile.HEADER_BYTES + pcm.size.toLong(), file.length())
        assertTrue(file.readBytes().copyOfRange(PcmWavFile.HEADER_BYTES, file.length().toInt()).contentEquals(pcm))
    }

    @Test fun `maximum is exactly one minute of pcm`() {
        assertEquals(1_920_000, HybridSttAudioFormat.MAX_PCM_BYTES)
        assertTrue(runCatching { PcmWavFile.header(HybridSttAudioFormat.MAX_PCM_BYTES + 1) }.isFailure)
    }

    private fun leInt(bytes: ByteArray, offset: Int) = (0 until 4).sumOf { (bytes[offset + it].toInt() and 0xff) shl (it * 8) }
    private fun leShort(bytes: ByteArray, offset: Int) = (bytes[offset].toInt() and 0xff) or ((bytes[offset + 1].toInt() and 0xff) shl 8)
}
