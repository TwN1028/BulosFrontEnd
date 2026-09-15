package com.example.bulosfrontend

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

    Surface(Modifier.fillMaxSize(), color = Color(0xFFFFFBF4)) {
        Column(Modifier.fillMaxSize()) {
            FeaturePatternHeader(
                title = stringResource(content.translateHeaderRes),
                subtitle = stringResource(content.home.textSubtitleRes),
                onBack = onBack,
                iconRes = R.drawable.ic_lucide_languages,
                headerBottomExtension = AppHeaderBottomExtension,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .offset(y = (-40).dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                TranslationLanguageBar(
                    swapLanguagesDescription = stringResource(content.speechResult.swapLanguagesDescriptionRes),
                    modifier = Modifier
                        .offset(y = (-8).dp)
                        .height(50.dp),
                    useHomeCardStyle = true,
                )
                Spacer(Modifier.height(18.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = TranslationCardMinHeight)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false,
                            ambientColor = Color.Black.copy(alpha = 0.08f),
                            spotColor = Color.Black.copy(alpha = 0.08f),
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        Color(0xFFE5DDD2).copy(alpha = 0.65f),
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                        Text(
                            TranslationState.sourceLanguage.uppercase(Locale.getDefault()),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(12.dp))
                        BasicTextField(
                            value = text,
                            onValueChange = { if (it.length <= 100) text = it },
                            modifier = Modifier.fillMaxWidth().height(252.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = Aileron,
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Default,
                            ),
                            decorationBox = { innerTextField ->
                                Box(Modifier.fillMaxSize()) {
                                    if (text.isEmpty()) {
                                        Text(
                                            stringResource(labels.inputPlaceholderRes, TranslationState.sourceLanguage),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Aileron),
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(content.characterCountRes, text.length, 100),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                            )
                            TextButton(
                                onClick = {
                                    text = ""
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        clipboard.clearPrimaryClip()
                                    } else {
                                        clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                                    }
                                },
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.clear_all),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.translateText(text)
                        onTranslate()
                    },
                    enabled = text.isNotBlank(),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        contentColor = WarmWhite,
                        disabledContainerColor = Sand.copy(alpha = 0.50f),
                        disabledContentColor = MutedText,
                    ),
                    elevation = ButtonDefaults.buttonElevation(disabledElevation = 0.dp),
                ) {
                    Text(
                        stringResource(labels.translateToRes, TranslationState.targetLanguage),
                        fontWeight = FontWeight.Bold,
                    )
                }
                TextButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp),
                ) {
                    Text(
                        stringResource(content.goBackRes),
                        style = MaterialTheme.typography.titleMedium,
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
    var selectedTimestamps by remember { mutableStateOf(emptySet<Long>()) }
    var selectionMode by remember { mutableStateOf(false) }
    var showHistoryMenu by remember { mutableStateOf(false) }

    LaunchedEffect(historyItems.toList()) {
        selectedTimestamps = selectedTimestamps.intersect(historyItems.mapTo(mutableSetOf()) { it.timestamp })
        if (historyItems.isEmpty()) selectionMode = false
    }

    Surface(Modifier.fillMaxSize(), color = Color(0xFFFFFBF4)) {
        OverlappingHeaderLayout(
            overlap = 31.dp,
            modifier = Modifier.fillMaxSize(),
            header = {
            SharedTopAppBar(
                title = stringResource(content.historyHeaderRes),
                logoDescriptionRes = content.home.appLogoDescriptionRes,
                iconRes = R.drawable.ic_saved_history_reference,
                subtitle = stringResource(content.home.historySubtitleRes),
                trailingContent = {
                    Crossfade(targetState = selectionMode, label = "historyHeaderActions") { selecting ->
                        if (selecting) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = {
                                        selectedTimestamps = historyItems.mapTo(mutableSetOf()) { it.timestamp }
                                    },
                                ) {
                                    Text(
                                        stringResource(R.string.select_all),
                                        color = WarmWhite,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.deleteSavedTranslations(selectedTimestamps)
                                        selectedTimestamps = emptySet()
                                        selectionMode = false
                                    },
                                    enabled = selectedTimestamps.isNotEmpty(),
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.delete_selected),
                                        tint = WarmWhite,
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        selectedTimestamps = emptySet()
                                        selectionMode = false
                                    },
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.cancel_selection),
                                        tint = WarmWhite,
                                    )
                                }
                            }
                        } else {
                            Box {
                                IconButton(onClick = { showHistoryMenu = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = stringResource(R.string.saved_history_options),
                                        tint = WarmWhite,
                                    )
                                }
                                DropdownMenu(
                                    expanded = showHistoryMenu,
                                    onDismissRequest = { showHistoryMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.clear_all)) },
                                        onClick = {
                                            showHistoryMenu = false
                                            selectedTimestamps = emptySet()
                                            viewModel.clearSavedTranslations()
                                        },
                                        enabled = historyItems.isNotEmpty(),
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.select_multiple)) },
                                        onClick = {
                                            showHistoryMenu = false
                                            selectionMode = true
                                            selectedTimestamps = emptySet()
                                        },
                                        enabled = historyItems.isNotEmpty(),
                                    )
                                }
                            }
                        }
                    }
                },
            )
            },
        ) {
        Column(
            Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
                    items(historyItems, key = { it.timestamp }) { item ->
                        HistoryCard(
                            item = item,
                            selected = item.timestamp in selectedTimestamps,
                            selectionEnabled = selectionMode,
                            onSelectionChange = { selected ->
                                selectedTimestamps = if (selected) {
                                    selectedTimestamps + item.timestamp
                                } else {
                                    selectedTimestamps - item.timestamp
                                }
                            },
                            onDelete = {
                                viewModel.deleteSavedTranslations(setOf(item.timestamp))
                            },
                        )
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
}

@Composable
fun HistoryCard(
    item: HistoryItem,
    selected: Boolean = false,
    selectionEnabled: Boolean = false,
    onSelectionChange: (Boolean) -> Unit = {},
    onDelete: () -> Unit = {},
) {
    Card(
        onClick = { if (selectionEnabled) onSelectionChange(!selected) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.sourceLang, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(12.dp).padding(horizontal = 4.dp), tint = MaterialTheme.colorScheme.outline)
                    Text(item.targetLang, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                if (selectionEnabled) {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = onSelectionChange,
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_translation),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
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
