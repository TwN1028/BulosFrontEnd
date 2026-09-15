package com.example.bulosfrontend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Singleton object to hold the current translation state.
 * Using enums for logic to prevent localization-based bugs.
 */
object TranslationState {
    var sourceLanguage by mutableStateOf(UiLanguage.ENGLISH)
    var targetLanguage by mutableStateOf(UiLanguage.BULOS)
    var textToTranslate by mutableStateOf("")
    var translatedText by mutableStateOf("")
    var recordedAudioPath by mutableStateOf<String?>(null)
}
