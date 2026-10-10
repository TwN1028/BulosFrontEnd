package com.example.bulosfrontend

import android.content.ClipData
import android.content.Intent
import android.text.format.DateUtils
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
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
    onFloatingNavigationVisibilityChanged: (Boolean) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val content = viewModel.content
    val labels = content.textTranslation
    val resultLabels = content.speechResult
    val tttTextColor = TranslationInputTextColor
    val tttContentTextColor = MaterialTheme.colorScheme.onSurface
    val shareChooserTitle = stringResource(resultLabels.shareChooserTitleRes)
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
            ?.takeIf {
                TextNormalizer.normalizeForMatching(it.originalText) ==
                    TextNormalizer.normalizeForMatching(text) &&
                    it.translatedText.isNotBlank()
            }
    val successfulTranslation = successfulResponse?.translatedText
    val hasSuccessfulResult = successfulTranslation != null
    val isSaved = hasSuccessfulResult && HistoryProvider.history.any {
        it.isSaved && it.matchesTranslation(
            TranslationState.sourceLanguage,
            TranslationState.targetLanguage,
            TranslationState.textToTranslate,
            successfulTranslation.orEmpty(),
        )
    }
    val offlineSuggestions = viewModel.offlineTranslationSuggestions.takeIf {
        TextNormalizer.normalizeForMatching(viewModel.offlineSuggestionInput) ==
            TextNormalizer.normalizeForMatching(text)
    }.orEmpty()
    val translationCardHeight = 200.dp
    val inputFieldHeight = 66.dp

    LaunchedEffect(text, hasSuccessfulResult) {
        onFloatingNavigationVisibilityChanged(text.isEmpty() || hasSuccessfulResult)
    }

    fun submitTranslation() {
        if (text.isBlank() || isTranslationLoading) return
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
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
                    uiLanguage = viewModel.uiLanguage,
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
                        color = tttTextColor,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = MaterialTheme.typography.labelLarge.fontSize,
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.width(60.dp))
                    Text(
                        text = stringResource(labels.translateToLabelRes),
                        modifier = Modifier.weight(1f),
                        color = tttTextColor,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = MaterialTheme.typography.labelLarge.fontSize,
                        ),
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(21.dp))
                TranslationInputCard(
                    label = TranslationState.sourceLanguage.uppercase(currentLocale),
                    value = text,
                    placeholder = stringResource(labels.inputPlaceholderRes, TranslationState.sourceLanguage),
                    onValueChange = { if (it.length <= 300) text = it },
                    textColor = tttTextColor,
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
                            stringResource(content.characterCountRes, text.length, 300),
                            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    footerAtBottom = true,
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
                            containerColor = SharedActiveButtonColor,
                            contentColor = WarmWhite,
                            disabledContainerColor = InactiveButtonColor,
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
                    textColor = tttContentTextColor,
                    minimumHeight = translationCardHeight,
                    isSaved = isSaved,
                    onToggleSaved = viewModel::toggleCurrentTranslationSaved,
                )
                if (!hasSuccessfulResult && viewModel.offlineTranslationMessage != null) {
                    Spacer(Modifier.height(12.dp))
                    val message = viewModel.offlineTranslationMessage.orEmpty()
                    if (isDirectBulosTranslationUnavailable(message)) {
                        TransientDirectBulosTranslationUnavailableNotice(
                            noticeId = viewModel.directBulosUnavailableNoticeId,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        TranslationFailureNotice(
                            presentation = translationFailurePresentation(message),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                OfflineSuggestionList(
                    suggestions = offlineSuggestions,
                    onSelect = viewModel::acceptOfflineSuggestion,
                    modifier = Modifier.padding(top = 8.dp),
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
                                        shareChooserTitle,
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
                            containerColor = SharedActiveButtonColor,
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
internal fun DirectBulosTranslationUnavailableNotice(
    modifier: Modifier = Modifier,
    showDictionaryIcon: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showDictionaryIcon) {
            Icon(
                painter = painterResource(R.drawable.ic_dictionary_book),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = ForestGreen,
            )
            Spacer(Modifier.height(12.dp))
        }
        Text(
            text = DIRECT_BULOS_TRANSLATION_UNAVAILABLE_TITLE,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = DIRECT_BULOS_TRANSLATION_UNAVAILABLE_DESCRIPTION,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF736E68),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun TransientDirectBulosTranslationUnavailableNotice(
    noticeId: Int,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(noticeId) {
        visible = true
        delay(3_000)
        visible = false
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(durationMillis = 180)),
        exit = fadeOut(tween(durationMillis = 180)),
        modifier = modifier,
    ) {
        DirectBulosTranslationUnavailableNotice(Modifier.fillMaxWidth())
    }
}

@Composable
internal fun TranslationFailureNotice(
    presentation: TranslationFailurePresentation,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = presentation.title,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = presentation.description,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF736E68),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TranslationOutputCard(
    label: String,
    translatedText: String?,
    placeholder: String,
    textColor: Color,
    minimumHeight: Dp,
    isSaved: Boolean,
    onToggleSaved: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minimumHeight)
            .shadow(
                elevation = 8.dp,
                shape = HomeCardShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.16f),
            ),
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = HomeSupportingCardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (!translatedText.isNullOrBlank()) {
                    IconButton(onClick = onToggleSaved, modifier = Modifier.size(48.dp)) {
                        Icon(
                            painter = painterResource(
                                if (isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline,
                            ),
                            contentDescription = if (isSaved) {
                                "Remove from saved translations"
                            } else {
                                "Save translation"
                            },
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = translatedText?.takeIf { it.isNotBlank() } ?: placeholder,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (translatedText.isNullOrBlank()) FontWeight.Normal else FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun HistoryScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val content = viewModel.content
    val historyItems = HistoryProvider.history.filter { it.isSaved }
    var historyFilter by rememberSaveable { mutableStateOf(HistoryFilter.ALL) }
    val visibleHistoryItems = historyItemsForFilter(historyItems, historyFilter)
    var selectedTimestamps by rememberSaveable { mutableStateOf(emptyList<Long>()) }
    var selectionMode by rememberSaveable { mutableStateOf(false) }
    var showHistoryMenu by remember { mutableStateOf(false) }
    val allItemsSelected = visibleHistoryItems.isNotEmpty() &&
        selectedTimestamps.size == visibleHistoryItems.size

    LaunchedEffect(historyItems.toList(), historyFilter) {
        selectedTimestamps = selectedTimestamps.intersect(
            visibleHistoryItems.mapTo(mutableSetOf()) { it.timestamp },
        ).toList()
        if (visibleHistoryItems.isEmpty()) selectionMode = false
    }

    BackHandler(enabled = selectionMode) {
        selectedTimestamps = emptyList()
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
                iconRes = R.drawable.ic_bookmark_filled,
                subtitle = stringResource(content.home.historySubtitleRes),
                onBack = onBack,
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
                            modifier = Modifier
                                .clip(LanguageSelectorCardShape)
                                .background(LanguageSelectorCardFill)
                                .padding(vertical = 6.dp),
                            shape = LanguageSelectorCardShape,
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            shadowElevation = 8.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, LanguageSelectorCardBorder),
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.clear_all)) },
                                onClick = {
                                    showHistoryMenu = false
                                    selectedTimestamps = emptyList()
                                    viewModel.clearSavedTranslations()
                                },
                                enabled = historyItems.isNotEmpty(),
                            )
                            if (historyFilter == HistoryFilter.ALL) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.select_multiple)) },
                                    onClick = {
                                        showHistoryMenu = false
                                        selectionMode = true
                                        selectedTimestamps = emptyList()
                                    },
                                    enabled = visibleHistoryItems.isNotEmpty(),
                                )
                            }
                        }
                    }
                },
                selectionMode = selectionMode,
                selectionContent = {
                    IconButton(
                        onClick = {
                            selectedTimestamps = emptyList()
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
                                emptyList()
                            } else {
                                visibleHistoryItems.map { it.timestamp }
                            }
                        },
                    ) {
                        Text(
                            stringResource(if (allItemsSelected) R.string.deselect_all else R.string.select_all),
                            color = WarmWhite,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    VerticalDivider(
                        modifier = Modifier.height(20.dp).padding(horizontal = 4.dp),
                        thickness = 1.dp,
                        color = WarmWhite.copy(alpha = 0.35f),
                    )
                    TextButton(
                        onClick = {
                            viewModel.addSavedTranslationsToFavorites(selectedTimestamps.toSet())
                            selectedTimestamps = emptyList()
                            selectionMode = false
                        },
                        enabled = selectedTimestamps.isNotEmpty(),
                    ) {
                        Text(
                            text = stringResource(
                                when (viewModel.uiLanguage) {
                                    UiLanguage.ENGLISH -> R.string.add_to_favorites
                                    UiLanguage.FILIPINO -> R.string.add_to_favorites_fil
                                    UiLanguage.BULOS -> R.string.add_to_favorites_bul
                                },
                            ),
                            color = WarmWhite,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.deleteSavedTranslations(selectedTimestamps.toSet())
                            selectedTimestamps = emptyList()
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
            HistoryFilterControl(
                selectedFilter = historyFilter,
                uiLanguage = viewModel.uiLanguage,
                onFilterSelected = { selectedFilter ->
                    historyFilter = selectedFilter
                    selectedTimestamps = emptyList()
                    selectionMode = false
                },
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
            )
            if (visibleHistoryItems.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (historyFilter == HistoryFilter.FAVORITES) {
                        HistoryFavoritesEmptyState(viewModel.uiLanguage)
                    } else {
                        HistorySavedEmptyState(content.noHistoryRes, viewModel.uiLanguage)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(visibleHistoryItems, key = { it.timestamp }) { item ->
                        HistoryCard(
                            item = item,
                            showSavedMetadata = true,
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
                            onToggleFavorite = {
                                viewModel.toggleSavedTranslationFavorite(item.timestamp)
                            },
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
fun RecentTranslationScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val content = viewModel.content
    val recentItems = recentHistoryItems(HistoryProvider.history)
    var selectedTimestamps by rememberSaveable { mutableStateOf(emptyList<Long>()) }
    var selectionMode by rememberSaveable { mutableStateOf(false) }
    var showRecentMenu by remember { mutableStateOf(false) }
    val allItemsSelected = recentItems.isNotEmpty() &&
        selectedTimestamps.size == recentItems.size

    LaunchedEffect(recentItems.toList()) {
        selectedTimestamps = selectedTimestamps.intersect(
            recentItems.mapTo(mutableSetOf()) { it.timestamp },
        ).toList()
        if (recentItems.isEmpty()) selectionMode = false
    }

    BackHandler(enabled = selectionMode) {
        selectedTimestamps = emptyList()
        selectionMode = false
    }

    Surface(Modifier.fillMaxSize(), color = Color(0xFFFFFBF4)) {
        OverlappingHeaderLayout(
            overlap = 40.dp,
            modifier = Modifier.fillMaxSize(),
            header = {
                SharedTopAppBar(
                    title = stringResource(content.home.recentDynamicTitleRes),
                    logoDescriptionRes = content.home.appLogoDescriptionRes,
                    iconRes = R.drawable.ic_saved_history_reference,
                    subtitle = stringResource(content.home.historySubtitleRes),
                    onBack = onBack,
                    trailingContent = {
                        Box {
                            IconButton(onClick = { showRecentMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.recent_translation_options),
                                    tint = WarmWhite,
                                )
                            }
                            DropdownMenu(
                                expanded = showRecentMenu,
                                onDismissRequest = { showRecentMenu = false },
                                modifier = Modifier
                                    .clip(LanguageSelectorCardShape)
                                    .background(LanguageSelectorCardFill)
                                    .padding(vertical = 6.dp),
                                shape = LanguageSelectorCardShape,
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                shadowElevation = 8.dp,
                                border = androidx.compose.foundation.BorderStroke(1.dp, LanguageSelectorCardBorder),
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.clear_all)) },
                                    onClick = {
                                        showRecentMenu = false
                                        selectedTimestamps = emptyList()
                                        viewModel.deleteRecentTranslations(recentItems.mapTo(mutableSetOf()) { it.timestamp })
                                    },
                                    enabled = recentItems.isNotEmpty(),
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.select_multiple)) },
                                    onClick = {
                                        showRecentMenu = false
                                        selectionMode = true
                                        selectedTimestamps = emptyList()
                                    },
                                    enabled = recentItems.isNotEmpty(),
                                )
                            }
                        }
                    },
                    selectionMode = selectionMode,
                    selectionContent = {
                        IconButton(
                            onClick = {
                                selectedTimestamps = emptyList()
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
                                    emptyList()
                                } else {
                                    recentItems.map { it.timestamp }
                                }
                            },
                        ) {
                            Text(
                                stringResource(if (allItemsSelected) R.string.deselect_all else R.string.select_all),
                                color = WarmWhite,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                        VerticalDivider(
                            modifier = Modifier.height(20.dp).padding(horizontal = 4.dp),
                            thickness = 1.dp,
                            color = WarmWhite.copy(alpha = 0.35f),
                        )
                        TextButton(
                            onClick = {
                                viewModel.addRecentTranslationsToSaved(selectedTimestamps.toSet())
                                selectedTimestamps = emptyList()
                                selectionMode = false
                            },
                            enabled = selectedTimestamps.isNotEmpty(),
                        ) {
                            Text(
                                text = stringResource(
                                    when (viewModel.uiLanguage) {
                                        UiLanguage.ENGLISH -> R.string.add_to_saved_translation
                                        UiLanguage.FILIPINO -> R.string.add_to_saved_translation_fil
                                        UiLanguage.BULOS -> R.string.add_to_saved_translation_bul
                                    },
                                ),
                                color = WarmWhite,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.deleteRecentTranslations(selectedTimestamps.toSet())
                                selectedTimestamps = emptyList()
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
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (recentItems.isEmpty()) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        RecentTranslationEmptyState(viewModel.uiLanguage)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(recentItems, key = { it.timestamp }) { item ->
                            HistoryCard(
                                item = item,
                                showFavorite = false,
                                showSaveAction = true,
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
                                    viewModel.deleteRecentTranslations(setOf(item.timestamp))
                                },
                                onToggleSaved = {
                                    viewModel.toggleRecentTranslationSaved(item.timestamp)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

internal enum class HistoryFilter { ALL, FAVORITES }

internal fun historyItemsForFilter(
    items: List<HistoryItem>,
    filter: HistoryFilter,
): List<HistoryItem> = when (filter) {
    HistoryFilter.ALL -> items
    HistoryFilter.FAVORITES -> items.filter { it.isFavorite }
}

@Composable
private fun HistoryFilterControl(
    selectedFilter: HistoryFilter,
    uiLanguage: UiLanguage,
    onFilterSelected: (HistoryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val allLabel = stringResource(
        when (uiLanguage) {
            UiLanguage.ENGLISH -> R.string.history_filter_all
            UiLanguage.FILIPINO -> R.string.history_filter_all_fil
            UiLanguage.BULOS -> R.string.history_filter_all_bul
        },
    )
    val favoritesLabel = stringResource(
        when (uiLanguage) {
            UiLanguage.ENGLISH -> R.string.history_filter_favorites
            UiLanguage.FILIPINO -> R.string.history_filter_favorites_fil
            UiLanguage.BULOS -> R.string.history_filter_favorites_bul
        },
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.16f),
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, LanguageSelectorCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(LanguageSelectorCardFill)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            listOf(
                HistoryFilter.ALL to allLabel,
                HistoryFilter.FAVORITES to favoritesLabel,
            ).forEach { (filter, label) ->
                val selected = selectedFilter == filter
                Surface(
                    onClick = { onFilterSelected(filter) },
                    modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) SharedActiveButtonColor else Color.Transparent,
                    contentColor = if (selected) WarmWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentTranslationEmptyState(uiLanguage: UiLanguage) {
    val titleRes = when (uiLanguage) {
        UiLanguage.ENGLISH -> R.string.recent_no_translation
        UiLanguage.FILIPINO -> R.string.recent_no_translation_fil
        UiLanguage.BULOS -> R.string.recent_no_translation_bul
    }
    val descriptionRes = when (uiLanguage) {
        UiLanguage.ENGLISH -> R.string.recent_no_translation_description
        UiLanguage.FILIPINO -> R.string.recent_no_translation_description_fil
        UiLanguage.BULOS -> R.string.recent_no_translation_description_bul
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HistorySavedEmptyState(@StringRes titleRes: Int, uiLanguage: UiLanguage) {
    val descriptionRes = when (uiLanguage) {
        UiLanguage.ENGLISH -> R.string.ui_no_history_description_eng
        UiLanguage.FILIPINO -> R.string.ui_no_history_description_fil
        UiLanguage.BULOS -> R.string.ui_no_history_description_bul
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HistoryFavoritesEmptyState(uiLanguage: UiLanguage) {
    val titleRes = when (uiLanguage) {
        UiLanguage.ENGLISH -> R.string.history_no_favorites
        UiLanguage.FILIPINO -> R.string.history_no_favorites_fil
        UiLanguage.BULOS -> R.string.history_no_favorites_bul
    }
    val descriptionRes = when (uiLanguage) {
        UiLanguage.ENGLISH -> R.string.history_no_favorites_description
        UiLanguage.FILIPINO -> R.string.history_no_favorites_description_fil
        UiLanguage.BULOS -> R.string.history_no_favorites_description_bul
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun HistoryCard(
    item: HistoryItem,
    selected: Boolean = false,
    selectionEnabled: Boolean = false,
    showFavorite: Boolean = true,
    showSaveAction: Boolean = false,
    showSavedMetadata: Boolean = false,
    onSelectionChange: (Boolean) -> Unit = {},
    onDelete: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onToggleSaved: () -> Unit = {},
) {
    val cardPadding = if (showSavedMetadata) {
        PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    } else {
        PaddingValues(16.dp)
    }
    val cardModifier = if (showSavedMetadata) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = HomeCardShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            )
    }
    Card(
        onClick = { if (selectionEnabled) onSelectionChange(!selected) },
        modifier = cardModifier,
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = HomeContentCream),
        border = if (showSavedMetadata) {
            androidx.compose.foundation.BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline)
        } else {
            HomeSupportingCardBorder
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(cardPadding)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(item.sourceLang, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.padding(horizontal = 4.dp).size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(item.targetLang, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                if (selectionEnabled) {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = onSelectionChange,
                    )
                }
                if (showSaveAction) {
                    IconButton(
                        onClick = onToggleSaved,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            painter = painterResource(
                                if (item.isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline,
                            ),
                            contentDescription = stringResource(
                                if (item.isSaved) R.string.remove_saved_translation else R.string.save_translation,
                            ),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else if (showFavorite) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(
                                if (item.isFavorite) R.string.unfavorite_translation else R.string.favorite_translation,
                            ),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(if (showSavedMetadata) 48.dp else 36.dp),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_translation),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            Spacer(Modifier.height(if (showSavedMetadata) 4.dp else 8.dp))
            Text(item.inputText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(item.translatedText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (showSavedMetadata) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = DateUtils.formatDateTime(
                        LocalContext.current,
                        item.timestamp,
                        DateUtils.FORMAT_SHOW_DATE or
                            DateUtils.FORMAT_SHOW_TIME or
                            DateUtils.FORMAT_ABBREV_RELATIVE,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
