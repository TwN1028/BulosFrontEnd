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
    private val historyRepository = HistoryRepository(application)

    var selectedUiLanguage by mutableStateOf<UiLanguage?>(value = null)
        private set
    var isLanguagePreferenceLoaded by mutableStateOf(value = false)
        private set
    var selectedAppTheme by mutableStateOf(value = AppTheme.LIGHT)
        private set
    var isThemePreferenceLoaded by mutableStateOf(value = false)
        private set
    val uiLanguage: UiLanguage get() = selectedUiLanguage ?: UiLanguage.ENGLISH
    val content: DialogueContent get() = DialogueProvider.getDialogue(uiLanguage)

    // Vosk ASR
    private var voskManager: VoskManager? = null
    var isModelLoaded by mutableStateOf(value = false)
        private set
    var isModelLoading by mutableStateOf(value = false)
        private set
    private var currentLoadedLanguage: UiLanguage? = null
    var asrStatus by mutableStateOf(value = "Ready")
        private set

    // Recording State
    var isRecording by mutableStateOf(value = false)
    var recordingTime by mutableLongStateOf(value = 0L)
    private var audioRecorder: AudioRecorder? = null
    private var audioFile: File? = null
    private var timerJob: Job? = null

    // UI Feedback States
    var isSpeechProcessing by mutableStateOf(value = false)
        private set
    var voiceInputReady by mutableStateOf(value = false)
        private set
    var speechSessionFinished by mutableStateOf(value = false)
        private set
    var speechRecognitionError by mutableStateOf<String?>(value = null)
        private set

    private var capturedText = ""
    private var currentPartialText = ""

    // Dictionary
    private val dictionaryManager = DictionaryManager(application)
    var isDictionaryLoaded by mutableStateOf(value = false)
        private set

    // Translation Repository (Hybrid)
    private val translationRepository = TranslationRepository(application, dictionaryManager)
    var isTranslating by mutableStateOf(value = false)
        private set
    
    var translationStatus by mutableStateOf(value = "")
        private set

    var isOnline by mutableStateOf(value = false)
        private set
    
    var isServerReady by mutableStateOf(value = false)
        private set

    var isSyncing by mutableStateOf(value = false)
        private set

    var lastSyncTime by mutableLongStateOf(value = 0L)
        private set

    val historyItems = mutableStateListOf<HistoryItem>()

    init {
        // Monitor network status and trigger server wake-up
        viewModelScope.launch {
            while (true) {
                val currentlyOnline = NetworkUtils.isOnline(application)
                if (currentlyOnline && !isOnline) {
                    // Just came online, trigger wake-up
                    viewModelScope.launch { translationRepository.wakeUpServer() }
                }
                isOnline = currentlyOnline
                delay(3.seconds)
            }
        }
        // Sync readiness from repository
        viewModelScope.launch {
            translationRepository.isServerReady.collect { ready ->
                isServerReady = ready
            }
        }
        viewModelScope.launch {
            languagePreferences.lastSyncTime.collect { time ->
                lastSyncTime = time
            }
        }
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
            historyRepository.history.collect { items ->
                historyItems.clear()
                historyItems.addAll(items)
            }
        }
        viewModelScope.launch {
            dictionaryManager.load()
            isDictionaryLoaded = true
            // Pre-load default model
            prepareModelForSourceLanguage()
        }
        initVosk()
    }

    private fun initVosk() {
        voskManager = VoskManager(
            getApplication(),
            onPartialResultCallback = { partial ->
                viewModelScope.launch {
                    currentPartialText = partial
                    val combined = if (capturedText.isEmpty()) partial else "$capturedText $partial"
                    TranslationState.textToTranslate = combined.trim()
                    asrStatus = "Listening..."
                }
            },
            onResultCallback = { result ->
                viewModelScope.launch {
                    if (result.isNotBlank()) {
                        capturedText = if (capturedText.isEmpty()) result else "$capturedText $result"
                        currentPartialText = ""
                        TranslationState.textToTranslate = capturedText.trim()
                        asrStatus = "Listening..."
                    }
                }
            },
            onErrorCallback = { e ->
                viewModelScope.launch {
                    e.printStackTrace()
                    asrStatus = "Error: ${e.message}"
                    speechRecognitionError = e.message
                    speechSessionFinished = true
                }
            },
        )
    }

    fun prepareModelForSourceLanguage() {
        val sourceLang = TranslationState.sourceLanguage
        if ((currentLoadedLanguage == sourceLang) && isModelLoaded) return
        
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

    fun translateText(text: String, onComplete: () -> Unit = {}) {
        TranslationState.textToTranslate = text
        isTranslating = true
        translationStatus = if (isOnline && !isServerReady) "Waking up server..." else "Translating..."
        viewModelScope.launch {
            try {
                TranslationState.translatedText = translationRepository.translate(
                    text,
                    TranslationState.sourceLanguage,
                    TranslationState.targetLanguage,
                )
                saveCurrentTranslation()
                onComplete()
            } finally {
                isTranslating = false
                translationStatus = ""
            }
        }
    }

    fun translateVoiceText(text: String, onComplete: () -> Unit = {}) {
        if (text.isBlank()) return
        TranslationState.textToTranslate = text
        capturedText = text // Sync internal buffer
        isTranslating = true
        translationStatus = if (isOnline && !isServerReady) "Waking up server..." else "Translating..."
        viewModelScope.launch {
            try {
                TranslationState.translatedText = translationRepository.translate(
                    text,
                    TranslationState.sourceLanguage,
                    TranslationState.targetLanguage,
                )
                saveCurrentTranslation()
                onComplete()
            } finally {
                isTranslating = false
                translationStatus = ""
            }
        }
    }

    fun clearVoiceDraft() {
        TranslationState.textToTranslate = ""
        TranslationState.translatedText = ""
        TranslationState.recordedAudioPath = null
        capturedText = ""
        currentPartialText = ""
        voiceInputReady = false
        speechSessionFinished = false
        speechRecognitionError = null
        isSpeechProcessing = false
    }

    fun saveCurrentTranslation() {
        val inputText = TranslationState.textToTranslate
        val translatedText = TranslationState.translatedText
        val context = getApplication<Application>()
        if (inputText.isNotEmpty() && translatedText.isNotEmpty()) {
            viewModelScope.launch {
                historyRepository.addEntry(
                    sourceLang = context.getString(TranslationState.sourceLanguage.displayNameRes),
                    targetLang = context.getString(TranslationState.targetLanguage.displayNameRes),
                    inputText = inputText,
                    translatedText = translatedText,
                )
            }
        }
    }

    fun deleteSavedTranslations(timestamps: Set<Long>) {
        viewModelScope.launch { historyRepository.delete(timestamps) }
    }

    fun clearSavedTranslations() {
        viewModelScope.launch { historyRepository.clear() }
    }

    fun syncModel(onResult: (Boolean) -> Unit = {}) {
        if (isSyncing) return
        isSyncing = true
        viewModelScope.launch {
            try {
                val success = translationRepository.syncOfflineModel()
                if (success) {
                    val now = System.currentTimeMillis()
                    languagePreferences.saveLastSyncTime(now)
                }
                onResult(success)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            } finally {
                isSyncing = false
            }
        }
    }

    fun startRecording() {
        if (!isModelLoaded) {
            prepareModelForSourceLanguage()
            asrStatus = "Waiting for model..."
            return
        }

        audioFile = File(getApplication<Application>().externalCacheDir, "recording_${System.currentTimeMillis()}.m4a")
        
        capturedText = ""
        currentPartialText = ""
        voiceInputReady = false
        speechSessionFinished = false
        speechRecognitionError = null
        isSpeechProcessing = false

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
            while (isRecording && (recordingTime < 60)) {
                delay(1.seconds)
                if (isRecording) {
                    recordingTime++
                }
            }
            if (isRecording) {
                stopRecording()
            }
        }
    }

    fun stopRecording() {
        if (!isRecording) return
        timerJob?.cancel()
        audioRecorder?.stop()
        
        isRecording = false
        isSpeechProcessing = true
        speechSessionFinished = false
        
        viewModelScope.launch {
            if (isModelLoaded) {
                val finalResult = voskManager?.stop() ?: ""
                if (finalResult.isNotBlank()) {
                    capturedText = if (capturedText.isEmpty()) finalResult else "$capturedText $finalResult"
                }
            }
            
            val finalCleanText = capturedText.trim()
            if (finalCleanText.isNotBlank()) {
                TranslationState.textToTranslate = finalCleanText
                voiceInputReady = true
            }
            
            currentPartialText = ""
            TranslationState.recordedAudioPath = audioFile?.absolutePath
            isSpeechProcessing = false
            speechSessionFinished = true
        }
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
        
        voiceInputReady = false
        speechSessionFinished = false
        speechRecognitionError = null
        isSpeechProcessing = false
    }

    override fun onCleared() {
        voskManager?.release()
        audioRecorder?.stop()
        timerJob?.cancel()
    }
}
