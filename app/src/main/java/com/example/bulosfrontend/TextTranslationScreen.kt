package com.example.bulosfrontend

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TranslateTextScreen(vm: MainViewModel, onTranslate: () -> Unit, onBack: () -> Unit) {
    var text by remember { mutableStateOf("") }
    Scaffold(topBar = { SharedTopAppBar(stringResource(vm.content.translateHeaderRes)) }) { p ->
        Column(Design.FillMax.padding(p).padding(Design.PaddingScreen), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Design.FillWidth, Arrangement.spacedBy(Design.PaddingElement), verticalAlignment = Alignment.CenterVertically) {
                LanguageSelector(Modifier.weight(1f), TranslationState.sourceLanguage, { TranslationState.sourceLanguage = it })
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                LanguageSelector(Modifier.weight(1f), TranslationState.targetLanguage, { TranslationState.targetLanguage = it })
            }
            OutlinedTextField(
                value = text, 
                onValueChange = { if (it.length <= 100) text = it }, 
                modifier = Design.FillWidth.weight(1f).padding(vertical = Design.SpacingLarge), 
                shape = Design.CardShape, 
                placeholder = { Text(stringResource(vm.content.placeholderRes)) }, 
                supportingText = { Text("${text.length} / 100", Design.FillWidth, textAlign = TextAlign.End) }
            )
            BulosButton(
                onClick = { vm.translateText(text); onTranslate() }, 
                text = stringResource(vm.content.translateActionRes), 
                modifier = Design.FillWidth.height(Design.ButtonHeightMedium), 
                enabled = text.isNotBlank() && TranslationState.sourceLanguage != TranslationState.targetLanguage, 
                colors = Design.primaryColors()
            )
            Spacer(Modifier.height(Design.PaddingElement))
            BulosButton(onBack, stringResource(vm.content.goBackRes), Design.FillWidth.height(Design.ButtonHeightSmall), colors = Design.dangerColors())
            Spacer(Modifier.weight(0.1f))
            StandardFooter(vm.content.footerRes)
        }
    }
}
