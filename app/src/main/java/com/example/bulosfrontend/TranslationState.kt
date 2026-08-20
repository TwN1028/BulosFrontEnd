package com.example.bulosfrontend

import androidx.compose.runtime.*

object TranslationState {
    var sourceLanguage by mutableStateOf("English")
    var targetLanguage by mutableStateOf("Bulos")
    var textToTranslate by mutableStateOf("")
    var translatedText by mutableStateOf("")
    var recordedAudioPath by mutableStateOf<String?>(null)
}
