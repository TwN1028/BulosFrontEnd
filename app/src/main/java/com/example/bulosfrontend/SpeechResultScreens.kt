package com.example.bulosfrontend

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import com.example.bulosfrontend.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TranslateVoiceScreen(viewModel: MainViewModel, onTranslate: () -> Unit, onBack: () -> Unit) {
    val contentHorizontalPadding = 20.dp
    val content = viewModel.content
    val labels = content.speechResult
    var recognizedText by rememberSaveable { mutableStateOf(value = TranslationState.textToTranslate) }
    var isEditing by rememberSaveable { mutableStateOf(value = true) }
    val hasRecording = (TranslationState.recordedAudioPath != null) || TranslationState.textToTranslate.isNotBlank()

    // Model Preparation logic
    LaunchedEffect(Unit) { viewModel.prepareModelForSourceLanguage() }
    LaunchedEffect(TranslationState.sourceLanguage) { viewModel.prepareModelForSourceLanguage() }

    // Sync State
    LaunchedEffect(TranslationState.textToTranslate) {
        recognizedText = TranslationState.textToTranslate
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            FeaturePatternHeader(
                title = "Transcribe",
                subtitle = "Review the text before translating",
                onBack = onBack,
                iconRes = R.drawable.ic_lucide_mic,
                headerBottomExtension = AppHeaderBottomExtension,
                isOnline = viewModel.isOnline,
                isServerReady = viewModel.isServerReady,
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
                        modifier = Modifier
                            .padding(horizontal = contentHorizontalPadding)
                            .height(50.dp),
                        useHomeCardStyle = true,
                    )
                    Spacer(Modifier.height(30.dp))
                    RecognizedTextCard(
                        label = stringResource(labels.recognizedTextLabelRes, stringResource(TranslationState.sourceLanguage.displayNameRes)),
                        value = recognizedText,
                        placeholder = stringResource(labels.recognizedTextPlaceholderRes),
                        isEditing = isEditing,
                        showEditControl = hasRecording && recognizedText.isNotBlank(),
                        editLabel = stringResource(if (isEditing) labels.doneEditingRes else labels.editRes),
                        isTranscribing = viewModel.isRecording,
                        asrStatus = viewModel.asrStatus,
                        onEditToggle = { isEditing = !isEditing },
                        onValueChange = { recognizedText = it },
                        modifier = Modifier.padding(horizontal = contentHorizontalPadding),
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = contentHorizontalPadding),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Button(
                            onClick = {
                                viewModel.translateVoiceText(recognizedText.trim(), onComplete = onTranslate)
                            },
                            enabled = recognizedText.isNotBlank() && !viewModel.isRecording && !viewModel.isTranslating,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestGreen,
                                contentColor = WarmWhite,
                                disabledContainerColor = Sand.copy(alpha = 0.50f),
                                disabledContentColor = MutedText,
                            ),
                            elevation = ButtonDefaults.buttonElevation(disabledElevation = 0.dp),
                        ) {
                            if (viewModel.isTranslating) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = WarmWhite)
                            } else {
                                Text(stringResource(labels.translateRes), fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = {
                                recognizedText = ""
                                isEditing = true
                                viewModel.clearVoiceDraft()
                            },
                            enabled = !viewModel.isRecording,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                disabledContainerColor = Sand.copy(alpha = 0.50f),
                                disabledContentColor = MutedText,
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                        ) {
                            Text(stringResource(labels.clearRes), fontWeight = FontWeight.SemiBold)
                        }
                    }
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
fun ResultScreen(viewModel: MainViewModel, onBack: () -> Unit, onTranslateAgain: () -> Unit) {
    val content = viewModel.content
    val labels = content.speechResult
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var showNotification by remember { mutableStateOf(false) }
    val originalText = TranslationState.textToTranslate
    val translatedText = TranslationState.translatedText.ifEmpty { stringResource(content.resultPlaceholderRes) }
    val shareBody = stringResource(labels.shareBodyRes, originalText, TranslationState.translatedText)
    val shareChooserTitle = stringResource(labels.shareChooserTitleRes)
    val sourceName = stringResource(TranslationState.sourceLanguage.displayNameRes)
    val targetName = stringResource(TranslationState.targetLanguage.displayNameRes)
    val isSaved = viewModel.historyItems.any {
        it.sourceLang == sourceName &&
            it.targetLang == targetName &&
            it.inputText == originalText &&
            it.translatedText == TranslationState.translatedText
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            OverlappingHeaderLayout(
                overlap = 31.dp,
                modifier = Modifier.fillMaxSize(),
                header = {
                FeaturePatternHeader(
                    title = stringResource(content.resultHeaderRes),
                    onBack = onBack,
                    iconRes = R.drawable.ic_lucide_mic,
                    isOnline = viewModel.isOnline,
                    isServerReady = viewModel.isServerReady,
                    trailingContent = {
                        IconButton(
                            onClick = viewModel::saveCurrentTranslation,
                            enabled = originalText.isNotEmpty() && TranslationState.translatedText.isNotEmpty(),
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Save translation",
                                tint = WarmWhite,
                            )
                        }
                    },
                )
                },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        TranslationTextCard(
                            label = "${sourceName.uppercase()} · ${stringResource(labels.originalRes)}",
                            text = originalText,
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f),
                            scrollableText = true,
                        )
                        LanguageDirectionPill(sourceName, targetName)
                        TranslationTextCard(
                            label = "${targetName.uppercase()} · ${stringResource(labels.translationRes)}",
                            text = translatedText,
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f),
                            scrollableText = true,
                            sourceInfo = TranslationState.translationSource,
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResultSecondaryAction(
                            text = stringResource(labels.copyRes),
                            icon = Icons.Default.ContentCopy,
                            onClick = {
                                if (TranslationState.translatedText.isNotEmpty()) {
                                    @Suppress("DEPRECATION")
                                    clipboard.setText(AnnotatedString(TranslationState.translatedText))
                                    showNotification = true
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
                    Button(
                        onClick = onTranslateAgain,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .widthIn(max = 480.dp)
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = WarmWhite),
                    ) {
                        Text(stringResource(labels.translateAgainRes), fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.navigationBarsPadding().height(4.dp))
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
    titleTopPadding: Dp = AppHeaderTitleTopPadding,
    showBackButton: Boolean = true,
    isOnline: Boolean? = null,
    isServerReady: Boolean = true,
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    val hasExtendedHeader = headerBottomExtension > 0.dp
    val adjustedBottomExtension = (
        headerBottomExtension - (titleTopPadding - 10.dp)
    ).coerceAtLeast(0.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .clipToBounds()
            .background(appHeaderGradientBrush()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .safeHeaderInsets()
                .padding(
                    start = 4.dp,
                    top = if (hasExtendedHeader) titleTopPadding else 6.dp,
                    end = 16.dp,
                    bottom = (if (hasExtendedHeader) 7.dp else 11.dp) + adjustedBottomExtension,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
                Text(title, color = WarmWhite, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        subtitle,
                        color = WarmWhite,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                        ),
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Clip,
                    )
                }
            }
            if (isOnline != null) {
                ConnectivityStatusPill(isOnline, isServerReady)
                Spacer(Modifier.width(8.dp))
            }
            trailingContent()
        }
    }
}

@Composable
fun TranslationLanguageBar(
    swapLanguagesDescription: String,
    modifier: Modifier = Modifier,
    useHomeCardStyle: Boolean = true,
) {
    val darkTheme = LocalBulosDarkTheme.current
    val cardShape = RoundedCornerShape(if (useHomeCardStyle) 16.dp else 24.dp)
    
    val glassFill = if (darkTheme) {
        Brush.verticalGradient(
            0.00f to Color(0xFF3A322A),
            1.00f to MaterialTheme.colorScheme.surface,
        )
    } else {
        Brush.verticalGradient(
            0.00f to Color(0xFFFFFEFD),
            0.42f to Color(0xFFFFFDF9),
            1.00f to Color(0xFFFFFBF4),
        )
    }
    
    val glassRim = if (darkTheme) {
        Brush.verticalGradient(
            0.00f to Color(0xFF51473B),
            1.00f to Color(0xFF2C251E),
        )
    } else {
        Brush.verticalGradient(
            0.00f to Color.White,
            0.48f to Color(0xFFFFFDF9),
            1.00f to Color(0xFFDBD9D2),
        )
    }
    
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
                            ambientColor = Color.Black.copy(alpha = if (darkTheme) 0.4f else 0.12f),
                            spotColor = Color.Black.copy(alpha = if (darkTheme) 0.5f else 0.16f),
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
            if (useHomeCardStyle) glassRim else Brush.linearGradient(
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
                        Modifier.background(glassFill)
                    } else {
                        Modifier
                    },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompactLanguageMenu(
                selected = TranslationState.sourceLanguage,
                onSelected = { TranslationState.sourceLanguage = it },
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
                        val source = TranslationState.sourceLanguage
                        TranslationState.sourceLanguage = TranslationState.targetLanguage
                        TranslationState.targetLanguage = source
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.SwapHoriz, swapLanguagesDescription, tint = MaterialTheme.colorScheme.secondary)
                }
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
            CompactLanguageMenu(
                selected = TranslationState.targetLanguage,
                onSelected = { TranslationState.targetLanguage = it },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CompactLanguageMenu(selected: UiLanguage, onSelected: (UiLanguage) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        TextButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(selected.displayNameRes), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            UiLanguage.entries.forEach { language ->
                DropdownMenuItem(
                    text = { Text(stringResource(language.displayNameRes)) },
                    onClick = {
                        onSelected(language)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun VoiceMicrophone(
    isRecording: Boolean,
    hasRecording: Boolean,
    isLoading: Boolean,
    timerSeconds: Long,
    status: String,
    description: String,
    amplitudeProvider: () -> Int,
    onClick: () -> Unit,
) {
    var visualEnvelope by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isRecording) {
        visualEnvelope = 0f
        if (isRecording) {
            var smoothedAmplitude = 0f
            while (isRecording) {
                val normalizedAmplitude = Design.normalizeAmplitude(amplitudeProvider())
                smoothedAmplitude = smoothedAmplitude * 0.72f + normalizedAmplitude * 0.28f
                val targetLevel = (
                    (smoothedAmplitude - Design.VISUAL_NOISE_FLOOR) / (1f - Design.VISUAL_NOISE_FLOOR)
                ).coerceIn(0f, 1f)
                val envelopeCoefficient = if (targetLevel > visualEnvelope) {
                    Design.ENVELOPE_EXPANSION_COEFFICIENT
                } else {
                    Design.ENVELOPE_CONTRACTION_COEFFICIENT
                }
                visualEnvelope = (
                    visualEnvelope + (targetLevel - visualEnvelope) * envelopeCoefficient
                ).coerceIn(0f, 1f)
                delay(75.milliseconds)
            }
        }
        visualEnvelope = 0f
    }
    val responsiveLevel = visualEnvelope.pow(0.72f).coerceIn(0f, 1f)
    var radiusTargetLevel by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isRecording, responsiveLevel) {
        if (!isRecording) {
            radiusTargetLevel = 0f
        } else if (abs(responsiveLevel - radiusTargetLevel) >= Design.RADIUS_CHANGE_THRESHOLD) {
            radiusTargetLevel = responsiveLevel
        }
    }
    val animatedRadiusLevel = remember { Animatable(0f) }
    LaunchedEffect(isRecording, radiusTargetLevel) {
        if (!isRecording) {
            animatedRadiusLevel.snapTo(0f)
        } else {
            animatedRadiusLevel.animateTo(
                targetValue = radiusTargetLevel,
                animationSpec = tween(
                    durationMillis = if (radiusTargetLevel > animatedRadiusLevel.value) {
                        Design.AURA_EXPANSION_DURATION_MS
                    } else {
                        Design.AURA_CONTRACTION_DURATION_MS
                    },
                    easing = Design.AURA_MOVEMENT_EASING,
                ),
            )
        }
    }
    val buttonScale by animateFloatAsState(
        targetValue = if (isRecording) 1f + responsiveLevel * 0.06f else 1f,
        animationSpec = tween(durationMillis = 90),
        label = "microphoneButtonScale",
    )
    val buttonColor = when {
        isRecording -> RecordingRed
        hasRecording -> VoiceReviewOrange
        else -> ForestGreen
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().height(230.dp),
            contentAlignment = Alignment.Center,
        ) {
            val maximumAuraCoreSize = minOf(maxWidth * 0.56f, 184.dp)
            val minimumAuraCoreSize = minOf(142.dp, maximumAuraCoreSize)
            val animatedAuraCoreSize = minimumAuraCoreSize +
                (maximumAuraCoreSize - minimumAuraCoreSize) * animatedRadiusLevel.value
            if (isRecording) {
                val auraColor = lerp(QuietVoiceInner, LoudVoiceInner, responsiveLevel)
                val auraAlpha = 0.38f + responsiveLevel * 0.34f
                Box(
                    Modifier
                        .size(animatedAuraCoreSize)
                        .blur(radius = 20.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                        .background(auraColor.copy(alpha = auraAlpha), CircleShape),
                )
            }
            IconButton(
                onClick = onClick,
                enabled = !isLoading,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = buttonScale
                        scaleY = buttonScale
                    }
                    .shadow(4.dp, CircleShape)
                    .size(100.dp)
                    .background(if (isLoading) Color.Gray else buttonColor, CircleShape),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_lucide_mic),
                        contentDescription = description,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        if (isRecording) {
            Text(
                String.format(Locale.US, "%02d:%02d / 01:00", timerSeconds / 60, timerSeconds % 60),
                color = RecordingRed,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(3.dp))
        }
        Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
}

@Composable
private fun RecognizedTextCard(
    label: String,
    value: String,
    placeholder: String,
    isEditing: Boolean,
    showEditControl: Boolean,
    editLabel: String,
    isTranscribing: Boolean,
    asrStatus: String,
    onEditToggle: () -> Unit,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().heightIn(min = 106.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                if (showEditControl) {
                    TextButton(onClick = onEditToggle, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(editLabel)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEditing && !isTranscribing,
                    placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        disabledTextColor = if (isTranscribing) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                    ),
                )
                
                if (isTranscribing) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val dotTransition = rememberInfiniteTransition(label = "transcribingDots")
                        repeat(3) { index ->
                            val dotAlpha by dotTransition.animateFloat(
                                initialValue = 0.28f,
                                targetValue = 0.9f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(520, delayMillis = index * 130),
                                    repeatMode = RepeatMode.Reverse,
                                ),
                                label = "transcribingDot$index",
                            )
                            Box(Modifier.padding(end = 4.dp).size(4.dp).graphicsLayer { alpha = dotAlpha }.background(RecordingRed, CircleShape))
                        }
                        Text(asrStatus, style = MaterialTheme.typography.labelSmall, color = RecordingRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun TranslationTextCard(
    label: String,
    text: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    scrollableText: Boolean = false,
    sourceInfo: String? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(0.5.dp, Color(0xFFE5DDD2).copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                sourceInfo?.let { info ->
                    Surface(
                        color = if (info == "Online") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = info.uppercase(),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = if (info == "Online") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
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
private fun ResultSecondaryAction(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (emphasized) PrimaryGreen else Color(0xFFFFFEFA),
            contentColor = if (emphasized) Color(0xFFFFFBF4) else PrimaryGreen,
        ),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(7.dp))
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}
