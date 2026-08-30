package com.example.bulosfrontend

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.bulosfrontend.ui.theme.Cream
import com.example.bulosfrontend.ui.theme.DeepForestGreen
import com.example.bulosfrontend.ui.theme.ForestGreen
import com.example.bulosfrontend.ui.theme.HomeCardBorder
import com.example.bulosfrontend.ui.theme.LoudVoiceInner
import com.example.bulosfrontend.ui.theme.MutedText
import com.example.bulosfrontend.ui.theme.RecordingRed
import com.example.bulosfrontend.ui.theme.ResultSage
import com.example.bulosfrontend.ui.theme.Sand
import com.example.bulosfrontend.ui.theme.SoftGreen
import com.example.bulosfrontend.ui.theme.PrimaryGreen
import com.example.bulosfrontend.ui.theme.QuietVoiceInner
import com.example.bulosfrontend.ui.theme.VoiceReviewOrange
import com.example.bulosfrontend.ui.theme.WarmBrown
import com.example.bulosfrontend.ui.theme.WarmWhite
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

@Composable
fun TranslateVoiceScreen(viewModel: MainViewModel, onTranslate: () -> Unit, onBack: () -> Unit) {
    val contentHorizontalPadding = 20.dp
    val content = viewModel.content
    val labels = content.speechResult
    val context = LocalContext.current
    var recognizedText by rememberSaveable { mutableStateOf(TranslationState.textToTranslate) }
    var isEditing by rememberSaveable { mutableStateOf(true) }
    val hasRecording = TranslationState.recordedAudioPath != null

    fun startRecording() {
        recognizedText = ""
        viewModel.clearVoiceDraft()
        isEditing = true
        viewModel.startRecording()
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startRecording()
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            SpeechRecognitionHeader(
                title = stringResource(content.voiceHeaderRes),
                subtitle = stringResource(labels.speechSubtitleRes),
                onBack = onBack,
            )
            BoxWithConstraints(Modifier.weight(1f)) {
                val useScrollingLayout = maxHeight < 720.dp
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (useScrollingLayout) Modifier.verticalScroll(scrollState) else Modifier)
                        .imePadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(24.dp))
                    TranslationLanguageBar(
                        swapLanguagesDescription = stringResource(labels.swapLanguagesDescriptionRes),
                        modifier = Modifier.padding(horizontal = contentHorizontalPadding),
                    )
                    Spacer(Modifier.height(30.dp))
                    VoiceMicrophone(
                        isRecording = viewModel.isRecording,
                        hasRecording = hasRecording,
                        timerSeconds = viewModel.recordingTime,
                        status = stringResource(
                            when {
                                viewModel.isRecording -> labels.listeningStatusRes
                                hasRecording -> labels.reviewStatusRes
                                else -> labels.idleStatusRes
                            },
                        ),
                        description = stringResource(labels.microphoneDescriptionRes),
                        amplitudeProvider = viewModel::currentRecordingAmplitude,
                        onClick = {
                            if (viewModel.isRecording) {
                                viewModel.stopRecording()
                                isEditing = true
                            } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                startRecording()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                    )
                    if (viewModel.isRecording) {
                        Spacer(Modifier.height(14.dp))
                        TextButton(onClick = { viewModel.cancelRecording() }) {
                            Text(stringResource(content.cancelBtnRes), color = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(Modifier.height(24.dp))
                    } else {
                        Spacer(Modifier.height(28.dp))
                    }
                    RecognizedTextCard(
                        label = stringResource(labels.recognizedTextLabelRes, TranslationState.sourceLanguage),
                        value = recognizedText,
                        placeholder = stringResource(labels.recognizedTextPlaceholderRes),
                        isEditing = isEditing,
                        showEditControl = hasRecording && recognizedText.isNotBlank(),
                        editLabel = stringResource(if (isEditing) labels.doneEditingRes else labels.editRes),
                        isTranscribing = viewModel.isRecording,
                        transcribingText = stringResource(labels.transcribingRes),
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
                                viewModel.translateVoiceText(recognizedText.trim())
                                onTranslate()
                            },
                            enabled = recognizedText.isNotBlank() && !viewModel.isRecording,
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
                            Text(stringResource(labels.translateRes), fontWeight = FontWeight.Bold)
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
private fun SpeechRecognitionHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().background(PrimaryGreen).clipToBounds()) {
        GeometricGreenBackground(
            modifier = Modifier.matchParentSize(),
            cellSize = 52.dp,
            patternAlpha = 0.18f,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = WarmWhite)
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(R.drawable.ic_lucide_mic),
                contentDescription = null,
                tint = WarmWhite.copy(alpha = 0.72f),
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = WarmWhite,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = subtitle,
                    color = SoftGreen.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Clip,
                )
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

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                FeaturePatternHeader(
                    title = stringResource(content.resultHeaderRes),
                    onBack = onBack,
                    iconRes = R.drawable.ic_lucide_mic,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    TranslationTextCard(
                        label = "${TranslationState.sourceLanguage.uppercase(Locale.getDefault())} · ${stringResource(labels.originalRes)}",
                        text = originalText,
                        containerColor = MaterialTheme.colorScheme.surface,
                    )
                    LanguageDirectionPill(TranslationState.sourceLanguage, TranslationState.targetLanguage)
                    TranslationTextCard(
                        label = "${TranslationState.targetLanguage.uppercase(Locale.getDefault())} · ${stringResource(labels.translationRes)}",
                        text = translatedText,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResultSecondaryAction(
                            text = stringResource(labels.copyRes),
                            icon = Icons.Default.ContentCopy,
                            onClick = {
                                if (TranslationState.translatedText.isNotEmpty()) {
                                    clipboard.setText(AnnotatedString(TranslationState.translatedText))
                                    showNotification = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                        ResultSecondaryAction(
                            text = stringResource(labels.shareRes),
                            icon = Icons.Default.Share,
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        context.getString(labels.shareBodyRes, originalText, TranslationState.translatedText),
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, context.getString(labels.shareChooserTitleRes)))
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
    patterned: Boolean = false,
    iconRes: Int,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(PrimaryGreen)
            .clipToBounds(),
    ) {
        if (patterned) {
            GeometricGreenBackground(
                modifier = Modifier.matchParentSize(),
                cellSize = 52.dp,
                patternAlpha = 0.18f,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 4.dp, top = 6.dp, end = 16.dp, bottom = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = WarmWhite)
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
                        color = SoftGreen.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Clip,
                    )
                }
            }
        }
    }
}

@Composable
fun TranslationLanguageBar(swapLanguagesDescription: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().height(68.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
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
private fun CompactLanguageMenu(selected: String, onSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf(
        stringResource(R.string.lang_label_eng),
        stringResource(R.string.lang_label_fil),
        stringResource(R.string.lang_label_bul),
    )
    Box(modifier) {
        TextButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            languages.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language) },
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
                val normalizedAmplitude = normalizeMicrophoneAmplitude(amplitudeProvider())
                smoothedAmplitude = (smoothedAmplitude * 0.72f) + (normalizedAmplitude * 0.28f)
                val targetLevel = (
                    (smoothedAmplitude - VISUAL_NOISE_FLOOR) / (1f - VISUAL_NOISE_FLOOR)
                ).coerceIn(0f, 1f)
                val envelopeCoefficient = if (targetLevel > visualEnvelope) {
                    ENVELOPE_EXPANSION_COEFFICIENT
                } else {
                    ENVELOPE_CONTRACTION_COEFFICIENT
                }
                visualEnvelope = (
                    visualEnvelope + (targetLevel - visualEnvelope) * envelopeCoefficient
                ).coerceIn(0f, 1f)
                delay(75L)
            }
        }
        visualEnvelope = 0f
    }
    val responsiveLevel = visualEnvelope.pow(0.72f).coerceIn(0f, 1f)
    var radiusTargetLevel by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isRecording, responsiveLevel) {
        if (!isRecording) {
            radiusTargetLevel = 0f
        } else if (abs(responsiveLevel - radiusTargetLevel) >= RADIUS_CHANGE_THRESHOLD) {
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
                        AURA_EXPANSION_DURATION_MS
                    } else {
                        AURA_CONTRACTION_DURATION_MS
                    },
                    easing = AuraMovementEasing,
                ),
            )
        }
    }
    val buttonScale by animateFloatAsState(
        targetValue = if (isRecording) 1f + (responsiveLevel * 0.06f) else 1f,
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
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = buttonScale
                        scaleY = buttonScale
                    }
                    .shadow(4.dp, CircleShape)
                    .size(100.dp)
                    .background(buttonColor, CircleShape),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_mic),
                    contentDescription = description,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp),
                )
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

private const val VISUAL_NOISE_FLOOR = 0.08f
private const val ENVELOPE_EXPANSION_COEFFICIENT = 0.12f
private const val ENVELOPE_CONTRACTION_COEFFICIENT = 0.055f
private const val RADIUS_CHANGE_THRESHOLD = 0.008f
private const val AURA_EXPANSION_DURATION_MS = 170
private const val AURA_CONTRACTION_DURATION_MS = 320
private val AuraMovementEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

private fun normalizeMicrophoneAmplitude(rawAmplitude: Int): Float {
    val noiseFloor = 300f
    val cappedAmplitude = rawAmplitude.coerceIn(0, 32767).toFloat()
    if (cappedAmplitude <= noiseFloor) return 0f

    val normalized = (ln(cappedAmplitude) - ln(noiseFloor)) / (ln(32767f) - ln(noiseFloor))
    return normalized.coerceIn(0f, 1f)
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
    transcribingText: String,
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
            if (isTranscribing) {
                val dotTransition = rememberInfiniteTransition(label = "transcribingDots")
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(3) { index ->
                        val dotAlpha by dotTransition.animateFloat(
                            initialValue = 0.28f,
                            targetValue = 0.9f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 520, delayMillis = index * 130),
                                repeatMode = RepeatMode.Reverse,
                            ),
                            label = "transcribingDot$index",
                        )
                        Box(
                            Modifier
                                .padding(end = 5.dp)
                                .size(6.dp)
                                .graphicsLayer { alpha = dotAlpha }
                                .background(RecordingRed, CircleShape),
                        )
                    }
                    Spacer(Modifier.width(5.dp))
                    Text(transcribingText, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    enabled = isEditing,
                    placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        disabledTextColor = DeepForestGreen,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                    ),
                )
            }
        }
    }
}

@Composable
private fun TranslationTextCard(label: String, text: String, containerColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Text(label, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
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
private fun ResultSecondaryAction(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(7.dp))
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}
