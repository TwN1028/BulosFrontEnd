package com.example.bulosfrontend

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper

private const val RECORDING_START_CUE_DURATION_MS = 40
internal const val RECORDING_START_CUE_LEAD_MS = 70L

/** Plays a brief, device-volume-respecting cue before microphone capture begins. */
internal fun playRecordingStartCue(context: Context): Boolean {
    val audioManager = context.getSystemService(AudioManager::class.java) ?: return false
    if (
        audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL ||
        audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) == 0
    ) return false

    val toneGenerator = runCatching {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 35)
    }.getOrNull() ?: return false

    if (!toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, RECORDING_START_CUE_DURATION_MS)) {
        toneGenerator.release()
        return false
    }

    Handler(Looper.getMainLooper()).postDelayed(
        { toneGenerator.release() },
        RECORDING_START_CUE_LEAD_MS,
    )
    return true
}
