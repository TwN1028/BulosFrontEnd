package com.example.bulosfrontend

import android.content.ClipData
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.example.bulosfrontend.ui.theme.ForestGreen
import com.example.bulosfrontend.ui.theme.InactiveButtonColor
import com.example.bulosfrontend.ui.theme.MutedText
import com.example.bulosfrontend.ui.theme.PrimaryGreen
import com.example.bulosfrontend.ui.theme.SharedActiveButtonColor
import com.example.bulosfrontend.ui.theme.SoftGreen
import com.example.bulosfrontend.ui.theme.WarmWhite
import com.example.bulosfrontend.ui.theme.WarmWhiteCard
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun TranslateVoiceScreen(
    viewModel: MainViewModel,
    onTranslate: () -> Unit,
    onBack: () -> Unit,
    tutorialStep: SttTutorialStep? = null,
    onTutorialStepChange: (SttTutorialStep) -> Unit = {},
) {
    val contentHorizontalPadding = 20.dp
    val content = viewModel.content
    val labels = content.speechResult
    val textTranslationLabels = content.textTranslation
    var recognizedText by rememberSaveable { mutableStateOf(TranslationState.sttTranscript) }
    var translateRequested by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(
        translateRequested,
        viewModel.isTranslating,
        TranslationState.translatedText,
    ) {
        if (
            translateRequested &&
            !viewModel.isTranslating &&
            TranslationState.translatedText.isNotBlank()
        ) {
            translateRequested = false
            onTranslate()
        }
    }

    BackHandler(onBack = onBack)

    Surface(Modifier.fillMaxSize(), color = HomeContentCream) {
        Column(Modifier.fillMaxSize()) {
            FeaturePatternHeader(
                title = "Transcribe",
                subtitle = "Review the text before translating",
                onBack = onBack,
                iconRes = R.drawable.ic_lucide_mic,
                headerBottomExtension = AppHeaderBottomExtension,
            )
            BoxWithConstraints(Modifier.weight(1f)) {
                val useScrollingLayout = maxHeight < 720.dp
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-58).dp)
                        .then(if (useScrollingLayout) Modifier.verticalScroll(scrollState) else Modifier)
                        .imePadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(24.dp))
                    TranslationLanguageBar(
                        swapLanguagesDescription = stringResource(labels.swapLanguagesDescriptionRes),
                        uiLanguage = viewModel.uiLanguage,
                        modifier = Modifier
                            .padding(horizontal = contentHorizontalPadding)
                            .height(50.dp),
                        useHomeCardStyle = true,
                    )
                    Spacer(Modifier.height(30.dp))
                    RecognizedTextCard(
                        label = stringResource(labels.recognizedTextLabelRes),
                        value = recognizedText,
                        placeholder = stringResource(labels.recognizedTextPlaceholderRes),
                        onValueChange = { recognizedText = it },
                        onFocusChanged = { focused ->
                            if (focused && tutorialStep == SttTutorialStep.TRANSCRIPT) {
                                onTutorialStepChange(SttTutorialStep.EDIT)
                            }
                        },
                        modifier = Modifier.padding(horizontal = contentHorizontalPadding),
                    )
                    if (tutorialStep == SttTutorialStep.TRANSCRIPT) {
                        Spacer(Modifier.height(12.dp))
                        SttTutorialHint(
                            step = 4,
                            title = "Check if your words were recognized correctly.",
                            body = "Review the text and fix any mistake before tapping Translate.",
                            bodyHighlight = "Translate",
                            modifier = Modifier.padding(horizontal = contentHorizontalPadding),
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    if (tutorialStep == SttTutorialStep.TRANSLATE) {
                        SttTutorialHint(
                            step = 6,
                            title = "Tap Translate to see the result.",
                            body = "The app will use its normal translation service and error handling.",
                            modifier = Modifier.padding(horizontal = contentHorizontalPadding),
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    TranslateLoadingButton(
                        onClick = {
                            val textToTranslate = recognizedText.trim()
                            if (tutorialStep != null) {
                                // The onboarding walkthrough intentionally presents a
                                // demo placeholder in the real result screen, so it
                                // must not wait on a live translation response.
                                TranslationState.textToTranslate = textToTranslate
                                onTranslate()
                            } else {
                                translateRequested = true
                                viewModel.translateVoiceText(textToTranslate)
                            }
                        },
                        translateText = stringResource(labels.translateRes),
                        translatingText = stringResource(textTranslationLabels.translatingRes),
                        enabled = recognizedText.isNotBlank() &&
                            (tutorialStep == null ||
                                tutorialStep == SttTutorialStep.TRANSCRIPT ||
                                tutorialStep == SttTutorialStep.EDIT ||
                                tutorialStep == SttTutorialStep.TRANSLATE),
                        isLoading = viewModel.isTranslating,
                        modifier = Modifier
                            .padding(horizontal = contentHorizontalPadding),
                    )
                    if (useScrollingLayout) {
                        Spacer(Modifier.height(24.dp))
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ResultScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onTranslateAgain: () -> Unit,
    onBackToHome: () -> Unit,
    tutorialComplete: Boolean = false,
    onTutorialComplete: () -> Unit = {},
) {
    val content = viewModel.content
    val labels = content.speechResult
    val textTranslationLabels = content.textTranslation
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val currentLocale = remember(configuration) {
        ConfigurationCompat.getLocales(configuration)[0] ?: Locale.ROOT
    }
    var showNotification by remember { mutableStateOf(false) }
    var tutorialTranslationSaved by rememberSaveable { mutableStateOf(false) }
    val originalText = TranslationState.textToTranslate
    val shareBody = stringResource(labels.shareBodyRes, originalText, TranslationState.translatedText)
    val shareChooserTitle = stringResource(labels.shareChooserTitleRes)
    val isSaved = HistoryProvider.history.any {
        it.isSaved && it.matchesTranslation(
            TranslationState.sourceLanguage,
            TranslationState.targetLanguage,
            originalText,
            TranslationState.translatedText,
        )
    }
    val resultTextForDisplay = if (tutorialComplete) null else TranslationState.translatedText
    val resultPlaceholder = if (tutorialComplete) {
        "This is a demo. Use the actual feature to see your translation."
    } else {
        stringResource(textTranslationLabels.outputPlaceholderRes)
    }

    val translationCardHeight = 200.dp
    val inputFieldHeight = 66.dp

    Surface(Modifier.fillMaxSize(), color = Color(0xFFFFFBF4)) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                FeaturePatternHeader(
                    title = stringResource(content.translateHeaderRes),
                    subtitle = stringResource(textTranslationLabels.subtitleRes),
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
                        swapLanguagesDescription = stringResource(labels.swapLanguagesDescriptionRes),
                        uiLanguage = viewModel.uiLanguage,
                        modifier = Modifier.offset(y = (-8).dp).height(50.dp),
                        useHomeCardStyle = true,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.offset(y = (-4).dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(textTranslationLabels.translateFromRes),
                            modifier = Modifier.weight(1f),
                            color = TranslationInputTextColor,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = MaterialTheme.typography.labelLarge.fontSize,
                            ),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.width(60.dp))
                        Text(
                            text = stringResource(textTranslationLabels.translateToLabelRes),
                            modifier = Modifier.weight(1f),
                            color = TranslationInputTextColor,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = MaterialTheme.typography.labelLarge.fontSize,
                            ),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(Modifier.height(21.dp))
                    TranslationInputCard(
                        label = TranslationState.sourceLanguage.uppercase(currentLocale),
                        value = originalText,
                        placeholder = stringResource(
                            textTranslationLabels.inputPlaceholderRes,
                            TranslationState.sourceLanguage,
                        ),
                        onValueChange = {},
                        textColor = TranslationInputTextColor,
                        editingEnabled = false,
                        minimumHeight = translationCardHeight,
                        textFieldHeight = inputFieldHeight,
                    )
                    Spacer(Modifier.height(8.dp))
                    TranslationDirectionRow(
                        source = TranslationState.sourceLanguage,
                        target = TranslationState.targetLanguage,
                    )
                    Spacer(Modifier.height(8.dp))
                    TranslationOutputCard(
                        label = TranslationState.targetLanguage.uppercase(currentLocale),
                        translatedText = resultTextForDisplay,
                        placeholder = resultPlaceholder,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        minimumHeight = translationCardHeight,
                        isSaved = if (tutorialComplete) tutorialTranslationSaved else isSaved,
                        onToggleSaved = {
                            if (tutorialComplete) tutorialTranslationSaved = !tutorialTranslationSaved
                            else viewModel.toggleCurrentTranslationSaved()
                        },
                        highlightSaveAction = tutorialComplete && !tutorialTranslationSaved,
                        showSaveAction = tutorialComplete,
                        saveHint = if (tutorialComplete && !tutorialTranslationSaved) {
                            "Tap here to save the translation."
                        } else {
                            null
                        },
                    )
                    if (tutorialComplete) {
                        Spacer(Modifier.height(12.dp))
                        SttTutorialHint(
                            step = 7,
                            title = "Save, copy, or share your translation.",
                            body = "Tap the highlighted bookmark to save this translation. Copy reuses the text, and Share sends it to another app.",
                        )
                    }
                    if (TranslationState.translatedText.isEmpty() && viewModel.offlineTranslationMessage != null) {
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
                        suggestions = viewModel.offlineTranslationSuggestions,
                        onSelect = viewModel::acceptOfflineSuggestion,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResultSecondaryAction(
                            text = stringResource(labels.copyRes),
                            icon = Icons.Default.ContentCopy,
                            onClick = {
                                if (TranslationState.translatedText.isNotEmpty()) {
                                    coroutineScope.launch {
                                        clipboard.setClipEntry(
                                            ClipEntry(
                                                ClipData.newPlainText(
                                                    "Translated text",
                                                    TranslationState.translatedText,
                                                ),
                                            ),
                                        )
                                        showNotification = true
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                        ResultSecondaryAction(
                            text = stringResource(labels.shareRes),
                            icon = Icons.Default.Share,
                            emphasized = true,
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareBody)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, shareChooserTitle))
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onTranslateAgain,
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
                        Text(stringResource(labels.translateAgainRes), fontWeight = FontWeight.Bold)
                    }
                    if (tutorialComplete) {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onTutorialComplete,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SharedActiveButtonColor,
                                contentColor = WarmWhite,
                            ),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Return to the tutorial to try other features.",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    } else {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onBackToHome,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .height(40.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarmWhiteCard,
                                contentColor = ForestGreen,
                            ),
                            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.22f)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        ) {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.back_to_home),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    // Keep the final action above the overlay navigation on button-navigation devices.
                    Spacer(Modifier.height(96.dp))
                }
            }
            TopToastNotification(showNotification, stringResource(content.copiedRes), PaddingValues(top = 88.dp))
        }
    }
}

@Composable
fun FeaturePatternHeader(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    iconRes: Int,
    headerBottomExtension: Dp = AppHeaderBottomExtension,
    showBackButton: Boolean = true,
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clipToBounds(),
    ) {
        Box(Modifier.matchParentSize().background(appHeaderGradientBrush()))
        Column {
        AdaptiveHeaderRow {
            if (showBackButton) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = WarmWhite)
                }
            }
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = WarmWhite.copy(alpha = 0.72f),
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        subtitle,
                        color = Color.White.copy(alpha = 0.88f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Clip,
                    )
                }
            }
            trailingContent()
            Spacer(Modifier.width(8.dp))
            ConnectionStatusIndicator()
        }
        Spacer(Modifier.height(headerBottomExtension))
        }
    }
}

@Composable
fun TranslationLanguageBar(
    swapLanguagesDescription: String,
    uiLanguage: UiLanguage,
    modifier: Modifier = Modifier,
    useHomeCardStyle: Boolean = true,
    onLanguageInteracted: (() -> Unit)? = null,
) {
    LaunchedEffect(TranslationState.sourceLanguage, TranslationState.targetLanguage) {
        TranslationState.ensureValidLanguagePair()
    }
    val targetOptions = TranslationLanguageRules.targetOptions(TranslationState.sourceLanguage)
    val cardShape = if (useHomeCardStyle) LanguageSelectorCardShape else RoundedCornerShape(24.dp)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .then(
                if (useHomeCardStyle) {
                    Modifier
                        .shadow(
                            elevation = 8.dp,
                            shape = cardShape,
                            clip = false,
                            ambientColor = Color.Black.copy(alpha = 0.12f),
                            spotColor = Color.Black.copy(alpha = 0.16f),
                        )
                        .clip(cardShape)
                } else {
                    Modifier
                },
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (useHomeCardStyle) Color.Transparent else MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            1.dp,
            if (useHomeCardStyle) LanguageSelectorCardBorder else Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.outlineVariant,
                    MaterialTheme.colorScheme.outlineVariant,
                ),
            ),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (useHomeCardStyle) 0.dp else 3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (useHomeCardStyle) {
                        Modifier.background(LanguageSelectorCardFill)
                    } else {
                        Modifier
                    },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompactLanguageMenu(
                selected = TranslationState.sourceLanguage,
                options = TranslationLanguageRules.supportedLanguages,
                onSelected = {
                    TranslationState.selectSourceLanguage(it, uiLanguage)
                    onLanguageInteracted?.invoke()
                },
                onInteraction = onLanguageInteracted,
                modifier = Modifier.weight(1f),
            )
            Row(
                modifier = Modifier.width(60.dp).height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                IconButton(
                    onClick = {
                        TranslationState.swapLanguages()
                        onLanguageInteracted?.invoke()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.SwapHoriz, swapLanguagesDescription, tint = MaterialTheme.colorScheme.secondary)
                }
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
            if (targetOptions.size == 1) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = targetOptions.single(),
                        color = TranslationInputTextColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                CompactLanguageMenu(
                    selected = TranslationState.targetLanguage,
                    options = targetOptions,
                onSelected = {
                    TranslationState.selectTargetLanguage(it)
                    onLanguageInteracted?.invoke()
                },
                onInteraction = onLanguageInteracted,
                modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CompactLanguageMenu(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    onInteraction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    BoxWithConstraints(modifier) {
        TextButton(
            onClick = {
                expanded = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                selected,
                color = TranslationInputTextColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                // Closing an opened selector without changing its value still
                // completes the tutorial's language-selection interaction.
                onInteraction?.invoke()
            },
            modifier = Modifier
                .width(maxWidth)
                .clip(LanguageSelectorCardShape)
                .background(LanguageSelectorCardFill)
                .padding(vertical = 6.dp),
            shape = LanguageSelectorCardShape,
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, LanguageSelectorCardBorder),
        ) {
            options
                .forEach { language ->
                    val isSelected = language.equals(selected, ignoreCase = true)
                    DropdownMenuItem(
                        modifier = Modifier
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                } else {
                                    Color.Transparent
                                },
                            ),
                        text = {
                            Text(
                                text = language,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        },
                        onClick = {
                            if (!isSelected) onSelected(language) else onInteraction?.invoke()
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
        }
    }
}

@Composable
private fun RecognizedTextCard(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    TranslationInputCard(
        label = label,
        value = value,
        placeholder = placeholder,
        onValueChange = onValueChange,
        textColor = Color(0xFF736E68),
        editingEnabled = true,
        onFocusChanged = onFocusChanged,
        modifier = modifier,
    )
}

@Composable
private fun TranslationTextCard(
    label: String,
    text: String,
    modifier: Modifier = Modifier,
    scrollableText: Boolean = false,
) {
    val transcriptionCardShape = RoundedCornerShape(16.dp)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = transcriptionCardShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.16f),
            ),
        shape = transcriptionCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(TranslationInputBorderWidth, TranslationInputBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Text(label, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = if (scrollableText) {
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                } else {
                    Modifier
                },
            )
        }
    }
}

@Composable
private fun LanguageDirectionPill(source: String, target: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Surface(shape = RoundedCornerShape(50), color = SoftGreen, contentColor = ForestGreen) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(source, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(7.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(7.dp))
                Text(target, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun TranslationDirectionRow(source: String, target: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = source,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(Modifier.width(12.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = target,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
internal fun ResultSecondaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    emphasized: Boolean = false,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String = text,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .height(52.dp)
            .shadow(
                elevation = 8.dp,
                shape = LanguageSelectorCardShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.16f),
            ),
        shape = LanguageSelectorCardShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (emphasized) SharedActiveButtonColor else Color(0xFFFFFEFA),
            contentColor = if (emphasized) Color(0xFFFFFBF4) else PrimaryGreen,
            disabledContainerColor = InactiveButtonColor,
            disabledContentColor = MutedText,
        ),
        border = if (emphasized) {
            null
        } else {
            BorderStroke(TranslationInputBorderWidth, TranslationInputBorderColor)
        },
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = if (isLoading) MutedText else if (emphasized) WarmWhite else PrimaryGreen,
            )
            Spacer(Modifier.width(10.dp))
            Text(loadingText, fontWeight = FontWeight.Bold)
        } else if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(7.dp))
            Text(text, fontWeight = FontWeight.SemiBold)
        } else {
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}
