package com.example.bulosfrontend

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import java.io.IOException
import kotlin.math.max

/** Streams smaller microphone chunks than Vosk's default Android SpeechService. */
class LowLatencyVoskSpeechService(
    private val recognizer: Recognizer,
    sampleRate: Float,
) {
    private val sampleRate = sampleRate.toInt()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val readBuffer = ShortArray((this.sampleRate * CHUNK_SECONDS).toInt())
    private val recorder = createRecorder()
    @Volatile private var running = false
    @Volatile private var releaseRequested = false
    private var worker: Thread? = null

    fun startListening(listener: RecognitionListener) {
        if (running) return
        running = true
        worker = Thread({ recognize(listener) }, "low-latency-vosk").also(Thread::start)
    }

    fun stop() {
        running = false
        worker?.interrupt()
    }

    fun shutdown() {
        releaseRequested = true
        running = false
        worker?.interrupt()
        if (worker?.isAlive != true) releaseResources()
    }

    private fun recognize(listener: RecognitionListener) {
        var lastPartial = ""
        var failed = false
        try {
            recorder.startRecording()
            if (recorder.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                throw IOException("Failed to start recording. Microphone might already be in use.")
            }
            while (running && !Thread.currentThread().isInterrupted) {
                val count = recorder.read(readBuffer, 0, readBuffer.size)
                if (count < 0) throw IOException("Unable to read microphone audio ($count).")
                if (count == 0) continue
                if (recognizer.acceptWaveForm(readBuffer, count)) {
                    val result = recognizer.result
                    lastPartial = ""
                    mainHandler.post { listener.onResult(result) }
                } else {
                    val partial = recognizer.partialResult
                    if (partial != lastPartial) {
                        lastPartial = partial
                        mainHandler.post { listener.onPartialResult(partial) }
                    }
                }
            }
        } catch (error: Exception) {
            failed = true
            mainHandler.post { listener.onError(error) }
        } finally {
            if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                runCatching { recorder.stop() }
            }
            if (!failed) {
                val finalResult = runCatching { recognizer.finalResult }.getOrDefault("")
                mainHandler.post { listener.onFinalResult(finalResult) }
            }
            if (releaseRequested) releaseResources()
        }
    }

    @SuppressLint("MissingPermission")
    private fun createRecorder(): AudioRecord {
        val minimumBytes = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minimumBytes <= 0) throw IOException("This device does not support microphone recording.")
        return AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            max(minimumBytes, readBuffer.size * 2),
        ).also {
            if (it.state != AudioRecord.STATE_INITIALIZED) {
                it.release()
                throw IOException("Failed to initialize the microphone.")
            }
        }
    }

    @Synchronized
    private fun releaseResources() {
        runCatching { recorder.release() }
        runCatching { recognizer.close() }
        worker = null
    }

    private companion object {
        const val CHUNK_SECONDS = 0.10f
    }
}
