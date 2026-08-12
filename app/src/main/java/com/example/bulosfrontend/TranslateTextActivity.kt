package com.example.bulosfrontend

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.*

class TranslateTextActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val key = intent.getStringExtra("dialogue1") ?: "none"
        val content = DialogueProvider.getDialogue(key)
        setContent {
            BulosFrontEndTheme {
                TranslateTextScreen(
                    content = content,
                    onTranslate = { text ->
                        TranslationState.textToTranslate = text
                        TranslationState.translatedText = "Translated: $text"
                        startActivity(Intent(this, ResultActivity::class.java).putExtra("dialogue1", key))
                    },
                ) { finish() }
            }
        }
    }
}

@Composable
fun TranslateTextScreen(content: DialogueContent, onTranslate: (String) -> Unit, onBack: () -> Unit) {
    var text by remember { mutableStateOf(value = "") }
    Scaffold(topBar = { SharedTopAppBar(stringResource(content.translateHeaderRes)) }) { p ->
        Column(Modifier.padding(p).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.padding(32.dp).weight(1f), Arrangement.Center, Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp), Alignment.CenterVertically) {
                    LanguageSelector(Modifier.weight(1f), TranslationState.sourceLanguage) { TranslationState.sourceLanguage = it }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                    LanguageSelector(Modifier.weight(1f), TranslationState.targetLanguage) { TranslationState.targetLanguage = it }
                }
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 100) text = it },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text(stringResource(content.placeholderRes)) },
                    supportingText = { Text("${text.length} / 100", Modifier.fillMaxWidth(), textAlign = TextAlign.End) },
                )
                Spacer(Modifier.height(24.dp))
                Button({ onTranslate(text) }, Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(content.translateActionRes), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text(stringResource(content.goBackRes), style = MaterialTheme.typography.titleMedium)
                }
            }
            StandardFooter(content.footerRes)
        }
    }
}

@Composable
fun LanguageSelector(modifier: Modifier = Modifier, selected: String, onSelected: (String) -> Unit) {
    var exp by remember { mutableStateOf(value = false) }
    val langs = listOf(stringResource(R.string.lang_label_eng), stringResource(R.string.lang_label_fil), stringResource(R.string.lang_label_bul))
    Box(modifier) {
        Button({ exp = true }, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp)) {
            Text(selected, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
        }
        DropdownMenu(exp, { exp = false }, Modifier.fillMaxWidth(0.4f)) {
            langs.forEach { lang -> DropdownMenuItem(text = { Text(lang) }, onClick = { onSelected(lang); exp = false }) }
        }
    }
}
