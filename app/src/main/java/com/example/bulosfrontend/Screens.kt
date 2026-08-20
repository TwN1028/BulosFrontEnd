package com.example.bulosfrontend

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import com.example.bulosfrontend.ui.theme.*
import java.util.Locale

@Composable
fun OpenerScreen(vm: MainViewModel, onHome: () -> Unit) = Scaffold(topBar = { SharedTopAppBar(stringResource(R.string.header_main_eng)) }) { p ->
    Column(Design.FillMax.padding(p).padding(horizontal = Design.PaddingScreen), Design.CenterArr, Design.CenterH) {
        listOf("english" to R.string.choose_lang_eng, "filipino" to R.string.choose_lang_fil, "bulos" to R.string.choose_lang_bul).forEach { (k, r) ->
            BulosButton({ vm.updateLanguage(k); onHome() }, stringResource(r), Design.FillWidth.height(Design.ButtonHeightLarge))
            Spacer(Modifier.height(Design.SpacingLarge))
        }
    }
}

@Composable
fun HomeScreen(vm: MainViewModel, onNav: (String) -> Unit) = Scaffold(topBar = { SharedTopAppBar(stringResource(vm.content.mainHeaderRes)) }) { p ->
    Column(Design.FillMax.padding(p), horizontalAlignment = Design.CenterH) {
        Column(Modifier.weight(1f).padding(Design.PaddingElement), Design.CenterArr) {
            listOf(vm.content.button1Res to "text", vm.content.button2Res to "voice", vm.content.button3Res to "history", vm.content.button4Res to "placeholder").chunked(2).forEach { row ->
                Row(Design.FillWidth, Arrangement.SpaceEvenly) {
                    row.forEach { (r, t) -> 
                        Button({ if (t in listOf("text", "voice", "history")) onNav(t) }, Modifier.size(Design.DashboardButtonSize), shape = Design.DashboardShape, colors = Design.secondaryColors()) {
                            Text(stringResource(r), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                Spacer(Modifier.height(Design.PaddingElement))
            }
        }
        StandardFooter(vm.content.footerRes)
    }
}

@Composable
fun TranslateTextScreen(vm: MainViewModel, onTranslate: () -> Unit, onBack: () -> Unit) {
    var text by remember { mutableStateOf("") }
    Scaffold(topBar = { SharedTopAppBar(stringResource(vm.content.translateHeaderRes)) }) { p ->
        Column(Design.FillMax.padding(p).padding(Design.PaddingScreen), horizontalAlignment = Design.CenterH) {
            Row(Design.FillWidth, Arrangement.spacedBy(Design.PaddingElement), Design.CenterV) {
                LanguageSelector(Modifier.weight(1f), TranslationState.sourceLanguage) { TranslationState.sourceLanguage = it }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                LanguageSelector(Modifier.weight(1f), TranslationState.targetLanguage) { TranslationState.targetLanguage = it }
            }
            OutlinedTextField(text, { if (it.length <= 100) text = it }, Design.FillWidth.weight(1f).padding(vertical = Design.SpacingLarge), shape = Design.CardShape, placeholder = { Text(stringResource(vm.content.placeholderRes)) }, supportingText = { Text("${text.length} / 100", Design.FillWidth, textAlign = TextAlign.End) })
            BulosButton({ vm.translateText(text); onTranslate() }, stringResource(vm.content.translateActionRes), Design.FillWidth.height(Design.ButtonHeightMedium), text.isNotBlank() && TranslationState.sourceLanguage != TranslationState.targetLanguage, Design.primaryColors())
            Spacer(Modifier.height(Design.PaddingElement))
            BulosButton(onBack, stringResource(vm.content.goBackRes), Design.FillWidth.height(Design.ButtonHeightSmall), colors = Design.dangerColors())
            Spacer(Modifier.weight(0.1f))
            StandardFooter(vm.content.footerRes)
        }
    }
}

@Composable
fun TranslateVoiceScreen(vm: MainViewModel, onStop: () -> Unit, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) vm.startRecording() }
    Scaffold(topBar = { if (!vm.isRecording && !vm.isReviewingTranscription) SharedTopAppBar(stringResource(vm.content.voiceHeaderRes)) }) { p ->
        Column(Design.FillMax.padding(p).padding(Design.PaddingScreen), Design.CenterArr, Design.CenterH) {
            if (vm.isReviewingTranscription) {
                var edited by remember { mutableStateOf(TranslationState.textToTranslate) }
                Text(stringResource(vm.content.voiceHeaderRes), style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(edited, { edited = it }, Design.FillWidth.weight(1f).padding(vertical = Design.PaddingElement), shape = Design.CardShape, label = { Text(stringResource(vm.content.reviewLabelRes)) })
                BulosButton({ vm.confirmVoiceTranslation(edited); onStop() }, stringResource(vm.content.confirmActionRes), Design.FillWidth.height(Design.ButtonHeightMedium), colors = Design.primaryColors())
                Spacer(Modifier.height(Design.PaddingElement))
                BulosButton({ vm.isReviewingTranscription = false }, stringResource(vm.content.goBackRes), Design.FillWidth.height(Design.ButtonHeightSmall), colors = Design.dangerColors())
            } else if (!vm.isRecording) {
                Row(Design.FillWidth, Arrangement.spacedBy(Design.PaddingElement), Design.CenterV) {
                    LanguageSelector(Modifier.weight(1f), TranslationState.sourceLanguage) { TranslationState.sourceLanguage = it }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                    LanguageSelector(Modifier.weight(1f), TranslationState.targetLanguage) { TranslationState.targetLanguage = it }
                }
                Spacer(Modifier.weight(1f))
                Button({ if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) vm.startRecording() else launcher.launch(Manifest.permission.RECORD_AUDIO) }, Modifier.size(Design.IconSizeLarge), TranslationState.sourceLanguage != TranslationState.targetLanguage, shape = CircleShape) {
                    Text(stringResource(vm.content.recordStartLabelRes), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.weight(1f))
                BulosButton(onBack, stringResource(vm.content.goBackRes), Design.FillWidth.height(Design.ButtonHeightSmall), colors = Design.dangerColors())
            } else {
                Text(String.format(Locale.US, "%02d:%02d / 01:00", vm.recordingTime / 60, vm.recordingTime % 60), style = MaterialTheme.typography.displayLarge.copy(fontSize = Design.TimerFontSize), fontWeight = Design.FontBold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(Design.PaddingScreen))
                BulosButton({ vm.stopRecording() }, stringResource(vm.content.stopBtnRes), Design.FillWidth.height(Design.ButtonHeightMedium), colors = Design.primaryColors())
                Spacer(Modifier.height(Design.SpacingLarge))
                BulosButton({ vm.cancelRecording() }, stringResource(vm.content.cancelBtnRes), Design.FillWidth.height(Design.ButtonHeightMedium), colors = Design.dangerColors())
            }
            if (!vm.isRecording && !vm.isReviewingTranscription) StandardFooter(vm.content.footerRes)
        }
    }
}

@Composable
fun ResultScreen(vm: MainViewModel, onHome: () -> Unit) {
    val clip = LocalClipboardManager.current
    var show by remember { mutableStateOf(false) }
    Scaffold(topBar = { SharedTopAppBar(stringResource(vm.content.resultHeaderRes)) }) { p ->
        Box(Design.FillMax) {
            Column(Design.FillMax.padding(p).padding(Design.PaddingScreen).fillMaxSize(), Design.CenterArr, Design.CenterH) {
                Row(Design.FillWidth, Arrangement.spacedBy(Design.PaddingElement), Design.CenterV) {
                    LanguageLabel(Modifier.weight(1f), TranslationState.sourceLanguage)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                    LanguageLabel(Modifier.weight(1f), TranslationState.targetLanguage)
                }
                Card(Design.FillWidth.weight(1f).padding(vertical = Design.SpacingLarge), Design.CardShape, CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))) {
                    Box(Design.FillMax) {
                        SelectionContainer { Box(Design.FillMax.padding(Design.PaddingElement)) { Text(TranslationState.translatedText.ifEmpty { "Result..." }, style = MaterialTheme.typography.bodyLarge) } }
                        PoppingIconButton({ if (TranslationState.translatedText.isNotEmpty()) { clip.setText(AnnotatedString(TranslationState.translatedText)); show = true } }, Icons.Default.ContentCopy, "Copy", Modifier.padding(Design.PaddingSmall).align(Alignment.BottomEnd))
                    }
                }
                BulosButton(onHome, stringResource(vm.content.backToHomeRes), Design.FillWidth.height(Design.ButtonHeightMedium), colors = Design.dangerColors())
            }
            TopToastNotification(show, stringResource(vm.content.copiedRes), p)
            Box(Design.FillMax, Alignment.BottomCenter) { StandardFooter(vm.content.footerRes) }
        }
    }
}

@Composable
fun HistoryScreen(vm: MainViewModel, onBack: () -> Unit) = Scaffold(topBar = { SharedTopAppBar(stringResource(vm.content.historyHeaderRes)) }) { p ->
    Column(Design.FillMax.padding(p), horizontalAlignment = Design.CenterH) {
        if (HistoryProvider.history.isEmpty()) Box(Modifier.weight(1f), contentAlignment = Design.CenterAlign) { Text("No history yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline) }
        else LazyColumn(Design.FillWidth.weight(1f), contentPadding = PaddingValues(Design.PaddingElement), verticalArrangement = Arrangement.spacedBy(Design.PaddingElement)) { items(HistoryProvider.history) { HistoryCard(it) } }
        BulosButton(onBack, stringResource(vm.content.goBackRes), Design.FillWidth.padding(Design.PaddingElement).height(Design.ButtonHeightSmall), colors = Design.dangerColors())
        StandardFooter(vm.content.footerRes)
    }
}

@Composable
fun HistoryCard(item: HistoryItem) = Card(Design.FillWidth, Design.CardShape, CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(Design.AlphaSecondary))) {
    Column(Modifier.padding(Design.PaddingElement), horizontalAlignment = Design.CenterH) {
        Text("${item.sourceLang}: ${item.inputText}", style = MaterialTheme.typography.bodyLarge, fontWeight = Design.FontBold, textAlign = TextAlign.Center)
        Icon(Icons.Default.ArrowDownward, null, Modifier.size(Design.IconSizeSmall).padding(Design.PaddingSmall), tint = MaterialTheme.colorScheme.primary)
        Text("${item.targetLang}: ${item.translatedText}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
    }
}

@Composable
fun LanguageLabel(modifier: Modifier = Modifier, lang: String) = Surface(modifier.height(Design.ButtonHeightSelector), Design.ButtonShape, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer) {
    Box(contentAlignment = Design.CenterAlign) { Text(lang, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge) }
}

@Composable
fun LanguageSelector(modifier: Modifier = Modifier, selected: String, onSelected: (String) -> Unit) {
    var exp by remember { mutableStateOf(false) }
    Box(modifier) {
        BulosButton({ exp = true }, selected, Design.FillWidth.height(Design.ButtonHeightSelector))
        DropdownMenu(exp, { exp = false }, Modifier.fillMaxWidth(0.4f)) {
            listOf(R.string.lang_label_eng, R.string.lang_label_fil, R.string.lang_label_bul).forEach { r ->
                val l = stringResource(r)
                DropdownMenuItem(text = { Text(l) }, onClick = { onSelected(l); exp = false })
            }
        }
    }
}
