package com.example.bulosfrontend

import android.app.Application
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.runtime.*
import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Duration.Companion.seconds

class MainViewModel(app: Application) : AndroidViewModel(app) {
    var dialogueKey by mutableStateOf("english")
    val content get() = DialogueProvider.getDialogue(dialogueKey)

    var isRecording by mutableStateOf(false)
    var isReviewingTranscription by mutableStateOf(false)
    var recordingTime by mutableLongStateOf(0L)
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun updateLanguage(key: String) {
        dialogueKey = key
        val (src, tgt) = when (key) {
            "english" -> "English" to "Bulos"
            "filipino" -> "Filipino" to "Bulos"
            else -> "Bulos" to "Filipino"
        }
        TranslationState.sourceLanguage = src
        TranslationState.targetLanguage = tgt
    }

    fun translateText(text: String) {
        TranslationState.textToTranslate = text
        TranslationState.translatedText = text
        saveToHistory()
    }

    private fun saveToHistory() {
        if (TranslationState.textToTranslate.isNotEmpty() && TranslationState.translatedText.isNotEmpty()) {
            HistoryProvider.addEntry(TranslationState.sourceLanguage, TranslationState.targetLanguage, TranslationState.textToTranslate, TranslationState.translatedText)
        }
    }

    fun startRecording() {
        audioFile = File(getApplication<Application>().externalCacheDir, "recording_${System.currentTimeMillis()}.mp3")
        recorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(getApplication()) else MediaRecorder()).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile?.absolutePath)
            try { prepare(); start(); isRecording = true; startTimer() } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun startTimer() = viewModelScope.launch {
        recordingTime = 0L
        while (isRecording && recordingTime < 60) { delay(1.seconds); recordingTime++ }
        if (isRecording) stopRecording()
    }

    fun stopRecording() {
        try { recorder?.apply { stop(); release() } } catch (e: Exception) { e.printStackTrace() } finally {
            recorder = null; isRecording = false
            TranslationState.recordedAudioPath = audioFile?.absolutePath
            TranslationState.textToTranslate = "Parsed Voice Text Placeholder"
            isReviewingTranscription = true
        }
    }

    fun confirmVoiceTranslation(text: String) {
        TranslationState.textToTranslate = text
        TranslationState.translatedText = text
        saveToHistory()
        isReviewingTranscription = false
    }

    fun cancelRecording() {
        try { recorder?.apply { stop(); release() } } catch (e: Exception) { e.printStackTrace() } finally {
            recorder = null; isRecording = false; isReviewingTranscription = false
            audioFile?.delete(); audioFile = null
        }
    }

    override fun onCleared() { super.onCleared(); recorder?.release() }
}
