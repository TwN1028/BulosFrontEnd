package com.example.bulosfrontend

import java.io.File
import java.io.FileInputStream
import org.vosk.Recognizer

data class ValidatedPcmWav(val pcmOffset: Int, val pcmBytes: Int)

object RecordedPcmWavValidator {
    fun validate(file: File): ValidatedPcmWav {
        require(file.isFile && file.length() >= PcmWavFile.HEADER_BYTES) { "A non-empty WAV recording is required." }
        val header = file.inputStream().use { it.readNBytes(PcmWavFile.HEADER_BYTES) }
        require(header.copyOfRange(0, 4).decodeToString() == "RIFF" && header.copyOfRange(8, 12).decodeToString() == "WAVE") { "Invalid WAV header." }
        require(leShort(header, 20) == 1 && leShort(header, 22) == 1 && leInt(header, 24) == HybridSttAudioFormat.SAMPLE_RATE_HZ && leShort(header, 34) == HybridSttAudioFormat.BITS_PER_SAMPLE) { "WAV must be 16 kHz mono PCM 16-bit." }
        val bytes = leInt(header, 40)
        require(bytes > 0 && bytes <= HybridSttAudioFormat.MAX_PCM_BYTES && file.length() == (PcmWavFile.HEADER_BYTES + bytes).toLong()) { "Invalid or truncated WAV payload." }
        return ValidatedPcmWav(PcmWavFile.HEADER_BYTES, bytes)
    }
    private fun leInt(b: ByteArray, o: Int) = (0 until 4).sumOf { (b[o + it].toInt() and 255) shl (it * 8) }
    private fun leShort(b: ByteArray, o: Int) = (b[o].toInt() and 255) or ((b[o + 1].toInt() and 255) shl 8)
}

/** Offline-only recognizer for finalized WAV files; never acquires microphone hardware. */
class RecordedAudioVoskAdapter(private val models: VoskModelManager) {
    fun modelLanguage(language: String): String = when {
        language.equals("English", true) -> "English"
        language.equals("Filipino", true) || language.equals("Bulos", true) -> "Filipino"
        else -> throw IllegalArgumentException("Unsupported offline speech language: $language")
    }

    fun transcribe(file: File, language: String, cancelled: () -> Boolean = { false }): String {
        val audio = RecordedPcmWavValidator.validate(file)
        val modelLanguage = modelLanguage(language)
        require(models.isInstalled(modelLanguage)) { "Offline $modelLanguage speech model is not installed." }
        val model = models.load(modelLanguage)
        val recognizer = Recognizer(model, HybridSttAudioFormat.SAMPLE_RATE_HZ.toFloat())
        return try {
            FileInputStream(file).use { input ->
                input.skip(audio.pcmOffset.toLong())
                val buffer = ByteArray(3_200)
                var remaining = audio.pcmBytes
                while (remaining > 0 && !cancelled()) {
                    val count = input.read(buffer, 0, minOf(buffer.size, remaining))
                    if (count <= 0) break
                    val samples = ShortArray(count / 2) { index -> ((buffer[index * 2].toInt() and 255) or (buffer[index * 2 + 1].toInt() shl 8)).toShort() }
                    recognizer.acceptWaveForm(samples, samples.size)
                    remaining -= count
                }
                if (cancelled()) "" else recognizer.finalResult
            }
        } finally { recognizer.close(); model.close() }
    }
}
