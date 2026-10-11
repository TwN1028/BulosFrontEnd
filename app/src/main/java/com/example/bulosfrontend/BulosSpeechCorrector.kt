package com.example.bulosfrontend

import android.util.Log

/**
 * Post-processes speech transcription when Bulos is the source language.
 *
 * Since no Vosk model exists for Bulos, the Filipino model is used instead.
 * The transcribed text will sound like Filipino, so this corrector:
 *
 *   Step 1 — Phrase match: try to match the FULL sentence against known
 *            Bulos or Filipino phrases from the dataset.
 *   Step 2 — Filipino lookup: each word is matched against Filipino dictionary
 *            entries to find the corresponding Bulos word.
 *   Step 3 — Fuzzy Bulos: each remaining word is fuzzy-matched directly
 *            against Bulos dictionary entries.
 */
class BulosSpeechCorrector(private val dictionary: OfflineDictionaryManager) {

    /**
     * Correct transcribed text to Bulos using the 3-step strategy.
     *
     * @param transcribedText Raw text from Vosk Filipino model
     * @return Corrected text with Bulos words/phrases substituted
     */
    fun correct(transcribedText: String): String {
        if (!dictionary.isLoaded) {
            Log.w(TAG, "Dictionary not loaded — returning raw transcription")
            return transcribedText
        }

        val trimmed = transcribedText.trim()
        if (trimmed.isEmpty()) return trimmed

        // ── Step 1: Full phrase/sentence match ──
        val phraseMatch = dictionary.matchPhraseToBulos(trimmed)
        if (phraseMatch != null && phraseMatch.second >= PHRASE_THRESHOLD) {
            Log.d(TAG, "✅ PHRASE MATCH: '$trimmed' → '${phraseMatch.first}' (score=${phraseMatch.second})")
            return phraseMatch.first
        }
        if (phraseMatch != null) {
            Log.d(TAG, "❌ PHRASE REJECT: best='${phraseMatch.first}' (score=${phraseMatch.second} < $PHRASE_THRESHOLD)")
        }

        // ── Step 2 & 3: Word-by-word correction ──
        val words = trimmed.split(Regex("\\s+"))
        val corrected = words.map { word ->
            val clean = word.lowercase().replace(Regex("[,.!?;:]+$"), "")
            val trailing = if (clean.length < word.length) word.substring(clean.length) else ""

            if (clean.length < MIN_WORD_LENGTH) {
                Log.d(TAG, "  SKIP (too short): '$word'")
                return@map word
            }

            // Step 2: Filipino → Bulos lookup
            // (Vosk transcribes Bulos speech as Filipino-sounding words)
            val filipinoMatch = dictionary.findBulosViaFilipino(clean)
            if (filipinoMatch != null && filipinoMatch.second >= WORD_THRESHOLD) {
                Log.d(TAG, "  ✅ FILIPINO→BULOS: '$word' → '${filipinoMatch.first}' (score=${filipinoMatch.second})")
                return@map filipinoMatch.first + trailing
            }

            // Step 3: Direct fuzzy match against Bulos words
            val bulosMatch = dictionary.findClosestBulosWord(clean)
            if (bulosMatch != null && bulosMatch.second >= WORD_THRESHOLD) {
                Log.d(TAG, "  ✅ FUZZY BULOS: '$word' → '${bulosMatch.first}' (score=${bulosMatch.second})")
                return@map bulosMatch.first + trailing
            }

            // No match — keep original
            Log.d(TAG, "  ❌ NO MATCH: '$word'")
            word
        }

        val result = corrected.joinToString(" ")
        Log.d(TAG, "FINAL: '$transcribedText' → '$result'")
        return result
    }

    companion object {
        private const val TAG = "BulosSpeechCorrector"
        private const val PHRASE_THRESHOLD = 0.70f  // sentence-level matching
        private const val WORD_THRESHOLD = 0.75f    // word-level matching
        private const val MIN_WORD_LENGTH = 3
    }
}