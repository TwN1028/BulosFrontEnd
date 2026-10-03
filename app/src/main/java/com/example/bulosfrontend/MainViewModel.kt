package com.example.bulosfrontend

import android.app.Application
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener as VoskRecognitionListener
import java.io.File
import kotlin.time.Duration.Companion.seconds

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val languagePreferences = LanguagePreferenceRepository(application)
    private val savedTranslationRepository = SavedTranslationRepository(application)
    private val translationRepository = TranslationServiceProvider.repository(application)
    private val voskModelManager = VoskModelManager(application)
    private val textTranslationEventsChannel = Channel<TextTranslationEvent>(Channel.BUFFERED)

    var textTranslationState by mutableStateOf<TextTranslationUiState>(TextTranslationUiState.Idle)
        private set
    var offlineTranslationSuggestions by mutableStateOf<List<OfflineTranslationSuggestion>>(emptyList())
        private set
    var offlineSuggestionInput by mutableStateOf("")
        private set
    var offlineTranslationMessage by mutableStateOf<String?>(null)
        private set
    val textTranslationEvents = textTranslationEventsChannel.receiveAsFlow()

    var selectedUiLanguage by mutableStateOf<UiLanguage?>(null)
        private set
    var isLanguagePreferenceLoaded by mutableStateOf(false)
        private set
    var selectedAppTheme by mutableStateOf(AppTheme.LIGHT)
        private set
    var isThemePreferenceLoaded by mutableStateOf(false)
        private set
    var selectedFontSize by mutableStateOf(AppFontSize.MEDIUM)
        private set
    var isFontSizePreferenceLoaded by mutableStateOf(false)
        private set
    val uiLanguage: UiLanguage get() = selectedUiLanguage ?: UiLanguage.ENGLISH
    val content: DialogueContent get() = DialogueProvider.getDialogue(uiLanguage)

    var isOnline by mutableStateOf(false)
        private set
    var isServerReady by mutableStateOf(false)
        private set
    var connectionStatus by mutableStateOf(ConnectionStatus.OFFLINE)
        private set
    var isTranslating by mutableStateOf(false)
        private set
    var translationStatus by mutableStateOf("")
        private set
    var isSyncing by mutableStateOf(false)
        private set
    var lastSyncTime by mutableLongStateOf(0L)
        private set
    var isDictionaryLoaded by mutableStateOf(false)
        private set
    var dictionaryEntries by mutableStateOf<List<DictionaryEntry>>(emptyList())
        private set

    // Recording State
    var isRecording by mutableStateOf(false)
    var recordingTime by mutableLongStateOf(0L)
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecorderStarting = false
    private var recordingTimerJob: Job? = null
    private var voskSpeechService: LowLatencyVoskSpeechService? = null
    private var voskModel: Model? = null
    private var voskModelLanguage: String? = null
    private var voskModelLoadJob: Job? = null
    private var voskPreloadJob: Job? = null
    private var backendReadinessJob: Job? = null
    private var offlineSyncAttemptedForConnection = false
    private val networkMonitor = NetworkMonitor(application) { connected ->
        viewModelScope.launch { handleConnectivityChange(connected, forceBackendCheck = false) }
    }
    private var onDeviceSpeechRecognizer: SpeechRecognizer? = null
    private var usesVoskRecognizer = false
    private var usesOnDeviceRecognizer = false
    private var speechSessionId = 0L
    private var voskCommittedText = ""
    private var speechAmplitude = 0
    var isSpeechProcessing by mutableStateOf(false)
        private set
    var voiceInputReady by mutableStateOf(false)
        private set
    var speechSessionFinished by mutableStateOf(false)
        private set
    var speechRecognitionError by mutableStateOf<String?>(null)
        private set
    var wasRecordingCancelled by mutableStateOf(false)
        private set
    val supportsLiveSpeechRecognition: Boolean
        get() = voskModelManager.supports(TranslationState.sourceLanguage)

    init {
        TranslationState.ensureValidLanguagePair()
        viewModelScope.launch {
            translationRepository.loadOfflineModel()
            isDictionaryLoaded = translationRepository.isOfflineModelLoaded
            dictionaryEntries = translationRepository.offlineDictionaryEntries
        }
        networkMonitor.start()
        viewModelScope.launch {
            languagePreferences.selectedLanguage.collect { language ->
                if (!isLanguagePreferenceLoaded && language != null) {
                    TranslationState.synchronizeSourceWithUiLanguage(language)
                }
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
            languagePreferences.selectedFontSize.collect { fontSize ->
                selectedFontSize = fontSize
                isFontSizePreferenceLoaded = true
            }
        }
        viewModelScope.launch {
            savedTranslationRepository.entries.collect { entries ->
                HistoryProvider.history.clear()
                HistoryProvider.history.addAll(entries)
            }
        }
    }

    fun refreshConnectionStatus() {
        viewModelScope.launch {
            handleConnectivityChange(
                connected = NetworkUtils.isOnline(getApplication()),
                forceBackendCheck = true,
            )
        }
    }

    private fun handleConnectivityChange(connected: Boolean, forceBackendCheck: Boolean) {
        val connectivityChanged = connected != isOnline
        if (!connected) {
            isOnline = false
            isServerReady = false
            connectionStatus = ConnectionStatus.OFFLINE
            offlineSyncAttemptedForConnection = false
            backendReadinessJob?.cancel()
            return
        }

        isOnline = true
        if (!offlineSyncAttemptedForConnection) {
            offlineSyncAttemptedForConnection = true
            syncModel()
        }
        if (connectivityChanged || forceBackendCheck) checkBackendReadiness()
    }

    private fun checkBackendReadiness() {
        if (backendReadinessJob?.isActive == true) return
        connectionStatus = ConnectionStatus.WAKING_UP
        backendReadinessJob = viewModelScope.launch {
            translationRepository.wakeUpServer()
            val stillConnected = NetworkUtils.isOnline(getApplication())
            isOnline = stillConnected
            isServerReady = stillConnected && translationRepository.isServerReady
            connectionStatus = resolveConnectionStatus(
                connected = stillConnected,
                backendCheckInProgress = false,
                backendReady = isServerReady,
            )
        }
    }

    fun selectUiLanguage(language: UiLanguage) {
        selectedUiLanguage = language
        viewModelScope.launch { languagePreferences.saveLanguage(language) }
    }

    fun selectHomeUiLanguage(language: UiLanguage) {
        selectUiLanguage(language)
        TranslationState.synchronizeSourceWithUiLanguage(language)
    }

    fun selectAppTheme(theme: AppTheme) {
        selectedAppTheme = theme
        viewModelScope.launch { languagePreferences.saveTheme(theme) }
    }

    fun selectFontSize(fontSize: AppFontSize) {
        selectedFontSize = fontSize
        viewModelScope.launch { languagePreferences.saveFontSize(fontSize) }
    }

    fun syncModel(onResult: (Boolean) -> Unit = {}) {
        if (isSyncing) return
        isSyncing = true
        viewModelScope.launch {
            val success = runCatching { translationRepository.syncOfflineModel() }
                .getOrDefault(false)
            if (success) {
                lastSyncTime = System.currentTimeMillis()
                isDictionaryLoaded = true
                dictionaryEntries = translationRepository.offlineDictionaryEntries
            }
            isSyncing = false
            onResult(success)
        }
    }

    fun translateText(text: String) {
        val displayText = TextNormalizer.cleanForDisplay(text)
        if (textTranslationState is TextTranslationUiState.Loading || displayText.isBlank()) return
        val sourceLanguage = TranslationState.sourceLanguage
        val targetLanguage = TranslationState.targetLanguage
        if (!TranslationLanguageRules.isValidPair(sourceLanguage, targetLanguage)) {
            val message = "This language pair is not supported."
            textTranslationState = TextTranslationUiState.Error(message)
            viewModelScope.launch {
                textTranslationEventsChannel.send(TextTranslationEvent.ShowError(message))
            }
            return
        }
        TranslationState.textToTranslate = text
        TranslationState.translatedText = ""
        offlineTranslationSuggestions = emptyList()
        offlineSuggestionInput = ""
        offlineTranslationMessage = null
        textTranslationState = TextTranslationUiState.Loading
        isTranslating = true
        translationStatus = if (isOnline && !isServerReady) "Waking up server..." else "Translating..."
        viewModelScope.launch {
            try {
                when (
                    val result = translationRepository.translate(
                        sourceLanguage = sourceLanguage,
                        targetLanguage = targetLanguage,
                        text = displayText,
                    )
                ) {
                    is TranslationResult.Success -> {
                        offlineTranslationMessage = null
                        val displayResponse = result.response.copy(originalText = text)
                        TranslationState.textToTranslate = text
                        TranslationState.translatedText = displayResponse.translatedText
                        textTranslationState = TextTranslationUiState.Success(displayResponse)
                        textTranslationEventsChannel.send(TextTranslationEvent.NavigateToResult)
                    }
                    is TranslationResult.Suggestions -> {
                        offlineTranslationMessage = null
                        offlineTranslationSuggestions = result.candidates
                        offlineSuggestionInput = text
                        textTranslationState = TextTranslationUiState.Suggestions(result.candidates)
                    }
                    is TranslationResult.Failure -> {
                        offlineTranslationMessage = result.message
                        textTranslationState = TextTranslationUiState.Error(result.message)
                        textTranslationEventsChannel.send(TextTranslationEvent.ShowError(result.message))
                    }
                }
            } finally {
                isTranslating = false
                translationStatus = ""
            }
        }
    }

    fun resetTextTranslationResult() {
        textTranslationState = TextTranslationUiState.Idle
        TranslationState.translatedText = ""
        offlineTranslationSuggestions = emptyList()
        offlineSuggestionInput = ""
        offlineTranslationMessage = null
    }

    fun translateVoiceText(text: String) {
        val displayText = TextNormalizer.cleanForDisplay(text)
        if (!TranslationState.hasValidLanguagePair() || displayText.isBlank()) return
        TranslationState.textToTranslate = displayText
        TranslationState.translatedText = ""
        offlineTranslationSuggestions = emptyList()
        offlineSuggestionInput = ""
        offlineTranslationMessage = null
        isTranslating = true
        translationStatus = if (isOnline && !isServerReady) "Waking up server..." else "Translating..."
        val sourceLanguage = TranslationState.sourceLanguage
        val targetLanguage = TranslationState.targetLanguage
        viewModelScope.launch {
            try {
                when (
                    val result = translationRepository.translate(
                        sourceLanguage = sourceLanguage,
                        targetLanguage = targetLanguage,
                        text = displayText,
                    )
                ) {
                    is TranslationResult.Success -> {
                        offlineTranslationMessage = null
                        TranslationState.textToTranslate = displayText
                        TranslationState.translatedText = result.response.translatedText
                    }
                    is TranslationResult.Suggestions -> {
                        offlineTranslationMessage = null
                        offlineTranslationSuggestions = result.candidates
                        offlineSuggestionInput = displayText
                    }
                    is TranslationResult.Failure -> {
                        offlineTranslationMessage = result.message
                        textTranslationEventsChannel.send(TextTranslationEvent.ShowError(result.message))
                    }
                }
            } finally {
                isTranslating = false
                translationStatus = ""
            }
        }
    }

    fun acceptOfflineSuggestion(suggestion: OfflineTranslationSuggestion) {
        if (!TranslationState.hasValidLanguagePair()) return
        val input = offlineSuggestionInput.takeIf(String::isNotBlank) ?: return
        val response = TranslationResponse(
            originalText = input,
            translatedText = suggestion.translatedText,
            sourceLanguage = BackendLanguageCodes.forUiLabel(TranslationState.sourceLanguage),
            targetLanguage = BackendLanguageCodes.forUiLabel(TranslationState.targetLanguage),
            confidence = suggestion.similarity,
            intermediateLanguage = null,
            translationMethod = "offline_user_selected_suggestion",
        )
        TranslationState.textToTranslate = input
        TranslationState.translatedText = suggestion.translatedText
        offlineTranslationSuggestions = emptyList()
        offlineSuggestionInput = ""
        offlineTranslationMessage = null
        textTranslationState = TextTranslationUiState.Success(response)
    }

    fun clearVoiceDraft() {
        TranslationState.textToTranslate = ""
        TranslationState.translatedText = ""
        TranslationState.recordedAudioPath = null
        voiceInputReady = false
        speechSessionFinished = false
        speechRecognitionError = null
        isSpeechProcessing = false
        offlineTranslationSuggestions = emptyList()
        offlineSuggestionInput = ""
        offlineTranslationMessage = null
    }

    fun saveCurrentTranslation() {
        val inputText = TranslationState.textToTranslate
        val translatedText = TranslationState.translatedText
        val alreadySaved = HistoryProvider.history.any {
            it.sourceLang == TranslationState.sourceLanguage &&
                it.targetLang == TranslationState.targetLanguage &&
                it.inputText == inputText &&
                it.translatedText == translatedText
        }
        if (inputText.isNotEmpty() && translatedText.isNotEmpty() && !alreadySaved) {
            viewModelScope.launch {
                savedTranslationRepository.save(
                    sourceLang = TranslationState.sourceLanguage,
                    targetLang = TranslationState.targetLanguage,
                    inputText = inputText,
                    translatedText = translatedText,
                )
            }
        }
    }

    fun toggleCurrentTranslationSaved() {
        val inputText = TranslationState.textToTranslate
        val translatedText = TranslationState.translatedText
        if (inputText.isEmpty() || translatedText.isEmpty()) return
        val saved = HistoryProvider.history.firstOrNull {
            it.matchesTranslation(
                sourceLang = TranslationState.sourceLanguage,
                targetLang = TranslationState.targetLanguage,
                inputText = inputText,
                translatedText = translatedText,
            )
        }
        if (saved != null) {
            deleteSavedTranslations(setOf(saved.timestamp))
        } else {
            saveCurrentTranslation()
        }
    }

    fun deleteSavedTranslations(timestamps: Set<Long>) {
        viewModelScope.launch { savedTranslationRepository.delete(timestamps) }
    }

    fun clearSavedTranslations() {
        viewModelScope.launch { savedTranslationRepository.clear() }
    }

    fun startRecording() {
        if (
            isRecording || recorder != null || voskSpeechService != null ||
            onDeviceSpeechRecognizer != null || isRecorderStarting
        ) return
        wasRecordingCancelled = false
        voiceInputReady = false
        speechSessionFinished = false
        speechRecognitionError = null
        isSpeechProcessing = false
        TranslationState.recordedAudioPath = null
        val language = TranslationState.sourceLanguage
        usesVoskRecognizer = voskModelManager.supports(language)
        if (usesVoskRecognizer && canUseOnDeviceRecognizer()) {
            startAndroidSpeechRecognition(language, preferOffline = true)
        } else if (usesVoskRecognizer && isOnline && canUseSystemRecognizer()) {
            startAndroidSpeechRecognition(language, preferOffline = false)
        } else if (usesVoskRecognizer) {
            startVoskSpeechRecognition(language)
        } else {
            startAudioFileRecording()
        }
    }

    fun preloadOfflineSpeechModel(language: String) {
        if (!voskModelManager.supports(language)) return
        if (voskModelLanguage == language && voskModel != null) return
        val previousPreload = voskPreloadJob
        voskPreloadJob = viewModelScope.launch {
            previousPreload?.join()
            runCatching {
                val loadedModel = withContext(Dispatchers.IO) { voskModelManager.load(language) }
                if (voskModelLanguage != language || voskModel == null) {
                    voskModel?.close()
                    voskModel = loadedModel
                    voskModelLanguage = language
                } else {
                    loadedModel.close()
                }
            }
        }
    }

    private fun canUseSystemRecognizer(): Boolean =
        SpeechRecognizer.isRecognitionAvailable(getApplication())

    private fun canUseOnDeviceRecognizer(): Boolean {
        val context = getApplication<Application>()
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
    }

    private fun startAndroidSpeechRecognition(language: String, preferOffline: Boolean) {
        val context = getApplication<Application>()
        val sessionId = ++speechSessionId
        isRecorderStarting = true
        isSpeechProcessing = true
        usesOnDeviceRecognizer = true
        TranslationState.textToTranslate = ""
        val recognizer = runCatching {
            if (preferOffline) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }
        }.getOrElse {
            usesOnDeviceRecognizer = false
            isRecorderStarting = false
            startFallbackSpeechRecognition(language, preferOffline)
            return
        }
        onDeviceSpeechRecognizer = recognizer
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                if (sessionId != speechSessionId) return
                isRecorderStarting = false
                isSpeechProcessing = false
                isRecording = true
                startTimer()
            }

            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) {
                speechAmplitude = (((rmsdB + 2f) / 12f).coerceIn(0f, 1f) * 32767f).toInt()
            }
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() {
                if (sessionId != speechSessionId) return
                isRecording = false
                isSpeechProcessing = true
                recordingTimerJob?.cancel()
                recordingTimerJob = null
            }

            override fun onError(error: Int) {
                if (sessionId != speechSessionId || !usesOnDeviceRecognizer) return
                if (shouldFallbackFromOnDevice(error)) {
                    finishOnDeviceRecognition(markFinished = false)
                    startFallbackSpeechRecognition(language, preferOffline)
                } else {
                    speechRecognitionError = onDeviceRecognitionError(error)
                    finishOnDeviceRecognition(markFinished = true)
                }
            }

            override fun onResults(results: Bundle?) {
                if (sessionId != speechSessionId || !usesOnDeviceRecognizer) return
                updateFromAndroidSpeechResults(results)
                finishOnDeviceRecognition(markFinished = true)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                if (sessionId == speechSessionId) updateFromAndroidSpeechResults(partialResults)
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        recognizer.startListening(
            androidRecognizerIntent(context.packageName, language, preferOffline),
        )
    }

    private fun startFallbackSpeechRecognition(language: String, attemptedOffline: Boolean) {
        if (attemptedOffline && isOnline && canUseSystemRecognizer()) {
            startAndroidSpeechRecognition(language, preferOffline = false)
        } else {
            startVoskSpeechRecognition(language)
        }
    }

    private fun androidRecognizerIntent(
        packageName: String,
        language: String,
        preferOffline: Boolean,
    ) =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            val tag = if (language.equals("Filipino", ignoreCase = true)) "fil-PH" else "en-PH"
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, tag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, tag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, preferOffline)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }

    private fun updateFromAndroidSpeechResults(results: Bundle?) {
        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?.let(TextNormalizer::cleanForDisplay)
            ?.takeIf(String::isNotEmpty)
            ?.let { TranslationState.textToTranslate = it }
    }

    private fun shouldFallbackFromOnDevice(error: Int): Boolean =
        error == SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ||
            error == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE ||
            error == SpeechRecognizer.ERROR_SERVER ||
            error == SpeechRecognizer.ERROR_SERVER_DISCONNECTED ||
            error == SpeechRecognizer.ERROR_CLIENT

    private fun onDeviceRecognitionError(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
        SpeechRecognizer.ERROR_NO_MATCH -> "No speech was recognized. Please try again."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech was detected. Please try again."
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please try again."
        else -> "On-device speech recognition could not be completed. Please try again."
    }

    private fun finishOnDeviceRecognition(markFinished: Boolean) {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        usesOnDeviceRecognizer = false
        onDeviceSpeechRecognizer?.destroy()
        onDeviceSpeechRecognizer = null
        speechAmplitude = 0
        isRecorderStarting = false
        isRecording = false
        isSpeechProcessing = false
        if (markFinished) {
            voiceInputReady = TranslationState.textToTranslate.isNotBlank()
            speechSessionFinished = true
        }
    }

    private fun startVoskSpeechRecognition(language: String) {
        isRecorderStarting = true
        isSpeechProcessing = true
        val sessionId = ++speechSessionId
        voskCommittedText = ""
        TranslationState.textToTranslate = ""
        voskModelLoadJob = viewModelScope.launch {
            try {
                voskPreloadJob?.join()
                val model = if (voskModel != null && voskModelLanguage == language) {
                    requireNotNull(voskModel)
                } else {
                    val loadedModel = withContext(Dispatchers.IO) { voskModelManager.load(language) }
                    if (sessionId != speechSessionId) {
                        loadedModel.close()
                        return@launch
                    }
                    voskModel?.close()
                    voskModel = loadedModel
                    voskModelLanguage = language
                    loadedModel
                }
                val recognizer = withContext(Dispatchers.IO) { Recognizer(model, VOSK_SAMPLE_RATE) }
                if (sessionId != speechSessionId) {
                    recognizer.close()
                    return@launch
                }
                val service = LowLatencyVoskSpeechService(recognizer, VOSK_SAMPLE_RATE)
                voskSpeechService = service
                service.startListening(voskListener(sessionId))
                isRecorderStarting = false
                isSpeechProcessing = false
                isRecording = true
                startTimer()
            } catch (error: Exception) {
                if (sessionId != speechSessionId) return@launch
                isRecorderStarting = false
                isSpeechProcessing = false
                isRecording = false
                usesVoskRecognizer = false
                speechSessionFinished = true
                speechRecognitionError = offlineModelError(language, error)
            }
        }
    }

    private fun voskListener(sessionId: Long) = object : VoskRecognitionListener {
        override fun onPartialResult(hypothesis: String?) {
            val partial = parseVoskText(hypothesis, "partial")
            viewModelScope.launch {
                if (sessionId == speechSessionId && isRecording) {
                    TranslationState.textToTranslate = joinSpeechText(voskCommittedText, partial)
                }
            }
        }

        override fun onResult(hypothesis: String?) {
            val result = parseVoskText(hypothesis, "text")
            viewModelScope.launch {
                if (sessionId == speechSessionId && result.isNotEmpty()) {
                    voskCommittedText = joinSpeechText(voskCommittedText, result)
                    TranslationState.textToTranslate = voskCommittedText
                }
            }
        }

        override fun onFinalResult(hypothesis: String?) {
            val result = parseVoskText(hypothesis, "text")
            viewModelScope.launch {
                if (sessionId != speechSessionId) return@launch
                if (result.isNotEmpty()) {
                    voskCommittedText = joinSpeechText(voskCommittedText, result)
                    TranslationState.textToTranslate = voskCommittedText
                }
                finishVoskRecognition()
            }
        }

        override fun onError(error: Exception?) {
            viewModelScope.launch {
                if (sessionId != speechSessionId) return@launch
                speechRecognitionError = error?.message
                    ?.takeIf(String::isNotBlank)
                    ?: "Offline speech recognition could not be completed. Please try again."
                finishVoskRecognition()
            }
        }

        override fun onTimeout() {
            viewModelScope.launch {
                if (sessionId == speechSessionId) finishVoskRecognition()
            }
        }
    }

    private fun parseVoskText(payload: String?, key: String): String = runCatching {
        TextNormalizer.cleanForDisplay(
            JsonParser.parseString(payload.orEmpty()).asJsonObject.get(key)?.asString.orEmpty(),
        )
    }.getOrDefault("")

    private fun joinSpeechText(committed: String, next: String): String =
        TextNormalizer.cleanForDisplay(
            listOf(committed, next).filter(String::isNotBlank).joinToString(" "),
        )

    private fun finishVoskRecognition() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        voskSpeechService?.shutdown()
        voskSpeechService = null
        speechAmplitude = 0
        isRecorderStarting = false
        isRecording = false
        isSpeechProcessing = false
        voiceInputReady = TranslationState.textToTranslate.isNotBlank()
        speechSessionFinished = true
    }

    private fun offlineModelError(language: String, error: Exception): String {
        val size = if (language.equals("Filipino", ignoreCase = true)) "314 MB" else "39 MB"
        return if (!NetworkUtils.isOnline(getApplication())) {
            "$language offline speech model is not installed. Connect once to download the $size model."
        } else {
            error.message?.takeIf(String::isNotBlank)
                ?: "Could not prepare the $language offline speech model. Please try again."
        }
    }

    private fun startAudioFileRecording() {
        isRecorderStarting = true
        val context = getApplication<Application>()
        audioFile = File(context.externalCacheDir, "recording_${System.currentTimeMillis()}.mp3")
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            createLegacyMediaRecorder()
        }
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile?.absolutePath)
            try {
                prepare()
                start()
                isRecording = true
                startTimer()
            } catch (e: Exception) {
                e.printStackTrace()
                release()
                recorder = null
                audioFile?.delete()
                audioFile = null
            } finally {
                isRecorderStarting = false
            }
        }
    }

    fun currentRecordingAmplitude(): Int {
        if (!isRecording) return 0
        if (usesOnDeviceRecognizer) return speechAmplitude
        if (usesVoskRecognizer) return speechAmplitude
        return try {
            recorder?.maxAmplitude ?: 0
        } catch (_: IllegalStateException) {
            0
        } catch (_: RuntimeException) {
            0
        }
    }

    @Suppress("DEPRECATION")
    private fun createLegacyMediaRecorder(): MediaRecorder = MediaRecorder()

    private fun startTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            recordingTime = 0L
            while (isRecording && recordingTime < 60) {
                delay(1.seconds)
                if (!isRecording) break
                recordingTime++
            }
            if (isRecording) {
                stopRecording()
            }
        }
    }

    fun stopRecording() {
        if (usesOnDeviceRecognizer && onDeviceSpeechRecognizer != null) {
            recordingTimerJob?.cancel()
            recordingTimerJob = null
            isRecording = false
            isSpeechProcessing = true
            onDeviceSpeechRecognizer?.stopListening()
            return
        }
        val activeVoskService = voskSpeechService
        if (usesVoskRecognizer && activeVoskService != null) {
            recordingTimerJob?.cancel()
            recordingTimerJob = null
            isRecording = false
            isSpeechProcessing = true
            activeVoskService.stop()
            val stoppingSessionId = speechSessionId
            viewModelScope.launch {
                delay(750)
                if (stoppingSessionId == speechSessionId && voskSpeechService === activeVoskService) {
                    finishVoskRecognition()
                }
            }
            return
        }
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder = null
            isRecorderStarting = false
            isRecording = false
            TranslationState.recordedAudioPath = audioFile?.absolutePath
            voiceInputReady = TranslationState.recordedAudioPath != null
            speechSessionFinished = true
        }
    }

    fun cancelRecording() {
        speechSessionId++
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        voskModelLoadJob?.cancel()
        voskModelLoadJob = null
        onDeviceSpeechRecognizer?.cancel()
        onDeviceSpeechRecognizer?.destroy()
        onDeviceSpeechRecognizer = null
        voskSpeechService?.stop()
        voskSpeechService?.shutdown()
        voskSpeechService = null
        speechAmplitude = 0
        val activeRecorder = recorder
        recorder = null
        try {
            activeRecorder?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                activeRecorder?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isRecorderStarting = false
            isRecording = false
            recordingTime = 0L
            audioFile?.delete()
            audioFile = null
            TranslationState.textToTranslate = ""
            TranslationState.translatedText = ""
            TranslationState.recordedAudioPath = null
            voiceInputReady = false
            speechSessionFinished = false
            speechRecognitionError = null
            isSpeechProcessing = false
            usesVoskRecognizer = false
            usesOnDeviceRecognizer = false
            wasRecordingCancelled = true
        }
    }

    override fun onCleared() {
        networkMonitor.stop()
        backendReadinessJob?.cancel()
        speechSessionId++
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        voskModelLoadJob?.cancel()
        voskModelLoadJob = null
        voskPreloadJob?.cancel()
        voskPreloadJob = null
        onDeviceSpeechRecognizer?.cancel()
        onDeviceSpeechRecognizer?.destroy()
        onDeviceSpeechRecognizer = null
        voskSpeechService?.stop()
        voskSpeechService?.shutdown()
        voskSpeechService = null
        voskModel?.close()
        voskModel = null
        recorder?.release()
        recorder = null
        super.onCleared()
    }

    private companion object {
        const val VOSK_SAMPLE_RATE = 16_000f
    }
}

sealed interface TextTranslationUiState {
    data object Idle : TextTranslationUiState
    data object Loading : TextTranslationUiState
    data class Success(val response: TranslationResponse) : TextTranslationUiState
    data class Suggestions(val candidates: List<OfflineTranslationSuggestion>) : TextTranslationUiState
    data class Error(val message: String) : TextTranslationUiState
}

sealed interface TextTranslationEvent {
    data object NavigateToResult : TextTranslationEvent
    data class ShowError(val message: String) : TextTranslationEvent
}
