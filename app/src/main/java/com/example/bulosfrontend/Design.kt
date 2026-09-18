package com.example.bulosfrontend

import androidx.compose.animation.core.CubicBezierEasing
import kotlin.math.ln

object Design {
    // ASR Configuration
    val LanguageModelMap = mapOf(
        UiLanguage.ENGLISH to "model-en",
        UiLanguage.FILIPINO to "model-fil",
        UiLanguage.BULOS to "model-fil", // Temporarily use Filipino model
    )

    // Voice Visualization Constants
    const val VISUAL_NOISE_FLOOR = 0.08f
    const val ENVELOPE_EXPANSION_COEFFICIENT = 0.12f
    const val ENVELOPE_CONTRACTION_COEFFICIENT = 0.055f
    const val RADIUS_CHANGE_THRESHOLD = 0.008f
    const val AURA_EXPANSION_DURATION_MS = 170
    const val AURA_CONTRACTION_DURATION_MS = 320
    val AURA_MOVEMENT_EASING = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    fun normalizeAmplitude(rawAmplitude: Int): Float {
        val noiseFloor = 300f
        val cappedAmplitude = rawAmplitude.coerceIn(0, 32767).toFloat()
        if (cappedAmplitude <= noiseFloor) return 0f
        return ((ln(cappedAmplitude) - ln(noiseFloor)) / (ln(32767f) - ln(noiseFloor))).coerceIn(0f, 1f)
    }
}
