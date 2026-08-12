package com.example.bulosfrontend

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import com.example.bulosfrontend.ui.theme.BulosFrontEndTheme
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

class TranslateVoiceActivity : ComponentActivity() {
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val key = intent.getStringExtra("dialogue1") ?: "none"
        val content = DialogueProvider.getDialogue(key)
        setContent {
            BulosFrontEndTheme {
                TranslateVoiceScreen(
                    content = content,
                    onStart = { startRecording() },
                    onStop = {
                        stopRecording()
                        TranslationState.recordedAudioPath = audioFile?.absolutePath
                        TranslationState.translatedText = "Voice translation placeholder"
                        startActivity(Intent(this, ResultActivity::class.java).putExtra("dialogue1", key))
                    },
                    onCancel = { cancelRecording() },
                ) { finish() }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun startRecording() {
        audioFile = File(externalCacheDir, "recording_${System.currentTimeMillis()}.mp3")
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(this) else MediaRecorder()
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile?.absolutePath)
            prepare()
            start()
        }
    }

    private fun stopRecording() = try {
        recorder?.stop()
        recorder?.release()
    } catch (_: Exception) {
    } finally {
        recorder = null
    }

    private fun cancelRecording() = stopRecording().also { audioFile?.delete(); audioFile = null }
    override fun onDestroy() {
        super.onDestroy()
        recorder?.release()
    }
}

@Composable
fun TranslateVoiceScreen(content: DialogueContent, onStart: () -> Unit, onStop: () -> Unit, onCancel: () -> Unit, onBack: () -> Unit) {
    var rec by remember { mutableStateOf(value = false) }
    var time by remember { mutableLongStateOf(0L) }
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) { rec = true; onStart() } }

    LaunchedEffect(rec) {
        if (rec) {
            time = 0L
            while (rec && (time < 60)) {
                delay(1.seconds)
                time++
            }
            if (rec) {
                rec = false
                onStop()
            }
        }
    }

    Scaffold(topBar = { if (!rec) SharedTopAppBar(stringResource(content.voiceHeaderRes)) }) { p ->
        Column(Modifier.padding(p).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.padding(32.dp).weight(1f), Arrangement.Center, Alignment.CenterHorizontally) {
                if (!rec) {
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp), Alignment.CenterVertically) {
                        LanguageSelector(Modifier.weight(1f), TranslationState.sourceLanguage) { TranslationState.sourceLanguage = it }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                        LanguageSelector(Modifier.weight(1f), TranslationState.targetLanguage) { TranslationState.targetLanguage = it }
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                rec = true
                                onStart()
                            } else {
                                launcher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.size(200.dp),
                        shape = CircleShape,
                    ) {
                        Text(stringResource(content.recordStartLabelRes), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text(stringResource(content.goBackRes), style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    Text(
                        text = String.format(Locale.US, "%02d:%02d / 01:00", time / 60, time % 60),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(48.dp))
                    Button({ rec = false; onStop() }, Modifier.size(120.dp), shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("STOP", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(24.dp))
                    TextButton(onClick = { rec = false; onCancel() }) {
                        Text(stringResource(content.cancelBtnRes), color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            if (!rec) StandardFooter(content.footerRes)
        }
    }
}
