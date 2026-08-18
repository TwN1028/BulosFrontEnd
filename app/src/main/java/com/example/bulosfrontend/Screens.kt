package com.example.bulosfrontend

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

@Composable
fun OpenerScreen(viewModel: MainViewModel, onNavigateToHome: () -> Unit) {
    val options = listOf(
        "english" to R.string.choose_lang_eng,
        "filipino" to R.string.choose_lang_fil,
        "bulos" to R.string.choose_lang_bul
    )
    Scaffold(topBar = { SharedTopAppBar(stringResource(R.string.header_main_eng)) }) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .padding(horizontal = 32.dp),
            Arrangement.Center,
            Alignment.CenterHorizontally
        ) {
            options.forEach { (key, res) ->
                Button(
                    onClick = {
                        viewModel.updateLanguage(key)
                        onNavigateToHome()
                    },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(res), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                }
                if (key != options.last().first) Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    val content = viewModel.content
    Scaffold(topBar = { SharedTopAppBar(stringResource(content.mainHeaderRes)) }) { p ->
        Column(Modifier.padding(p).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            val buttons = listOf(
                content.button1Res to "text",
                content.button2Res to "voice",
                content.button3Res to "history",
                content.button4Res to "placeholder"
            )
            val chunkedButtons = buttons.chunked(2)
            Column(Modifier.weight(1f).padding(16.dp), Arrangement.Center, Alignment.CenterHorizontally) {
                chunkedButtons.forEachIndexed { index, row ->
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                        row.forEach { (res, type) ->
                            SquareButton(stringResource(res)) {
                                if (type == "text" || type == "voice" || type == "history") onNavigate(type)
                            }
                        }
                    }
                    if (index == 0) Spacer(Modifier.height(16.dp))
                }
            }
            StandardFooter(content.footerRes)
        }
    }
}

@Composable
fun SquareButton(text: String, onClick: () -> Unit) = Button(
    onClick = onClick,
    modifier = Modifier.size(160.dp),
    shape = RoundedCornerShape(24.dp)
) {
    Text(text, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
}

@Composable
fun TranslateTextScreen(viewModel: MainViewModel, onTranslate: () -> Unit, onBack: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val content = viewModel.content
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
                Button(
                    onClick = {
                        viewModel.translateText(text)
                        onTranslate()
                    },
                    enabled = text.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
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
fun TranslateVoiceScreen(viewModel: MainViewModel, onStop: () -> Unit, onBack: () -> Unit) {
    val content = viewModel.content
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) viewModel.startRecording()
    }

    Scaffold(topBar = { if (!viewModel.isRecording) SharedTopAppBar(stringResource(content.voiceHeaderRes)) }) { p ->
        Column(Modifier.padding(p).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.padding(32.dp).weight(1f), Arrangement.Center, Alignment.CenterHorizontally) {
                if (!viewModel.isRecording) {
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp), Alignment.CenterVertically) {
                        LanguageSelector(Modifier.weight(1f), TranslationState.sourceLanguage) { TranslationState.sourceLanguage = it }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                        LanguageSelector(Modifier.weight(1f), TranslationState.targetLanguage) { TranslationState.targetLanguage = it }
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                viewModel.startRecording()
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
                        text = String.format(Locale.US, "%02d:%02d / 01:00", viewModel.recordingTime / 60, viewModel.recordingTime % 60),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(48.dp))
                    Button(
                        onClick = {
                            viewModel.stopRecording()
                            onStop()
                        },
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("STOP", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(24.dp))
                    TextButton(onClick = { viewModel.cancelRecording() }) {
                        Text(stringResource(content.cancelBtnRes), color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            if (!viewModel.isRecording) StandardFooter(content.footerRes)
        }
    }
}

@Composable
fun ResultScreen(viewModel: MainViewModel, onBackToHome: () -> Unit) {
    val content = viewModel.content
    val clipboard = LocalClipboardManager.current
    var showNotif by remember { mutableStateOf(false) }

    Scaffold(topBar = { SharedTopAppBar(stringResource(content.resultHeaderRes)) }) { p ->
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.padding(p).padding(32.dp).fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp), Alignment.CenterVertically) {
                    LanguageLabel(Modifier.weight(1f), TranslationState.sourceLanguage)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                    LanguageLabel(Modifier.weight(1f), TranslationState.targetLanguage)
                }
                Spacer(Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(Modifier.fillMaxSize()) {
                        SelectionContainer {
                            Box(Modifier.fillMaxSize().padding(16.dp)) {
                                Text(TranslationState.translatedText.ifEmpty { "Result..." }, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
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
                Button(
                    onClick = onBackToHome,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(content.backToHomeRes), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
            TopToastNotification(showNotif, stringResource(content.copiedRes), p)
            Box(Modifier.fillMaxSize(), Alignment.BottomCenter) { StandardFooter(content.footerRes) }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val content = viewModel.content
    val historyItems = HistoryProvider.history

    Scaffold(topBar = { SharedTopAppBar(stringResource(content.historyHeaderRes)) }) { p ->
        Column(Modifier.padding(p).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (historyItems.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No history yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(historyItems) { item ->
                        HistoryCard(item)
                    }
                }
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp)) {
                Text(stringResource(content.goBackRes), style = MaterialTheme.typography.titleMedium)
            }
            StandardFooter(content.footerRes)
        }
    }
}

@Composable
fun HistoryCard(item: HistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.sourceLang, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(12.dp).padding(horizontal = 4.dp), tint = MaterialTheme.colorScheme.outline)
                Text(item.targetLang, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(8.dp))
            Text(item.inputText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(item.translatedText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LanguageLabel(modifier: Modifier = Modifier, language: String) = Surface(
    modifier = modifier.height(56.dp),
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(contentAlignment = Alignment.Center) {
        Text(language, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun LanguageSelector(modifier: Modifier = Modifier, selected: String, onSelected: (String) -> Unit) {
    var exp by remember { mutableStateOf(false) }
    val langs = listOf(
        stringResource(R.string.lang_label_eng),
        stringResource(R.string.lang_label_fil),
        stringResource(R.string.lang_label_bul)
    )
    Box(modifier) {
        Button(
            onClick = { exp = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(selected, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
        }
        DropdownMenu(expanded = exp, onDismissRequest = { exp = false }, modifier = Modifier.fillMaxWidth(0.4f)) {
            langs.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang) },
                    onClick = {
                        onSelected(lang)
                        exp = false
                    }
                )
            }
        }
    }
}
