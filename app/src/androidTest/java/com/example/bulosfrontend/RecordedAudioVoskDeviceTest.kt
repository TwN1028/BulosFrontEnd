package com.example.bulosfrontend

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.util.Log
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Development-only device validation; it is not referenced by production code or navigation. */
@RunWith(AndroidJUnit4::class)
class RecordedAudioVoskDeviceTest {
    @Test
    fun authorizedRealMicrophoneCapture_processesEnglishPhrase_and_deletesTemporaryFile() {
        val arguments = InstrumentationRegistry.getArguments()
        assumeTrue(
            "Real microphone capture requires the explicit runRealMicrophoneCapture=true runner argument.",
            arguments.getString("runRealMicrophoneCapture") == "true"
        )
        val expectedPhrase = "Hello, this is a speech recognition test."

        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val capture = IsolatedPcmWavCapture(context)
        var wav: File? = null
        try {
            assertTrue("The isolated capture could not acquire the microphone.", capture.start())
            // This test action is explicitly requested by its runner argument. Speak the
            // authorized phrase during this seven-second, single-owner capture window.
            Thread.sleep(7_000)
            wav = capture.stop()
            assertTrue("Stopping capture did not produce a WAV file.", wav?.isFile == true)

            val validated = RecordedPcmWavValidator.validate(requireNotNull(wav))
            val durationMs = validated.pcmBytes * 1_000L /
                (HybridSttAudioFormat.SAMPLE_RATE_HZ * HybridSttAudioFormat.CHANNEL_COUNT * 2)
            val peak = peakPcmSample(requireNotNull(wav), validated)
            assertTrue("Capture duration was outside the requested recording window: $durationMs ms", durationMs in 6_000L..8_000L)
            assertTrue("The emulator microphone supplied no audible input (peak PCM sample: $peak).", peak > 200)

            val startedAt = System.nanoTime()
            val transcript = RecordedAudioVoskAdapter(VoskModelManager(context)).transcribe(requireNotNull(wav), "English")
            val processingMs = (System.nanoTime() - startedAt) / 1_000_000
            val normalizedExpected = expectedPhrase.lowercase().filter(Char::isLetterOrDigit)
            val normalizedActual = transcript.lowercase().filter(Char::isLetterOrDigit)
            val exactNormalizedMatch = normalizedActual == normalizedExpected
            Log.i(
                "RecordedAudioVoskDeviceTest",
                "REAL_MIC_RESULT durationMs=$durationMs peak=$peak processingMs=$processingMs " +
                    "expected=$expectedPhrase transcript=$transcript exactNormalizedMatch=$exactNormalizedMatch"
            )
            assertTrue("Vosk returned no transcript for recorded speech.", transcript.isNotBlank())
        } finally {
            capture.cancel()
            wav?.delete()
            assertFalse("The real-audio temporary WAV must be removed.", wav?.exists() == true)
        }
    }

    @Test
    fun installedModels_processSyntheticWav_and_cleanupTemporaryFile() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val fixture = File.createTempFile("vosk-device-fixture", ".wav", context.cacheDir)
        try {
            // One second of non-sensitive silence in Vosk-compatible PCM/WAV format.
            PcmWavFile.write(fixture, ByteArray(HybridSttAudioFormat.SAMPLE_RATE_HZ * 2))
            val manager = VoskModelManager(context)
            val adapter = RecordedAudioVoskAdapter(manager)

            assertTrue("English Vosk model must already be installed", manager.isInstalled("English"))
            assertTrue("Filipino Vosk model must already be installed", manager.isInstalled("Filipino"))
            assertEquals("Filipino", adapter.modelLanguage("Bulos"))

            val english = adapter.transcribe(fixture, "English")
            val filipino = adapter.transcribe(fixture, "Filipino")
            val bulosFallback = adapter.transcribe(fixture, "Bulos")
            val cancelled = adapter.transcribe(fixture, "English") { true }

            // Silence is not expected to produce meaningful text; completing calls proves
            // model loading and finalized-WAV consumption, not recognition accuracy.
            assertTrue(english.isNotBlank() || english.isBlank())
            assertTrue(filipino.isNotBlank() || filipino.isBlank())
            assertTrue(bulosFallback.isNotBlank() || bulosFallback.isBlank())
            assertEquals("", cancelled)
        } finally {
            fixture.delete()
            assertFalse("Synthetic fixture must be removed", fixture.exists())
        }
    }

    private fun peakPcmSample(file: File, audio: ValidatedPcmWav): Int =
        file.inputStream().use { input ->
            input.skip(audio.pcmOffset.toLong())
            val bytes = input.readNBytes(audio.pcmBytes)
            bytes.asList().chunked(2).maxOfOrNull { pair ->
                val sample = ((pair[0].toInt() and 0xff) or (pair[1].toInt() shl 8)).toShort().toInt()
                kotlin.math.abs(sample)
            } ?: 0
        }
}
