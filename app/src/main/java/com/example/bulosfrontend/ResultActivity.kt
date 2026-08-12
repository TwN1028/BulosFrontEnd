package com.example.bulosfrontend

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class ResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val key = intent.getStringExtra("dialogue1") ?: "none"
        val content = DialogueProvider.getDialogue(key)
        setContent {
            BulosFrontEndTheme {
                ResultScreen(content) {
                    startActivity(
                        Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra("dialogue1", key)
                        },
                    )
                    finish()
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun ResultScreen(content: DialogueContent, onBackToHome: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    var showNotif by remember { mutableStateOf(value = false) }
    LaunchedEffect(showNotif) {
        if (showNotif) {
            delay(2.seconds)
            showNotif = false
        }
    }

    Scaffold(topBar = { SharedTopAppBar(stringResource(content.resultHeaderRes)) }) { p ->
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.padding(p).padding(32.dp).fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp), Alignment.CenterVertically) {
                    LanguageLabel(Modifier.weight(1f), TranslationState.sourceLanguage)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                    LanguageLabel(Modifier.weight(1f), TranslationState.targetLanguage)
                }
                Spacer(Modifier.height(24.dp))
                Card(Modifier.fillMaxWidth().weight(1f), RoundedCornerShape(16.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                    Box(Modifier.fillMaxSize()) {
                        SelectionContainer { Box(Modifier.fillMaxSize().padding(16.dp)) { Text(TranslationState.translatedText.ifEmpty { "Result..." }, style = MaterialTheme.typography.bodyLarge) } }
                        PoppingIconButton(
                            onClick = {
                                if (TranslationState.translatedText.isNotEmpty()) {
                                    clipboard.setText(AnnotatedString(TranslationState.translatedText))
                                    showNotif = true
                                }
                            },
                            icon = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier
                                .padding(12.dp)
                                .align(Alignment.BottomEnd),
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(onClick = onBackToHome, modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(content.backToHomeRes), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
            TopToastNotification(showNotif, stringResource(content.copiedRes), p)
            Box(Modifier.fillMaxSize(), Alignment.BottomCenter) { StandardFooter(content.footerRes) }
        }
    }
}

@Composable
fun LanguageLabel(modifier: Modifier = Modifier, language: String) = Surface(modifier.height(56.dp), RoundedCornerShape(12.dp), MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer) {
    Box(contentAlignment = Alignment.Center) { Text(language, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge) }
}
