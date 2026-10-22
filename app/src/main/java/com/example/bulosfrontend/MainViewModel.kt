package com.example.bulosfrontend

import android.app.Application
import android.content.Intent
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener as VoskRecognitionListener
import java.io.File
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

enum class OfflineSpeechResourceState {
    NOT_DOWNLOADED,
    DOWNLOADING,
    READY,
    FAILED,
}

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
    var directBulosUnavailableNoticeId by mutableIntStateOf(0)
        private set
    val textTranslationEvents = textTranslationEventsChannel.receiveAsFlow()

    var selectedUiLanguage by mutableStateOf<UiLanguage?>(null)
        private set
    var isLanguagePreferenceLoaded by mutableStateOf(false)
        private set
    var selectedFontSize by mutableStateOf(AppFontSize.MEDIUM)
        private set
    var isFontSizePreferenceLoaded by mutableStateOf(false)
        private set
    var offlineModeEnabled by mutableStateOf(false)
        private set
    var isOfflineModePreferenceLoaded by mutableStateOf(false)
        private set
    var hasCompletedPreservationIntro by mutableStateOf<Boolean?>(null)
        private set
    var hasCompletedOnboarding by mutableStateOf<Boolean?>(null)
        private set
    var hasCompletedSttWalkthrough by mutableStateOf<Boolean?>(null)
        private set
    var isHistoryLoaded by mutableStateOf(false)
        private set
    var isOfflineDictionaryLoadComplete by mutableStateOf(false)
        private set
    var startupError by mutableStateOf<String?>(null)
        private set
    val startupCompletedTaskCount: Int
        get() = listOf(
            isLanguagePreferenceLoaded,
            isFontSizePreferenceLoaded,
            isOfflineModePreferenceLoaded,
            hasCompletedOnboarding != null,
            hasCompletedSttWalkthrough != null,
            isHistoryLoaded,
            isOfflineDictionaryLoadComplete,
        ).count { it }
    val startupTaskCount: Int get() = 7
    val isStartupReady: Boolean get() = startupCompletedTaskCount == startupTaskCount && startupError == null
    val uiLanguage: UiLanguage get() = selectedUiLanguage ?: UiLanguage.ENGLISH
    val content: DialogueContent get() = DialogueProvider.getDialogue(uiLanguage)

    var isOnline by mutableStateOf(false)
        private set
    var isServerReady by mutableStateOf(false)
        private set
    var connectionStatus by mutableStateOf(ConnectionStatus.OFFLINE)
        private set
    val canUseOnlineServices: Boolean get() = isOnline && !offlineModeEnabled
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
    private var bulosCapture: IsolatedPcmWavCapture? = null
    private var bulosWavFile: File? = null
    private var bulosTranscriptionJob: Job? = null
    private var isRecorderStarting = false
    private var recordingTimerJob: Job? = null
    private var cancelledGlowResetJob: Job? = null
    private var voskSpeechService: LowLatencyVoskSpeechService? = null
    private var voskModel: Model? = null
    private var voskModelLanguage: String? = null
    private var voskModelLoadJob: Job? = null
    private var voskPreloadJob: Job? = null
    var offlineSpeechResourceStates by mutableStateOf<Map<UiLanguage, OfflineSpeechResourceState>>(emptyMap())
        private set
    private var backendReadinessJob: Job? = null
    private var connectivityChangeJob: Job? = null
    private var savedTranslationsObservationJob: Job? = null
    private var dictionarySyncJob: Job? = null
    private var textTranslationJob: Job? = null
    private var voiceTranslationJob: Job? = null
    private var offlineSyncAttemptedForConnection = false
    private val networkMonitor = NetworkMonitor(application) { connected ->
        connectivityChangeJob?.cancel()
        connectivityChangeJob = viewModelScope.launch {
            handleConnectivityChange(
                connected = connected && NetworkUtils.isOnline(getApplication()),
                forceBackendCheck = false,
            )
        }
    }
    private var onDeviceSpeechRecognizer: SpeechRecognizer? = null
    private var usesVoskRecognizer = false
    private var usesOnDeviceRecognizer = false
    private var usesBulosOnlineCapture = false
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
    var isNoSpeechDetected by mutableStateOf(false)
        private set
    var wasRecordingCancelled by mutableStateOf(false)
        private set
    val supportsLiveSpeechRecognition: Boolean
        get() = voskModelManager.supports(TranslationState.sourceLanguage)

    private val bulosSttApi: BackendSttApi by lazy {
        BackendSttClientFactory.create(
            TranslationServiceProvider.BASE_URL,
            TranslationServiceProvider.createBackendClient(getApplication<Application>()),
        )
    }

    init {
        TranslationState.ensureValidLanguagePair()
        refreshOfflineSpeechModelStatus()
        loadRequiredStartupData()
        BulosGrammarRuleLoader.loadRules()
    }

    fun retryStartup() {
        startupError = null
        loadRequiredStartupData()
    }

    private fun loadRequiredStartupData() {
        fun load(task: suspend () -> Unit) {
            viewModelScope.launch {
                runCatching { task() }.onFailure {
                    startupError = "Unable to prepare the app. Check local storage and try again."
                }
            }
        }
        if (!isOfflineDictionaryLoadComplete) load {
            translationRepository.loadOfflineModel()
            isDictionaryLoaded = translationRepository.isOfflineModelLoaded
            dictionaryEntries = translationRepository.offlineDictionaryEntries
            isOfflineDictionaryLoadComplete = true
        }
        if (!isLanguagePreferenceLoaded) load {
            val language = languagePreferences.selectedLanguage.first()
            if (language != null) TranslationState.synchronizeSourceWithUiLanguage(language)
            selectedUiLanguage = language
            isLanguagePreferenceLoaded = true
        }
        if (!isFontSizePreferenceLoaded) load {
            selectedFontSize = languagePreferences.selectedFontSize.first()
            isFontSizePreferenceLoaded = true
        }
        if (!isOfflineModePreferenceLoaded) load {
            offlineModeEnabled = languagePreferences.offlineModeEnabled.first()
            translationRepository.setOfflineModeEnabled(offlineModeEnabled)
            isOfflineModePreferenceLoaded = true
            networkMonitor.start()
        }
        if (hasCompletedPreservationIntro == null) load {
            hasCompletedPreservationIntro = languagePreferences.hasCompletedPreservationIntro.first() ?: false
        }
        if (hasCompletedOnboarding == null) load {
            hasCompletedOnboarding = languagePreferences.hasCompletedOnboarding.first()
        }
        if (hasCompletedSttWalkthrough == null) load {
            hasCompletedSttWalkthrough = languagePreferences.hasCompletedSttWalkthrough.first()
        }
        if (!isHistoryLoaded && savedTranslationsObservationJob?.isActive != true) {
            savedTranslationsObservationJob = viewModelScope.launch {
                savedTranslationRepository.pruneExpiredUnsavedEntries()
                savedTranslationRepository.entries
                    .catch {
                        startupError = "Unable to prepare the app. Check local storage and try again."
                    }
                    .collect { entries ->
                        HistoryProvider.history.clear()
                        HistoryProvider.history.addAll(entries)
                        isHistoryLoaded = true
                    }
            }
        }
    }

    fun refreshConnectionStatus() {
        if (!isOfflineModePreferenceLoaded) return
        viewModelScope.launch {
            handleConnectivityChange(
                connected = NetworkUtils.isOnline(getApplication()),
                forceBackendCheck = true,
            )
        }
    }

    private fun handleConnectivityChange(connected: Boolean, forceBackendCheck: Boolean) {
        val connectivityChanged = connected != isOnline
        isOnline = connected
        if (offlineModeEnabled) {
            isServerReady = false
            connectionStatus = ConnectionStatus.OFFLINE_MODE
            offlineSyncAttemptedForConnection = false
            backendReadinessJob?.cancel()
            return
        }
        if (!connected) {
            isServerReady = false
            connectionStatus = ConnectionStatus.OFFLINE
            offlineSyncAttemptedForConnection = false
            backendReadinessJob?.cancel()
            return
        }

        if (!offlineSyncAttemptedForConnection) {
            offlineSyncAttemptedForConnection = true
            syncModel()
        }
        if (connectivityChanged || forceBackendCheck) {
            checkBackendReadiness(force = forceBackendCheck)
        }
    }

    private fun checkBackendReadiness(force: Boolean) {
        if (backendReadinessJob?.isActive == true) {
            if (!force) return
            backendReadinessJob?.cancel()
        }
        connectionStatus = ConnectionStatus.WAKING_UP
        backendReadinessJob = viewModelScope.launch {
            updateBackendReadiness()
            while (canUseOnlineServices && !isServerReady) {
                delay(BACKEND_RECOVERY_INTERVAL_MS)
                if (!NetworkUtils.isOnline(getApplication())) {
                    handleConnectivityChange(connected = false, forceBackendCheck = false)
                    return@launch
                }
                // Keep showing "Service unavailable" during background recovery probes.
                updateBackendReadiness()
            }
        }
    }

    private suspend fun updateBackendReadiness() {
        translationRepository.wakeUpServer()
        val stillConnected = NetworkUtils.isOnline(getApplication())
        isOnline = stillConnected
        isServerReady = canUseOnlineServices && translationRepository.isServerReady
        connectionStatus = resolveConnectionStatus(
            connected = stillConnected,
            backendCheckInProgress = false,
            backendReady = isServerReady,
            offlineModeEnabled = offlineModeEnabled,
        )
    }

    fun selectUiLanguage(language: UiLanguage) {
        selectedUiLanguage = language
        viewModelScope.launch { languagePreferences.saveLanguage(language) }
    }

    fun selectHomeUiLanguage(language: UiLanguage) {
        selectUiLanguage(language)
        TranslationState.synchronizeSourceWithUiLanguage(language)
    }

    fun markSttWalkthroughCompleted() {
        hasCompletedSttWalkthrough = true
        viewModelScope.launch { languagePreferences.markSttWalkthroughCompleted() }
    }

    fun markPreservationIntroCompleted() {
        hasCompletedPreservationIntro = true
        viewModelScope.launch { languagePreferences.markPreservationIntroCompleted() }
    }

    fun markOnboardingCompleted() {
        // Keep the in-memory value synchronized with persistence so reopening the User Guide
        // within the same app session is correctly treated as a replay.
        hasCompletedOnboarding = true
        viewModelScope.launch { languagePreferences.markOnboardingCompleted() }
    }

    fun selectFontSize(fontSize: AppFontSize) {
        selectedFontSize = fontSize
        viewModelScope.launch { languagePreferences.saveFontSize(fontSize) }
    }

    fun selectOfflineMode(enabled: Boolean) {
        if (offlineModeEnabled == enabled) return
        offlineModeEnabled = enabled
        translationRepository.setOfflineModeEnabled(enabled)
        viewModelScope.launch { languagePreferences.saveOfflineMode(enabled) }
        if (enabled) {
            dictionarySyncJob?.cancel()
            textTranslationJob?.cancel()
            voiceTranslationJob?.cancel()
            voskPreloadJob?.cancel()
            isSyncing = false
            if (textTranslationState is TextTranslationUiState.Loading) {
                textTranslationState = TextTranslationUiState.Idle
            }
            isTranslating = false
            translationStatus = ""
            backendReadinessJob?.cancel()
            isServerReady = false
            connectionStatus = ConnectionStatus.OFFLINE_MODE
        } else {
            offlineSyncAttemptedForConnection = false
            handleConnectivityChange(
                connected = NetworkUtils.isOnline(getApplication()),
                forceBackendCheck = true,
            )
            preloadOfflineSpeechModel(TranslationState.sourceLanguage)
        }
    }

    fun syncModel(onResult: (Boolean) -> Unit = {}) {
        if (isSyncing || offlineModeEnabled) {
            onResult(false)
            return
        }
        isSyncing = true
        dictionarySyncJob = viewModelScope.launch {
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
            offlineTranslationMessage = message
            textTranslationState = TextTranslationUiState.Error(message)
            viewModelScope.launch {
                textTranslationEventsChannel.send(
                    TextTranslationEvent.ShowError(translationFailurePresentation(message).title),
                )
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
        translationStatus = if (canUseOnlineServices && !isServerReady) "Waking up server..." else "Translating..."
        textTranslationJob = viewModelScope.launch {
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
                        savedTranslationRepository.record(
                            sourceLang = sourceLanguage,
                            targetLang = targetLanguage,
                            inputText = text,
                            translatedText = displayResponse.translatedText,
                        )
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
                        if (isDirectBulosTranslationUnavailable(result.message)) {
                            directBulosUnavailableNoticeId++
                        } else {
                            textTranslationEventsChannel.send(
                                TextTranslationEvent.ShowError(
                                    translationFailurePresentation(result.message).title,
                                ),
                            )
                        }
                    }
                }
            } finally {
                isTranslating = false
                translationStatus = ""
            }
        }
    }

    fun resetTextTranslationResult() {
        textTranslationJob?.cancel()
        textTranslationJob = null
        isTranslating = false
        translationStatus = ""
        textTranslationState = TextTranslationUiState.Idle
        TranslationState.clearTextTranslationContent()
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
        translationStatus = if (canUseOnlineServices && !isServerReady) "Waking up server..." else "Translating..."
        val sourceLanguage = TranslationState.sourceLanguage
        val targetLanguage = TranslationState.targetLanguage
        voiceTranslationJob = viewModelScope.launch {
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
                        savedTranslationRepository.record(
                            sourceLang = sourceLanguage,
                            targetLang = targetLanguage,
                            inputText = displayText,
                            translatedText = result.response.translatedText,
                        )
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
                        if (isDirectBulosTranslationUnavailable(result.message)) {
                            directBulosUnavailableNoticeId++
                        } else {
                            textTranslationEventsChannel.send(
                                TextTranslationEvent.ShowError(
                                    translationFailurePresentation(result.message).title,
                                ),
                            )
                        }
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
        viewModelScope.launch {
            savedTranslationRepository.record(
                sourceLang = TranslationState.sourceLanguage,
                targetLang = TranslationState.targetLanguage,
                inputText = input,
                translatedText = suggestion.translatedText,
            )
        }
        offlineTranslationSuggestions = emptyList()
        offlineSuggestionInput = ""
        offlineTranslationMessage = null
        textTranslationState = TextTranslationUiState.Success(response)
    }

    fun clearVoiceDraft() {
        TranslationState.sttTranscript = ""
        TranslationState.recordedAudioPath = null
        voiceInputReady = false
        speechSessionFinished = false
        speechRecognitionError = null
        isNoSpeechDetected = false
        isSpeechProcessing = false
        offlineTranslationSuggestions = emptyList()
        offlineSuggestionInput = ""
        offlineTranslationMessage = null
    }

    /** Clears only the temporary cancellation visual state before a new attempt begins. */
    fun clearVoiceCancellationGlow() {
        cancelledGlowResetJob?.cancel()
        cancelledGlowResetJob = null
        wasRecordingCancelled = false
        isNoSpeechDetected = false
    }

    /** Returns the voice flow to a clean, idle microphone state without changing its language pair. */
    fun resetVoiceTranslationForNewRecording() {
        voiceTranslationJob?.cancel()
        voiceTranslationJob = null
        isTranslating = false
        translationStatus = ""
        TranslationState.clearTextTranslationContent()
        clearVoiceDraft()
    }

    fun saveCurrentTranslation() {
        val inputText = TranslationState.textToTranslate
        val translatedText = TranslationState.translatedText
        if (inputText.isNotEmpty() && translatedText.isNotEmpty()) {
            viewModelScope.launch {
                savedTranslationRepository.ensureSaved(
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
        viewModelScope.launch {
            savedTranslationRepository.toggleSaved(
                sourceLang = TranslationState.sourceLanguage,
                targetLang = TranslationState.targetLanguage,
                inputText = inputText,
                translatedText = translatedText,
            )
        }
    }

    fun deleteSavedTranslations(timestamps: Set<Long>) {
        viewModelScope.launch { savedTranslationRepository.removeSaved(timestamps) }
    }

    fun deleteRecentTranslations(timestamps: Set<Long>) {
        viewModelScope.launch { savedTranslationRepository.delete(timestamps) }
    }

    fun toggleRecentTranslationSaved(timestamp: Long) {
        viewModelScope.launch { savedTranslationRepository.toggleSaved(timestamp) }
    }

    fun toggleSavedTranslationFavorite(timestamp: Long) {
        viewModelScope.launch { savedTranslationRepository.toggleFavorite(timestamp) }
    }

    fun addSavedTranslationsToFavorites(timestamps: Set<Long>) {
        viewModelScope.launch { savedTranslationRepository.addFavorites(timestamps) }
    }

    fun addRecentTranslationsToSaved(timestamps: Set<Long>) {
        viewModelScope.launch { savedTranslationRepository.addToSaved(timestamps) }
    }

    fun clearSavedTranslations() {
        viewModelScope.launch { savedTranslationRepository.clearSaved() }
    }

    fun startRecording() {
        if (
            isRecording || voskSpeechService != null ||
            onDeviceSpeechRecognizer != null || isRecorderStarting ||
            bulosCapture != null || bulosTranscriptionJob?.isActive == true
        ) return
        cancelledGlowResetJob?.cancel()
        cancelledGlowResetJob = null
        wasRecordingCancelled = false
        voiceInputReady = false
        speechSessionFinished = false
        speechRecognitionError = null
        isNoSpeechDetected = false
        isSpeechProcessing = false
        TranslationState.recordedAudioPath = null
        val language = TranslationState.sourceLanguage
        if (BulosOnlineStt.shouldUseBackend(language, canUseOnlineServices)) {
            startBulosOnlineRecording()
            return
        }
        // When a connection and a system recognizer are available, begin recognition without
        // waiting for a Vosk model to be installed or loaded. Vosk remains the offline route.
        if (canUseOnlineServices && canUseSystemRecognizer()) {
            usesVoskRecognizer = false
            startAndroidSpeechRecognition(language, preferOffline = false)
            return
        }
        usesVoskRecognizer = voskModelManager.supports(language)
        if (usesVoskRecognizer) {
            startVoskSpeechRecognition(language)
        } else {
            startSpeechRecognizerFallback(language)
        }
    }

    fun preloadOfflineSpeechModel(language: String) {
        if (!voskModelManager.supports(language)) return
        if (!isOfflineModePreferenceLoaded) return
        if (offlineModeEnabled && !voskModelManager.isInstalled(language)) return
        if (voskModelLanguage == language && voskModel != null) return
        val uiLanguage = language.toOfflineSpeechUiLanguage() ?: return
        val needsInstallation = !voskModelManager.isInstalled(language)
        val previousPreload = voskPreloadJob
        voskPreloadJob = viewModelScope.launch {
            previousPreload?.join()
            if (needsInstallation) updateOfflineSpeechResourceState(
                uiLanguage,
                OfflineSpeechResourceState.DOWNLOADING,
            )
            try {
                val loadedModel = withContext(Dispatchers.IO) { voskModelManager.load(language) }
                if (voskModelLanguage != language || voskModel == null) {
                    voskModel?.close()
                    voskModel = loadedModel
                    voskModelLanguage = language
                } else {
                    loadedModel.close()
                }
                refreshOfflineSpeechModelStatus()
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                if (needsInstallation) updateOfflineSpeechResourceState(
                    uiLanguage,
                    OfflineSpeechResourceState.FAILED,
                )
            } finally {
                if (offlineSpeechResourceState(uiLanguage) == OfflineSpeechResourceState.DOWNLOADING) {
                    refreshOfflineSpeechModelStatus()
                }
            }
        }
    }

    fun downloadOfflineSpeechModel(language: UiLanguage) {
        val modelLanguage = TranslationLanguageRules.languageForUi(language)
        if (!voskModelManager.supports(modelLanguage) ||
            offlineSpeechResourceState(language) == OfflineSpeechResourceState.READY
        ) return
        if (offlineSpeechResourceState(language) == OfflineSpeechResourceState.DOWNLOADING) return

        voskPreloadJob?.cancel()
        val previousPreload = voskPreloadJob
        voskPreloadJob = viewModelScope.launch {
            updateOfflineSpeechResourceState(language, OfflineSpeechResourceState.DOWNLOADING)
            try {
                previousPreload?.join()
                val loadedModel = withContext(Dispatchers.IO) { voskModelManager.load(modelLanguage) }
                if (voskModelLanguage != modelLanguage || voskModel == null) {
                    voskModel?.close()
                    voskModel = loadedModel
                    voskModelLanguage = modelLanguage
                } else {
                    loadedModel.close()
                }
                refreshOfflineSpeechModelStatus()
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                updateOfflineSpeechResourceState(language, OfflineSpeechResourceState.FAILED)
            } finally {
                if (offlineSpeechResourceState(language) == OfflineSpeechResourceState.DOWNLOADING) {
                    refreshOfflineSpeechModelStatus()
                }
            }
        }
    }

    fun offlineSpeechModelSizeMb(language: UiLanguage): Int {
        val bytes = voskModelManager.downloadSizeBytes(TranslationLanguageRules.languageForUi(language))
            ?: return 0
        return (bytes / (1024.0 * 1024.0)).roundToInt()
    }

    fun offlineSpeechResourceState(language: UiLanguage): OfflineSpeechResourceState =
        offlineSpeechResourceStates[language] ?: OfflineSpeechResourceState.NOT_DOWNLOADED

    private fun refreshOfflineSpeechModelStatus() {
        offlineSpeechResourceStates = listOf(UiLanguage.ENGLISH, UiLanguage.FILIPINO).associateWith {
            if (voskModelManager.isInstalled(TranslationLanguageRules.languageForUi(it))) {
                OfflineSpeechResourceState.READY
            } else {
                OfflineSpeechResourceState.NOT_DOWNLOADED
            }
        }
    }

    private fun updateOfflineSpeechResourceState(
        language: UiLanguage,
        state: OfflineSpeechResourceState,
    ) {
        offlineSpeechResourceStates = offlineSpeechResourceStates + (language to state)
    }

    private fun String.toOfflineSpeechUiLanguage(): UiLanguage? = when {
        equals("English", ignoreCase = true) -> UiLanguage.ENGLISH
        equals("Filipino", ignoreCase = true) || equals("Bulos", ignoreCase = true) -> UiLanguage.FILIPINO
        else -> null
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
        TranslationState.sttTranscript = ""
        val recognizer = runCatching {
            if (preferOffline && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }
        }.getOrElse {
            usesOnDeviceRecognizer = false
            isRecorderStarting = false
            recoverAndroidSpeechRecognizer(language, preferOffline)
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
                    recoverAndroidSpeechRecognizer(language, preferOffline)
                } else {
                    isNoSpeechDetected = error == SpeechRecognizer.ERROR_NO_MATCH ||
                        error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
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

    private fun startSpeechRecognizerFallback(language: String) {
        when {
            canUseOnDeviceRecognizer() -> startAndroidSpeechRecognition(language, preferOffline = true)
            canUseOnlineServices && canUseSystemRecognizer() -> startAndroidSpeechRecognition(language, preferOffline = false)
            else -> finishRecognizerUnavailable()
        }
    }

    private fun recoverAndroidSpeechRecognizer(language: String, attemptedOffline: Boolean) {
        if (attemptedOffline && canUseOnlineServices && canUseSystemRecognizer()) {
            startAndroidSpeechRecognition(language, preferOffline = false)
        } else {
            finishRecognizerUnavailable()
        }
    }

    private fun finishRecognizerUnavailable() {
        usesVoskRecognizer = false
        usesOnDeviceRecognizer = false
        isRecorderStarting = false
        isSpeechProcessing = false
        isRecording = false
        speechSessionFinished = true
        speechRecognitionError = if (offlineModeEnabled) {
            offlineResourcesUnavailableMessage()
        } else {
            "Speech recognition is unavailable on this device."
        }
    }

    private fun androidRecognizerIntent(
        packageName: String,
        language: String,
        preferOffline: Boolean,
    ) =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            val tag = if (
                language.equals("Filipino", ignoreCase = true) ||
                language.equals("Bulos", ignoreCase = true)
            ) {
                "fil-PH"
            } else {
                "en-PH"
            }
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
            ?.let { TranslationState.sttTranscript = it }
    }

    private fun shouldFallbackFromOnDevice(error: Int): Boolean {
        val isApi31RecognitionError = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (
            error == SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ||
                error == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE ||
                error == SpeechRecognizer.ERROR_SERVER_DISCONNECTED
            )
        return isApi31RecognitionError ||
            error == SpeechRecognizer.ERROR_SERVER ||
            error == SpeechRecognizer.ERROR_CLIENT
    }

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
            voiceInputReady = TranslationState.sttTranscript.isNotBlank()
            isNoSpeechDetected = !voiceInputReady &&
                (isNoSpeechDetected || speechRecognitionError == null)
            speechSessionFinished = true
        }
    }

    private fun startVoskSpeechRecognition(language: String) {
        isRecorderStarting = true
        isSpeechProcessing = true
        val sessionId = ++speechSessionId
        voskCommittedText = ""
        TranslationState.sttTranscript = ""
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
                val service = LowLatencyVoskSpeechService(
                    recognizer = recognizer,
                    sampleRate = VOSK_SAMPLE_RATE,
                    onAmplitude = { amplitude ->
                        if (sessionId == speechSessionId && isRecording) {
                            speechAmplitude = amplitude
                        }
                    },
                )
                voskSpeechService = service
                service.startListening(voskListener(sessionId))
                isRecorderStarting = false
                isSpeechProcessing = false
                isRecording = true
                startTimer()
            } catch (error: Exception) {
                if (sessionId != speechSessionId) return@launch
                usesVoskRecognizer = false
                startSpeechRecognizerFallback(language)
            }
        }
    }

    private fun voskListener(sessionId: Long) = object : VoskRecognitionListener {
        override fun onPartialResult(hypothesis: String?) {
            val partial = parseVoskText(hypothesis, "partial")
            viewModelScope.launch {
                if (sessionId == speechSessionId && isRecording) {
                    TranslationState.sttTranscript = joinSpeechText(voskCommittedText, partial)
                }
            }
        }

        override fun onResult(hypothesis: String?) {
            val result = parseVoskText(hypothesis, "text")
            viewModelScope.launch {
                if (sessionId == speechSessionId && result.isNotEmpty()) {
                    voskCommittedText = joinSpeechText(voskCommittedText, result)
                    TranslationState.sttTranscript = voskCommittedText
                }
            }
        }

        override fun onFinalResult(hypothesis: String?) {
            val result = parseVoskText(hypothesis, "text")
            viewModelScope.launch {
                if (sessionId != speechSessionId) return@launch
                if (result.isNotEmpty()) {
                    voskCommittedText = joinSpeechText(voskCommittedText, result)
                    TranslationState.sttTranscript = voskCommittedText
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
        voiceInputReady = TranslationState.sttTranscript.isNotBlank()
        isNoSpeechDetected = !voiceInputReady && speechRecognitionError == null
        speechSessionFinished = true
    }

    private fun offlineResourcesUnavailableMessage(): String {
        val messageRes = when (uiLanguage) {
            UiLanguage.ENGLISH -> R.string.offline_resources_unavailable
            UiLanguage.FILIPINO -> R.string.offline_resources_unavailable_fil
            UiLanguage.BULOS -> R.string.offline_resources_unavailable_bul
        }
        return getApplication<Application>().getString(messageRes)
    }

    /** Bulos-only online path. English and Filipino retain their existing recognizer routes. */
    private fun startBulosOnlineRecording() {
        val capture = IsolatedPcmWavCapture(getApplication<Application>())
        val sessionId = ++speechSessionId
        isRecorderStarting = true
        isSpeechProcessing = true
        usesBulosOnlineCapture = true
        TranslationState.sttTranscript = ""
        if (!capture.start()) {
            usesBulosOnlineCapture = false
            isRecorderStarting = false
            isSpeechProcessing = false
            speechSessionFinished = true
            speechRecognitionError = "Microphone recording could not be started. Please try again."
            return
        }
        if (sessionId != speechSessionId) {
            capture.cancel()
            return
        }
        bulosCapture = capture
        isRecorderStarting = false
        isSpeechProcessing = false
        isRecording = true
        startTimer()
    }

    private suspend fun transcribeBulosRecording(sessionId: Long, wav: File) {
        try {
            val recognizedText = try {
                val response = withContext(Dispatchers.IO) {
                    RecordedPcmWavValidator.validate(wav)
                    bulosSttApi.transcribe(
                        audio = BackendSttClientFactory.audioPart(wav, HybridSttAudioFormat.WAV_MIME_TYPE),
                        language = BackendSttClientFactory.languagePart("bul"),
                    )
                }
                BulosOnlineStt.recognizedText(response)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                transcribeBulosOfflineFallback(wav, sessionId)
            }
            if (sessionId != speechSessionId) return
            if (recognizedText.isBlank()) {
                finishBulosRecognition(
                    sessionId,
                    text = null,
                    error = "Online Bulos transcription failed and the Filipino offline fallback is unavailable.",
                )
            } else {
                finishBulosRecognition(sessionId, text = recognizedText, error = null)
            }
        } catch (error: CancellationException) {
            throw error
        } finally {
            wav.delete()
            if (sessionId == speechSessionId) {
                bulosWavFile = null
                bulosCapture = null
                usesBulosOnlineCapture = false
            }
        }
    }

    private suspend fun transcribeBulosOfflineFallback(wav: File, sessionId: Long): String =
        runCatching {
            withContext(Dispatchers.IO) {
                if (!voskModelManager.isInstalled("Bulos")) return@withContext ""
                val result = RecordedAudioVoskAdapter(voskModelManager).transcribe(wav, "Bulos") {
                    sessionId != speechSessionId
                }
                parseVoskText(result, "text")
            }
        }.getOrDefault("")

    private fun finishBulosRecognition(sessionId: Long, text: String?, error: String?) {
        if (sessionId != speechSessionId) return
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        isRecorderStarting = false
        isRecording = false
        isSpeechProcessing = false
        if (text != null) {
            TranslationState.sttTranscript = text
            speechRecognitionError = null
            isNoSpeechDetected = false
            voiceInputReady = true
        } else {
            TranslationState.sttTranscript = ""
            speechRecognitionError = error
            isNoSpeechDetected = false
            voiceInputReady = false
        }
        speechSessionFinished = true
    }

    fun currentRecordingAmplitude(): Int {
        if (!isRecording) return 0
        if (usesOnDeviceRecognizer) return speechAmplitude
        if (usesVoskRecognizer) return speechAmplitude
        return 0
    }

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
        val activeBulosCapture = bulosCapture
        if (usesBulosOnlineCapture && activeBulosCapture != null) {
            recordingTimerJob?.cancel()
            recordingTimerJob = null
            isRecording = false
            isSpeechProcessing = true
            val sessionId = speechSessionId
            bulosTranscriptionJob = viewModelScope.launch {
                val wav = withContext(Dispatchers.IO) { activeBulosCapture.stop() }
                if (sessionId != speechSessionId || bulosCapture !== activeBulosCapture) {
                    wav?.delete()
                    return@launch
                }
                if (wav == null) {
                    finishBulosRecognition(
                        sessionId,
                        text = null,
                        error = "Microphone recording could not be finalized. Please try again.",
                    )
                    bulosCapture = null
                    usesBulosOnlineCapture = false
                    return@launch
                }
                bulosWavFile = wav
                transcribeBulosRecording(sessionId, wav)
            }
            return
        }
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
        finishRecognizerUnavailable()
    }

    fun cancelRecording() {
        speechSessionId++
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        bulosTranscriptionJob?.cancel()
        bulosTranscriptionJob = null
        bulosCapture?.cancel()
        bulosCapture = null
        bulosWavFile?.delete()
        bulosWavFile = null
        voskModelLoadJob?.cancel()
        voskModelLoadJob = null
        onDeviceSpeechRecognizer?.cancel()
        onDeviceSpeechRecognizer?.destroy()
        onDeviceSpeechRecognizer = null
        voskSpeechService?.stop()
        voskSpeechService?.shutdown()
        voskSpeechService = null
        speechAmplitude = 0
        isRecorderStarting = false
        isRecording = false
        recordingTime = 0L
        TranslationState.sttTranscript = ""
        TranslationState.recordedAudioPath = null
        voiceInputReady = false
        speechSessionFinished = false
        speechRecognitionError = null
        isNoSpeechDetected = false
        isSpeechProcessing = false
        usesVoskRecognizer = false
        usesOnDeviceRecognizer = false
        usesBulosOnlineCapture = false
        wasRecordingCancelled = true
        cancelledGlowResetJob?.cancel()
        cancelledGlowResetJob = viewModelScope.launch {
            delay(CANCELLED_GLOW_DURATION_MS)
            wasRecordingCancelled = false
        }
    }

    override fun onCleared() {
        networkMonitor.stop()
        backendReadinessJob?.cancel()
        connectivityChangeJob?.cancel()
        savedTranslationsObservationJob?.cancel()
        speechSessionId++
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        cancelledGlowResetJob?.cancel()
        cancelledGlowResetJob = null
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
        bulosTranscriptionJob?.cancel()
        bulosTranscriptionJob = null
        bulosCapture?.cancel()
        bulosCapture = null
        bulosWavFile?.delete()
        bulosWavFile = null
        voskModel?.close()
        voskModel = null
    }

    private companion object {
        const val VOSK_SAMPLE_RATE = 16_000f
        const val BACKEND_RECOVERY_INTERVAL_MS = 30_000L
        const val CANCELLED_GLOW_DURATION_MS = 300L
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
