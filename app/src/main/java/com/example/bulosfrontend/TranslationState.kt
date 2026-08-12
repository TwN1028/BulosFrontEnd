package com.example.bulosfrontend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Singleton object to hold the current translation state.
 * This data is kept here to be easily accessible for server communication.
 */
object TranslationState {
    var sourceLanguage by mutableStateOf("English")
    var targetLanguage by mutableStateOf("Bulos")
    var textToTranslate by mutableStateOf("")
    var recordedAudioPath by mutableStateOf<String?>(null)

    // all code to translate the needed text goes here
    var translatedText by mutableStateOf("")
    

}
