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
    // UI Language State
    var dialogueKey by mutableStateOf("english")
    val content get() = DialogueProvider.getDialogue(dialogueKey)

    // Recording State
    var isRecording by mutableStateOf(false)
    var recordingTime by mutableLongStateOf(0L)
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun updateLanguage(key: String) {
        dialogueKey = key
    }

    fun translateText(text: String) {
        TranslationState.textToTranslate = text
        // Placeholder for actual translation logic
        TranslationState.translatedText = text
        saveToHistory()
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
            TranslationState.translatedText = "Voice translation placeholder"
            TranslationState.textToTranslate = "Voice Recording" // Label for history
            saveToHistory()
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
