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
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Duration.Companion.seconds

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val languagePreferences = LanguagePreferenceRepository(application)
    private val savedTranslationRepository = SavedTranslationRepository(application)
    private val translationRepository = TranslationServiceProvider.repository(application)
    private val textTranslationEventsChannel = Channel<TextTranslationEvent>(Channel.BUFFERED)

    var textTranslationState by mutableStateOf<TextTranslationUiState>(TextTranslationUiState.Idle)
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
    val uiLanguage: UiLanguage get() = selectedUiLanguage ?: UiLanguage.ENGLISH
    val content: DialogueContent get() = DialogueProvider.getDialogue(uiLanguage)

    // Recording State
    var isRecording by mutableStateOf(false)
    var recordingTime by mutableLongStateOf(0L)
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecorderStarting = false
    private var recordingTimerJob: Job? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var usesBuiltInSpeechRecognizer = false
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
            savedTranslationRepository.entries.collect { entries ->
                HistoryProvider.history.clear()
                HistoryProvider.history.addAll(entries)
            }
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
        if (textTranslationState is TextTranslationUiState.Loading || text.isBlank()) return
        val sourceLanguage = TranslationState.sourceLanguage
        val targetLanguage = TranslationState.targetLanguage
        TranslationState.textToTranslate = text
        TranslationState.translatedText = ""
        textTranslationState = TextTranslationUiState.Loading
        viewModelScope.launch {
            when (
                val result = translationRepository.translate(
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    text = text,
                )
            ) {
                is TranslationResult.Success -> {
                    TranslationState.sourceLanguage = sourceLanguage
                    TranslationState.targetLanguage = targetLanguage
                    TranslationState.textToTranslate = result.response.originalText
                    TranslationState.translatedText = result.response.translatedText
                    textTranslationState = TextTranslationUiState.Success(result.response)
                    textTranslationEventsChannel.send(TextTranslationEvent.NavigateToResult)
                }
                is TranslationResult.Failure -> {
                    textTranslationState = TextTranslationUiState.Error(result.message)
                    textTranslationEventsChannel.send(TextTranslationEvent.ShowError(result.message))
                }
            }
        }
    }

    fun translateVoiceText(text: String) {
        TranslationState.textToTranslate = text
        // Placeholder for the existing translation integration. No speech text is fabricated.
        TranslationState.translatedText = text
    }

    fun clearVoiceDraft() {
        TranslationState.textToTranslate = ""
        TranslationState.translatedText = ""
        TranslationState.recordedAudioPath = null
        voiceInputReady = false
        speechSessionFinished = false
        speechRecognitionError = null
        isSpeechProcessing = false
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

    fun deleteSavedTranslations(timestamps: Set<Long>) {
        viewModelScope.launch { savedTranslationRepository.delete(timestamps) }
    }

    fun clearSavedTranslations() {
        viewModelScope.launch { savedTranslationRepository.clear() }
    }

    fun startRecording() {
        if (isRecording || recorder != null || speechRecognizer != null || isRecorderStarting) return
        wasRecordingCancelled = false
        voiceInputReady = false
        speechSessionFinished = false
        speechRecognitionError = null
        isSpeechProcessing = false
        usesBuiltInSpeechRecognizer = supportsBuiltInRecognition(TranslationState.sourceLanguage)
        if (usesBuiltInSpeechRecognizer) {
            startBuiltInSpeechRecognition(TranslationState.sourceLanguage)
        } else {
            startAudioFileRecording()
        }
    }

    private fun supportsBuiltInRecognition(language: String): Boolean =
        language.equals("English", ignoreCase = true) ||
            language.equals("Filipino", ignoreCase = true)

    private fun recognitionLanguageTag(language: String): String =
        if (language.equals("Filipino", ignoreCase = true)) "fil-PH" else "en-PH"

    private fun startBuiltInSpeechRecognition(language: String) {
        val context = getApplication<Application>()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognitionError = "Speech recognition is unavailable on this device."
            speechSessionFinished = true
            usesBuiltInSpeechRecognizer = false
            return
        }
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer = recognizer
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isRecording = true
                isSpeechProcessing = false
                startTimer()
            }

            override fun onBeginningOfSpeech() = Unit

            override fun onRmsChanged(rmsdB: Float) {
                speechAmplitude = (((rmsdB + 2f) / 12f).coerceIn(0f, 1f) * 32767f).toInt()
            }

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                isRecording = false
                isSpeechProcessing = true
                recordingTimerJob?.cancel()
                recordingTimerJob = null
            }

            override fun onError(error: Int) {
                finishBuiltInRecognition()
                speechRecognitionError = speechRecognitionErrorMessage(error)
                speechSessionFinished = true
            }

            override fun onResults(results: Bundle?) {
                val recognizedText = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                    .trim()
                if (recognizedText.isNotEmpty()) {
                    TranslationState.textToTranslate = recognizedText
                    voiceInputReady = true
                }
                finishBuiltInRecognition()
                speechSessionFinished = true
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.trim()
                    ?.takeIf(String::isNotEmpty)
                    ?.let { TranslationState.textToTranslate = it }
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        val languageTag = recognitionLanguageTag(language)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        recognizer.startListening(intent)
    }

    private fun speechRecognitionErrorMessage(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please try again."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
            "Speech recognition network error. Please try again."
        SpeechRecognizer.ERROR_NO_MATCH -> "No speech was recognized. Please try again."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognition is busy. Please try again."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech was detected. Please try again."
        else -> "Speech recognition could not be completed. Please try again."
    }

    private fun finishBuiltInRecognition() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        speechRecognizer?.destroy()
        speechRecognizer = null
        speechAmplitude = 0
        isRecording = false
        isSpeechProcessing = false
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
        if (usesBuiltInSpeechRecognizer) return speechAmplitude
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
        if (usesBuiltInSpeechRecognizer && speechRecognizer != null) {
            recordingTimerJob?.cancel()
            recordingTimerJob = null
            isRecording = false
            isSpeechProcessing = true
            speechRecognizer?.stopListening()
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
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
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
            usesBuiltInSpeechRecognizer = false
            wasRecordingCancelled = true
        }
    }

    override fun onCleared() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        recorder?.release()
        recorder = null
    }
}

sealed interface TextTranslationUiState {
    data object Idle : TextTranslationUiState
    data object Loading : TextTranslationUiState
    data class Success(val response: TranslationResponse) : TextTranslationUiState
    data class Error(val message: String) : TextTranslationUiState
}

sealed interface TextTranslationEvent {
    data object NavigateToResult : TextTranslationEvent
    data class ShowError(val message: String) : TextTranslationEvent
}
