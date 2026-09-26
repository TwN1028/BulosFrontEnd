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

    fun isValidPair(source: String, target: String): Boolean =
        source in supportedLanguages &&
            target in supportedLanguages &&
            !source.equals(target, ignoreCase = true)

    fun selectSource(current: TranslationLanguagePair, selected: String): TranslationLanguagePair {
        require(selected in supportedLanguages)
        return if (selected.equals(current.target, ignoreCase = true)) {
            TranslationLanguagePair(selected, nextLanguageAfter(selected))
        } else {
            current.copy(source = selected)
        }
    }

    fun selectTarget(current: TranslationLanguagePair, selected: String): TranslationLanguagePair {
        require(selected in supportedLanguages)
        return if (selected.equals(current.source, ignoreCase = true)) {
            TranslationLanguagePair(nextLanguageAfter(selected), selected)
        } else {
            current.copy(target = selected)
        }
    }

    fun swap(current: TranslationLanguagePair): TranslationLanguagePair =
        if (isValidPair(current.source, current.target)) {
            TranslationLanguagePair(current.target, current.source)
        } else {
            TranslationLanguagePair(current.source, nextLanguageAfter(current.source))
        }

    private fun nextLanguageAfter(language: String): String {
        val index = supportedLanguages.indexOf(language)
        return supportedLanguages[(index + 1) % supportedLanguages.size]
    }

    fun languageForUi(language: UiLanguage): String = when (language) {
        UiLanguage.ENGLISH -> "English"
        UiLanguage.FILIPINO -> "Filipino"
        UiLanguage.BULOS -> "Bulos"
    }
}

/**
 * Singleton object to hold the current translation state.
 * Using enums for logic to prevent localization-based bugs.
 */
object TranslationState {
    var sourceLanguage by mutableStateOf(UiLanguage.ENGLISH)
    var targetLanguage by mutableStateOf(UiLanguage.BULOS)
    var textToTranslate by mutableStateOf("")
    var translatedText by mutableStateOf("")
    var translationSource by mutableStateOf("") // "Online" or "Offline"
    var recordedAudioPath by mutableStateOf<String?>(null)

    fun selectSourceLanguage(language: String) {
        applyLanguagePair(
            TranslationLanguageRules.selectSource(currentLanguagePair(), language),
        )
    }

    fun selectTargetLanguage(language: String) {
        applyLanguagePair(
            TranslationLanguageRules.selectTarget(currentLanguagePair(), language),
        )
    }

    fun synchronizeSourceWithUiLanguage(language: UiLanguage) {
        selectSourceLanguage(TranslationLanguageRules.languageForUi(language))
    }

    fun swapLanguages() {
        applyLanguagePair(TranslationLanguageRules.swap(currentLanguagePair()))
    }

    fun hasValidLanguagePair(): Boolean =
        TranslationLanguageRules.isValidPair(sourceLanguage, targetLanguage)

    private fun currentLanguagePair() = TranslationLanguagePair(sourceLanguage, targetLanguage)

    private fun applyLanguagePair(pair: TranslationLanguagePair) {
        sourceLanguage = pair.source
        targetLanguage = pair.target
    }
}
