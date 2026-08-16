package com.example.bulosfrontend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

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
}
