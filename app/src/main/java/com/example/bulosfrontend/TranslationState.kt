package com.example.bulosfrontend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class TranslationLanguagePair(
    val source: String,
    val target: String,
)

object TranslationLanguageRules {
    val supportedLanguages = listOf("English", "Filipino", "Bulos")
    val supportedPairs = setOf(
        TranslationLanguagePair("Bulos", "English"),
        TranslationLanguagePair("Bulos", "Filipino"),
        TranslationLanguagePair("English", "Bulos"),
        TranslationLanguagePair("Filipino", "Bulos"),
    )

    fun isValidPair(source: String, target: String): Boolean =
        TranslationLanguagePair(source, target) in supportedPairs

    fun targetOptions(source: String): List<String> = when (source) {
        "Bulos" -> listOf("English", "Filipino")
        "English", "Filipino" -> listOf("Bulos")
        else -> emptyList()
    }

    fun normalize(
        current: TranslationLanguagePair,
        preferredBulosTarget: String = "English",
    ): TranslationLanguagePair {
        if (isValidPair(current.source, current.target)) return current
        return when (current.source) {
            "Bulos" -> TranslationLanguagePair(
                "Bulos",
                preferredBulosTarget.takeIf { it in targetOptions("Bulos") } ?: "English",
            )
            "Filipino" -> TranslationLanguagePair("Filipino", "Bulos")
            else -> TranslationLanguagePair("English", "Bulos")
        }
    }

    fun selectSource(
        current: TranslationLanguagePair,
        selected: String,
        preferredBulosTarget: String = "English",
    ): TranslationLanguagePair {
        require(selected in supportedLanguages)
        return when (selected) {
            "Bulos" -> TranslationLanguagePair(
                "Bulos",
                current.target.takeIf { it in targetOptions("Bulos") }
                    ?: preferredBulosTarget.takeIf { it in targetOptions("Bulos") }
                    ?: "English",
            )
            else -> TranslationLanguagePair(selected, "Bulos")
        }
    }

    fun selectTarget(current: TranslationLanguagePair, selected: String): TranslationLanguagePair {
        require(selected in targetOptions(current.source))
        return TranslationLanguagePair(current.source, selected)
    }

    fun swap(current: TranslationLanguagePair): TranslationLanguagePair =
        if (isValidPair(current.source, current.target)) {
            TranslationLanguagePair(current.target, current.source)
        } else {
            normalize(current)
        }

    fun languageForUi(language: UiLanguage): String = when (language) {
        UiLanguage.ENGLISH -> "English"
        UiLanguage.FILIPINO -> "Filipino"
        UiLanguage.BULOS -> "Bulos"
    }
}

/**
 * Singleton object to hold the current translation state.
 * Decoupled from the ViewModel to allow custom API integration.
 */
object TranslationState {
    var sourceLanguage by mutableStateOf("English")
    var targetLanguage by mutableStateOf("Bulos")
    var textToTranslate by mutableStateOf("")
    var translatedText by mutableStateOf("")
    var recordedAudioPath by mutableStateOf<String?>(null)
    private var lastBulosTarget = "English"

    fun selectSourceLanguage(language: String) {
        if (sourceLanguage == "Bulos" && targetLanguage in TranslationLanguageRules.targetOptions("Bulos")) {
            lastBulosTarget = targetLanguage
        }
        applyLanguagePair(
            TranslationLanguageRules.selectSource(currentLanguagePair(), language, lastBulosTarget),
        )
    }

    fun selectTargetLanguage(language: String) {
        val pair = TranslationLanguageRules.selectTarget(currentLanguagePair(), language)
        if (pair.source == "Bulos") lastBulosTarget = pair.target
        applyLanguagePair(pair)
    }

    fun synchronizeSourceWithUiLanguage(language: UiLanguage) {
        selectSourceLanguage(TranslationLanguageRules.languageForUi(language))
    }

    fun swapLanguages() {
        if (sourceLanguage == "Bulos" && targetLanguage in TranslationLanguageRules.targetOptions("Bulos")) {
            lastBulosTarget = targetLanguage
        }
        applyLanguagePair(TranslationLanguageRules.swap(currentLanguagePair()))
    }

    fun ensureValidLanguagePair() {
        applyLanguagePair(TranslationLanguageRules.normalize(currentLanguagePair(), lastBulosTarget))
    }

    fun hasValidLanguagePair(): Boolean =
        TranslationLanguageRules.isValidPair(sourceLanguage, targetLanguage)

    private fun currentLanguagePair() = TranslationLanguagePair(sourceLanguage, targetLanguage)

    private fun applyLanguagePair(pair: TranslationLanguagePair) {
        if (pair.source == "Bulos" && pair.target in TranslationLanguageRules.targetOptions("Bulos")) {
            lastBulosTarget = pair.target
        }
        sourceLanguage = pair.source
        targetLanguage = pair.target
    }
}
