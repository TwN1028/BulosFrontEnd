package com.example.bulosfrontend

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.*
import java.util.Locale

@Composable
fun TranslateTextScreen(viewModel: MainViewModel, onTranslate: () -> Unit, onBack: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val content = viewModel.content
    val labels = content.textTranslation

    Surface(Modifier.fillMaxSize(), color = Cream) {
        Column(Modifier.fillMaxSize()) {
            FeaturePatternHeader(
                title = stringResource(content.translateHeaderRes),
                subtitle = stringResource(labels.subtitleRes),
                onBack = onBack,
                patterned = true,
                iconRes = R.drawable.ic_lucide_languages,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                TranslationLanguageBar(stringResource(content.speechResult.swapLanguagesDescriptionRes))
                Spacer(Modifier.height(18.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HomeCardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                        Text(
                            TranslationState.sourceLanguage.uppercase(Locale.getDefault()),
                            color = WarmBrown,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(12.dp))
                        BasicTextField(
                            value = text,
                            onValueChange = { if (it.length <= 100) text = it },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 126.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = DeepForestGreen),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Default,
                            ),
                            decorationBox = { innerTextField ->
                                Box(Modifier.fillMaxSize()) {
                                    if (text.isEmpty()) {
                                        Text(
                                            stringResource(labels.inputPlaceholderRes, TranslationState.sourceLanguage),
                                            color = WarmBrown.copy(alpha = 0.58f),
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(content.characterCountRes, text.length, 100),
                            color = WarmBrown.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.translateText(text)
                        onTranslate()
                    },
                    enabled = text.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        contentColor = WarmWhite,
                        disabledContainerColor = Sand,
                        disabledContentColor = WarmBrown.copy(alpha = 0.72f),
                    ),
                ) {
                    Text(
                        stringResource(labels.translateToRes, TranslationState.targetLanguage),
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val content = viewModel.content
    val historyItems = HistoryProvider.history

    Scaffold(topBar = { SharedTopAppBar(stringResource(content.historyHeaderRes), content.home.appLogoDescriptionRes, R.drawable.ic_lucide_history) }) { p ->
        Column(Modifier.padding(p).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (historyItems.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(stringResource(content.noHistoryRes), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
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
