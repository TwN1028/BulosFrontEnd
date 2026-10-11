package com.example.bulosfrontend

import java.text.Normalizer
import java.util.Locale

/** Keeps display text readable while providing a separate canonical form for lookup only. */
object TextNormalizer {
    private val whitespace = Regex("\\s+")
    private val controlCharacters = Regex("\\p{C}+")
    private val optionalSentencePunctuation = Regex("[,.;:!?…]+")
    private val nonWordApostrophe = Regex("(?<![\\p{L}\\p{N}])'|'(?![\\p{L}\\p{N}])")

    fun cleanForDisplay(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFC)
        .replace('\u00A0', ' ')
        .replace(controlCharacters, " ")
        .replace(whitespace, " ")
        .trim()

    fun normalizeForMatching(text: String): String = cleanForDisplay(text)
        .replace(Regex("[‘’ʼ＇]"), "'")
        .replace(Regex("[“”„‟]"), "\"")
        .replace(Regex("[‐‑‒–—−]"), "-")
        .replace("\"", " ")
        .replace(nonWordApostrophe, " ")
        .replace(optionalSentencePunctuation, " ")
        .lowercase(Locale.ROOT)
        .replace(whitespace, " ")
        .trim()
}
