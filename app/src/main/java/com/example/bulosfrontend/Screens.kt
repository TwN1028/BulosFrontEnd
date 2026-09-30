package com.example.bulosfrontend

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.example.bulosfrontend.ui.theme.*
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TranslateTextScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onHome: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val content = viewModel.content
    val labels = content.textTranslation
    val resultLabels = content.speechResult
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val translateButtonRequester = remember { BringIntoViewRequester() }
    var isInputFocused by remember { mutableStateOf(false) }
    var showCopiedFeedback by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val currentLocale = remember(configuration) {
        ConfigurationCompat.getLocales(configuration)[0] ?: Locale.ROOT
    }
    val isTranslationLoading = viewModel.textTranslationState is TextTranslationUiState.Loading
    val successfulResponse =
        (viewModel.textTranslationState as? TextTranslationUiState.Success)?.response
            ?.takeIf { it.originalText == text && it.translatedText.isNotBlank() }
    val successfulTranslation = successfulResponse?.translatedText
    val hasSuccessfulResult = successfulTranslation != null
    val translationCardHeight = if (hasSuccessfulResult) 156.dp else 220.dp
    val inputFieldHeight = if (hasSuccessfulResult) 66.dp else 130.dp

    fun submitTranslation() {
        if (text.isBlank() || isTranslationLoading) return
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
        viewModel.translateText(text)
    }

    LaunchedEffect(isInputFocused) {
        if (isInputFocused) {
            delay(300)
            translateButtonRequester.bringIntoView()
        }
    }

    LaunchedEffect(showCopiedFeedback) {
        if (showCopiedFeedback) {
            delay(1_800)
            showCopiedFeedback = false
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.textTranslationEvents.collect { event ->
            when (event) {
                TextTranslationEvent.NavigateToResult -> Unit
                is TextTranslationEvent.ShowError ->
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    Surface(Modifier.fillMaxSize(), color = Color(0xFFFFFBF4)) {
        Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            FeaturePatternHeader(
                title = stringResource(content.translateHeaderRes),
                subtitle = stringResource(labels.subtitleRes),
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
                    modifier = Modifier.offset(y = (-8).dp).height(50.dp),
                    useHomeCardStyle = true,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .offset(y = (-4).dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(labels.translateFromRes),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.width(60.dp))
                    Text(
                        text = stringResource(labels.translateToLabelRes),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(21.dp))
                TranslationInputCard(
                    label = TranslationState.sourceLanguage.uppercase(currentLocale),
                    value = text,
                    placeholder = stringResource(labels.inputPlaceholderRes, TranslationState.sourceLanguage),
                    onValueChange = { if (it.length <= 100) text = it },
                    minimumHeight = translationCardHeight,
                    textFieldHeight = inputFieldHeight,
                    headerAction = if (text.isNotEmpty()) {
                        {
                            TextButton(
                                onClick = { text = "" },
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            ) {
                                Text(
                                    text = stringResource(labels.clearRes),
                                    color = Color(0xFF36583A),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    footer = {
                        Text(
                            stringResource(content.characterCountRes, text.length, 100),
                            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    onSubmit = ::submitTranslation,
                    onFocusChanged = { isInputFocused = it },
                )
                Spacer(Modifier.height(if (hasSuccessfulResult) 8.dp else 16.dp))
                if (hasSuccessfulResult) {
                    Text(
                        text = "${TranslationState.sourceLanguage} → ${TranslationState.targetLanguage}",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Button(
                        onClick = ::submitTranslation,
                        enabled = text.isNotBlank() && !isTranslationLoading,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .widthIn(max = 480.dp)
                            .fillMaxWidth()
                            .bringIntoViewRequester(translateButtonRequester)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F7045),
                            contentColor = WarmWhite,
                            disabledContainerColor = Sand.copy(alpha = 0.50f),
                            disabledContentColor = MutedText,
                        ),
                        elevation = ButtonDefaults.buttonElevation(disabledElevation = 0.dp),
                    ) {
                        if (isTranslationLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MutedText,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(labels.translatingRes), fontWeight = FontWeight.Bold)
                        } else {
                            Text(
                                stringResource(labels.translateToRes, TranslationState.targetLanguage),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(if (hasSuccessfulResult) 8.dp else 16.dp))
                TranslationOutputCard(
                    label = TranslationState.targetLanguage.uppercase(currentLocale),
                    translatedText = successfulTranslation,
                    placeholder = stringResource(labels.outputPlaceholderRes),
                    minimumHeight = translationCardHeight,
                )
                if (hasSuccessfulResult) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ResultSecondaryAction(
                            text = stringResource(resultLabels.copyRes),
                            icon = Icons.Default.ContentCopy,
                            onClick = {
                                coroutineScope.launch {
                                    clipboard.setClipEntry(
                                        ClipEntry(
                                            ClipData.newPlainText(
                                                "Translated text",
                                                successfulTranslation,
                                            ),
                                        ),
                                    )
                                    showCopiedFeedback = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                        ResultSecondaryAction(
                            text = stringResource(resultLabels.shareRes),
                            icon = Icons.Default.Share,
                            emphasized = true,
                            onClick = {
                                val shareText =
                                    "${TranslationState.sourceLanguage} → ${TranslationState.targetLanguage}\n\n$successfulTranslation"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        intent,
                                        context.getString(resultLabels.shareChooserTitleRes),
                                    ),
                                )
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = viewModel::resetTextTranslationResult,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .widthIn(max = 480.dp)
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F7045),
                            contentColor = WarmWhite,
                        ),
                    ) {
                        Text(
                            stringResource(resultLabels.translateAgainRes),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 12.dp),
            ) {
                TextButton(
                    onClick = onHome,
                    modifier = Modifier.align(Alignment.Center).height(48.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = stringResource(labels.backRes),
                        color = Color(0xFF36583A),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
            TopToastNotification(
                visible = showCopiedFeedback,
                message = stringResource(content.copiedRes),
                innerPadding = PaddingValues(top = 88.dp),
            )
        }
    }
}

@Composable
private fun TranslationOutputCard(
    label: String,
    translatedText: String?,
    placeholder: String,
    minimumHeight: Dp,
) {
    Card(
        modifier = Modifier.fillMaxWidth().heightIn(min = minimumHeight),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            TranslationInputBorderWidth,
            TranslationInputBorderColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = translatedText?.takeIf { it.isNotBlank() } ?: placeholder,
                color = if (translatedText.isNullOrBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.bodyLarge,
            )
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
    val allItemsSelected = historyItems.isNotEmpty() && selectedTimestamps.size == historyItems.size

    LaunchedEffect(historyItems.toList()) {
        selectedTimestamps = selectedTimestamps.intersect(historyItems.mapTo(mutableSetOf()) { it.timestamp })
        if (historyItems.isEmpty()) selectionMode = false
    }

    BackHandler(enabled = selectionMode) {
        selectedTimestamps = emptySet()
        selectionMode = false
    }

    Surface(Modifier.fillMaxSize(), color = Color(0xFFFFFBF4)) {
        OverlappingHeaderLayout(
            // The list has 16.dp top padding, leaving a visible 24.dp card overlap.
            overlap = 40.dp,
            modifier = Modifier.fillMaxSize(),
            header = {
            SharedTopAppBar(
                title = stringResource(content.historyHeaderRes),
                logoDescriptionRes = content.home.appLogoDescriptionRes,
                iconRes = R.drawable.ic_saved_history_reference,
                subtitle = stringResource(content.home.historySubtitleRes),
                trailingContent = {
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
                },
                selectionMode = selectionMode,
                selectionContent = {
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
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            selectedTimestamps = if (allItemsSelected) {
                                emptySet()
                            } else {
                                historyItems.mapTo(mutableSetOf()) { it.timestamp }
                            }
                        },
                    ) {
                        Text(
                            stringResource(if (allItemsSelected) R.string.deselect_all else R.string.select_all),
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
