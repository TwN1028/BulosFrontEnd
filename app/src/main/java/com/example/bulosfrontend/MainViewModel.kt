package com.example.bulosfrontend

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Duration.Companion.seconds

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val languagePreferences = LanguagePreferenceRepository(application)

    var selectedUiLanguage by mutableStateOf<UiLanguage?>(null)
        private set
    var isLanguagePreferenceLoaded by mutableStateOf(false)
        private set
    var selectedAppTheme by mutableStateOf(AppTheme.LIGHT)
        private set
    var isThemePreferenceLoaded by mutableStateOf(false)
        private set
    val uiLanguage: UiLanguage get() = selectedUiLanguage ?: UiLanguage.ENGLISH
    val content: DialogueContent get() = DialogueProvider.getDialogue(uiLanguage)

    // Vosk ASR
    private var voskManager: VoskManager? = null
    var isModelLoaded by mutableStateOf(false)
        private set
    var isModelLoading by mutableStateOf(false)
        private set
    private var currentLoadedLanguage: UiLanguage? = null
    var asrStatus by mutableStateOf("Ready")
        private set

    // Recording State
    var isRecording by mutableStateOf(false)
    var recordingTime by mutableLongStateOf(0L)
    private var audioRecorder: AudioRecorder? = null
    private var audioFile: File? = null
    private var timerJob: Job? = null

    // Dictionary
    private val dictionaryManager = DictionaryManager(application)
    var isDictionaryLoaded by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            languagePreferences.selectedLanguage.collect { language ->
                selectedUiLanguage = language
                isLanguagePreferenceLoaded = true
            }
        }
        viewModelScope.launch {
            languagePreferences.selectedTheme.collect { theme ->
                selectedAppTheme = theme
                isThemePreferenceLoaded = true
            }
        }
        viewModelScope.launch {
            dictionaryManager.load()
            isDictionaryLoaded = true // Explicitly track completion
        }
        initVosk()
    }

    private fun initVosk() {
        voskManager = VoskManager(
            getApplication(),
            onPartialResultCallback = { partial ->
                viewModelScope.launch {
                    TranslationState.textToTranslate = partial
                    asrStatus = "Listening..."
                }
            },
            onResultCallback = { result ->
                viewModelScope.launch {
                    if (result.isNotBlank()) {
                        TranslationState.textToTranslate = result
                        asrStatus = "Captured"
                    }
                }
            },
            onErrorCallback = { e -> 
                viewModelScope.launch {
                    e.printStackTrace()
                    asrStatus = "Error: ${e.message}"
                }
            }
        )
    }

    fun prepareModelForSourceLanguage() {
        val sourceLang = TranslationState.sourceLanguage
        if (currentLoadedLanguage == sourceLang && isModelLoaded) return
        
        val modelPath = Design.LanguageModelMap[sourceLang] ?: return
        
        val assets = getApplication<Application>().assets
        val exists = try {
            assets.list(modelPath)?.isNotEmpty() == true
        } catch (_: Exception) {
            false
        }

        if (!exists) {
            isModelLoaded = false
            isModelLoading = false
            asrStatus = "Model not found"
            return
        }

        isModelLoaded = false
        isModelLoading = true
        asrStatus = "Loading model..."
        voskManager?.loadModel(sourceLang, modelPath) { loaded ->
            currentLoadedLanguage = if (loaded) sourceLang else null
            isModelLoaded = loaded
            isModelLoading = false
            asrStatus = if (loaded) "Ready" else "Load failed"
        }
    }

    fun selectUiLanguage(language: UiLanguage) {
        selectedUiLanguage = language
        viewModelScope.launch { languagePreferences.saveLanguage(language) }
    }

    fun selectAppTheme(theme: AppTheme) {
        selectedAppTheme = theme
        viewModelScope.launch { languagePreferences.saveTheme(theme) }
    }

    fun translateText(text: String) {
        TranslationState.textToTranslate = text
        TranslationState.translatedText = dictionaryManager.translate(
            text,
            TranslationState.sourceLanguage,
            TranslationState.targetLanguage
        )
        saveToHistory()
    }

    fun translateVoiceText(text: String) {
        if (text.isBlank()) return
        TranslationState.textToTranslate = text
        TranslationState.translatedText = dictionaryManager.translate(
            text,
            TranslationState.sourceLanguage,
            TranslationState.targetLanguage
        )
        saveToHistory()
    }

    fun clearVoiceDraft() {
        TranslationState.textToTranslate = ""
        TranslationState.translatedText = ""
        TranslationState.recordedAudioPath = null
    }

    private fun saveToHistory() {
        if (TranslationState.textToTranslate.isNotEmpty() && TranslationState.translatedText.isNotEmpty()) {
            HistoryProvider.addEntry(
                sourceLang = TranslationState.sourceLanguage.name,
                targetLang = TranslationState.targetLanguage.name,
                inputText = TranslationState.textToTranslate,
                translatedText = TranslationState.translatedText
            )
        }
    }

    fun startRecording() {
        audioFile = File(getApplication<Application>().externalCacheDir, "recording_${System.currentTimeMillis()}.m4a")
        
        if (isModelLoaded) {
            voskManager?.start()
            asrStatus = "Listening..."
        }
        
        audioRecorder = AudioRecorder(audioFile!!) { buffer, length ->
            if (isModelLoaded) {
                voskManager?.feedAudio(buffer, length)
            }
        }
        
        audioRecorder?.start()
        isRecording = true
        startTimer()
    }

    fun currentRecordingAmplitude(): Int = audioRecorder?.currentAmplitude ?: 0

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            recordingTime = 0L
            while (isRecording && recordingTime < 60) {
                delay(1.seconds)
                recordingTime++
            }
            if (isRecording) stopRecording()
        }
    }

    fun stopRecording() {
        timerJob?.cancel()
        audioRecorder?.stop()
        if (isModelLoaded) {
            val finalResult = voskManager?.stop() ?: ""
            if (finalResult.isNotEmpty()) TranslationState.textToTranslate = finalResult
        }
        
        isRecording = false
        TranslationState.recordedAudioPath = audioFile?.absolutePath
    }

    fun cancelRecording() {
        timerJob?.cancel()
        audioRecorder?.stop()
        if (isModelLoaded) {
            voskManager?.stop()
        }
        isRecording = false
        TranslationState.textToTranslate = ""
        audioFile?.delete()
        audioFile = null
    }

    override fun onCleared() {
        super.onCleared()
        voskManager?.release()
        audioRecorder?.stop()
        timerJob?.cancel()
    }
}
