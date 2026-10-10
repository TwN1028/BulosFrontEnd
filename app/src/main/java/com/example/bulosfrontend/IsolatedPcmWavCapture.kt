package com.example.bulosfrontend

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream

/** Vosk-compatible PCM parameters for a future, opt-in hybrid STT session. */
object HybridSttAudioFormat {
    const val SAMPLE_RATE_HZ = 16_000
    const val CHANNEL_COUNT = 1
    const val BITS_PER_SAMPLE = 16
    const val MAX_DURATION_SECONDS = 60
    const val MAX_PCM_BYTES = SAMPLE_RATE_HZ * CHANNEL_COUNT * (BITS_PER_SAMPLE / 8) * MAX_DURATION_SECONDS
    const val WAV_MIME_TYPE = "audio/wav"
}

/** Pure WAV serialization, intentionally separate from microphone ownership for JVM testing. */
object PcmWavFile {
    const val HEADER_BYTES = 44

    fun write(destination: File, pcm: ByteArray) {
        require(pcm.size <= HybridSttAudioFormat.MAX_PCM_BYTES) { "Audio exceeds the maximum recording size." }
        destination.parentFile?.mkdirs()
        FileOutputStream(destination).use { output ->
            output.write(header(pcm.size))
            output.write(pcm)
        }
    }

    fun header(pcmBytes: Int): ByteArray {
        require(pcmBytes >= 0 && pcmBytes <= HybridSttAudioFormat.MAX_PCM_BYTES)
        val byteRate = HybridSttAudioFormat.SAMPLE_RATE_HZ * HybridSttAudioFormat.CHANNEL_COUNT * 2
        return ByteArray(HEADER_BYTES).also { bytes ->
            bytes.writeAscii(0, "RIFF")
            bytes.writeLeInt(4, 36 + pcmBytes)
            bytes.writeAscii(8, "WAVEfmt ")
            bytes.writeLeInt(16, 16)
            bytes.writeLeShort(20, 1)
            bytes.writeLeShort(22, HybridSttAudioFormat.CHANNEL_COUNT)
            bytes.writeLeInt(24, HybridSttAudioFormat.SAMPLE_RATE_HZ)
            bytes.writeLeInt(28, byteRate)
            bytes.writeLeShort(32, 2)
            bytes.writeLeShort(34, HybridSttAudioFormat.BITS_PER_SAMPLE)
            bytes.writeAscii(36, "data")
            bytes.writeLeInt(40, pcmBytes)
        }
    }

    private fun ByteArray.writeAscii(offset: Int, text: String) = text.forEachIndexed { index, char -> this[offset + index] = char.code.toByte() }
    private fun ByteArray.writeLeShort(offset: Int, value: Int) { this[offset] = value.toByte(); this[offset + 1] = (value shr 8).toByte() }
    private fun ByteArray.writeLeInt(offset: Int, value: Int) { repeat(4) { this[offset + it] = (value shr (it * 8)).toByte() } }
}

/**
 * Standalone microphone capture. It has no application call sites and must not run beside
 * another microphone owner. stop() returns a valid WAV; cancel() removes all temporary data.
 */
class IsolatedPcmWavCapture(private val context: Context) {
    private var recorder: AudioRecord? = null
    private var worker: Thread? = null
    private var rawFile: File? = null
    @Volatile private var running = false

    @Synchronized fun start(): Boolean {
        if (running || recorder != null || context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return false
        val minBytes = AudioRecord.getMinBufferSize(HybridSttAudioFormat.SAMPLE_RATE_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (minBytes <= 0) return false
        val audioRecord = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, HybridSttAudioFormat.SAMPLE_RATE_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(minBytes, 3_200))
        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) { audioRecord.release(); return false }
        val file = File(context.cacheDir, "hybrid_stt_${System.currentTimeMillis()}.pcm")
        recorder = audioRecord; rawFile = file; running = true
        worker = Thread({ capture(audioRecord, file) }, "isolated-pcm-wav-capture").also(Thread::start)
        return true
    }

    @Synchronized fun stop(): File? {
        val file = rawFile ?: return null
        running = false; runCatching { recorder?.stop() }; worker?.join(1_000); recorder?.release(); recorder = null; worker = null
        return runCatching { File(context.cacheDir, file.nameWithoutExtension + ".wav").also { PcmWavFile.write(it, file.readBytes()); file.delete() } }.getOrNull().also { rawFile = null }
    }

    @Synchronized fun cancel() { running = false; runCatching { recorder?.stop() }; worker?.join(1_000); recorder?.release(); recorder = null; worker = null; rawFile?.delete(); rawFile = null }

    private fun capture(audioRecord: AudioRecord, file: File) {
        val buffer = ByteArray(3_200)
        FileOutputStream(file).use { output ->
            audioRecord.startRecording()
            var written = 0
            while (running && written < HybridSttAudioFormat.MAX_PCM_BYTES) {
                val count = audioRecord.read(buffer, 0, minOf(buffer.size, HybridSttAudioFormat.MAX_PCM_BYTES - written))
                if (count <= 0) break
                output.write(buffer, 0, count); written += count
            }
        }
        running = false
    }
}
