package com.example.bulosfrontend

import android.app.Application
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

    // Recording State
    var isRecording by mutableStateOf(false)
    var recordingTime by mutableLongStateOf(0L)
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

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
        // Placeholder for actual translation logic
        TranslationState.translatedText = text
        saveToHistory()
    }

    fun translateVoiceText(text: String) {
        TranslationState.textToTranslate = text
        // Placeholder for the existing translation integration. No speech text is fabricated.
        TranslationState.translatedText = text
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
                sourceLang = TranslationState.sourceLanguage,
                targetLang = TranslationState.targetLanguage,
                inputText = TranslationState.textToTranslate,
                translatedText = TranslationState.translatedText
            )
        }
    }

    fun startRecording() {
        val context = getApplication<Application>()
        audioFile = File(context.externalCacheDir, "recording_${System.currentTimeMillis()}.mp3")
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
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
            }
        }
    }

    fun currentRecordingAmplitude(): Int {
        if (!isRecording) return 0
        return try {
            recorder?.maxAmplitude ?: 0
        } catch (_: IllegalStateException) {
            0
        } catch (_: RuntimeException) {
            0
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            recordingTime = 0L
            while (isRecording && recordingTime < 60) {
                delay(1.seconds)
                recordingTime++
            }
            if (isRecording) {
                stopRecording()
            }
        }
    }

    fun stopRecording() {
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder = null
            isRecording = false
            TranslationState.recordedAudioPath = audioFile?.absolutePath
        }
    }

    fun cancelRecording() {
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder = null
            isRecording = false
            audioFile?.delete()
            audioFile = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        recorder?.release()
    }
}
