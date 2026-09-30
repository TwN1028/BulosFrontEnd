package com.example.bulosfrontend

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.bulosfrontend.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    dictionaryFabProgress: Float = 1f,
    onNavigate: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        listState.scrollToItem(0)
    }
    Crossfade(
        targetState = viewModel.uiLanguage,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "homeLanguageCrossfade",
    ) { language ->
        HomeScreenContent(
            content = DialogueProvider.getDialogue(language),
            selectedLanguage = viewModel.uiLanguage,
            onLanguageSelected = viewModel::selectHomeUiLanguage,
            onNavigate = onNavigate,
            listState = listState,
            dictionaryFabProgress = dictionaryFabProgress,
        )
    }
}

@Composable
private fun HomeScreenContent(
    content: DialogueContent,
    selectedLanguage: UiLanguage,
    onLanguageSelected: (UiLanguage) -> Unit,
    onNavigate: (String) -> Unit,
    listState: LazyListState,
    dictionaryFabProgress: Float,
) {
    val darkTheme = LocalBulosDarkTheme.current
    val labels = content.home
    val bodyBackgroundBrush = if (darkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.background,
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(HomeContentCream, HomeContentCream),
        )
    }
    val features = listOf(
        HomeFeature(labels.textTitleRes, labels.textSubtitleRes, painterResource(R.drawable.ic_lucide_languages), HomeTextTranslationCard, GoldenAccent, AppDestinations.TEXT),
        HomeFeature(labels.historyTitleRes, labels.historySubtitleRes, painterResource(R.drawable.ic_saved_history_reference), HomeSavedHistoryCard, MainText, AppDestinations.HISTORY),
        HomeFeature(labels.settingsTitleRes, labels.settingsSubtitleRes, painterResource(R.drawable.ic_lucide_settings), HomeSettingsCard, PrimaryGreen, AppDestinations.MORE),
        HomeFeature(content.help.cardTitleRes, content.help.cardSubtitleRes, painterResource(R.drawable.ic_help_reference), HomeHelpCard, PrimaryGreen, AppDestinations.HELP),
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(if (darkTheme) MaterialTheme.colorScheme.background else HomeContentCream),
    ) {
        val firstRowCardTop = 402.dp
        val creamBackgroundTop = firstRowCardTop + 24.dp
        HomeHeaderBackground(
            modifier = Modifier.fillMaxWidth().height(creamBackgroundTop),
            includeCreamTail = true,
            includePattern = true,
        )
        Box(Modifier.fillMaxSize().safeHeaderInsets()) {
            HomeIdentityHeader(
                title = stringResource(content.mainHeaderRes),
                badge = stringResource(labels.homeBadgeRes),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, top = 32.dp, end = 22.dp),
            )
            HomeVoiceHero(
                promptRes = labels.speechSubtitleRes,
                onClick = { onNavigate(AppDestinations.HOME_RECORDING) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 101.dp),
                useReferenceHomeGlow = true,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = creamBackgroundTop)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(bodyBackgroundBrush),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = firstRowCardTop)
                .clipToBounds(),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                overscrollEffect = null,
            ) {
                items(2) { rowIndex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        features.subList(rowIndex * 2, rowIndex * 2 + 2).forEach { feature ->
                            FeatureCard(
                                feature = feature,
                                onClick = { onNavigate(feature.route) },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }
                    }
                }
                item {
                    SupportedLanguagesCard(
                        titleRes = labels.supportedLanguagesRes,
                        descriptionRes = labels.supportedLanguagesDescriptionRes,
                        selectedLanguage = selectedLanguage,
                        onLanguageSelected = onLanguageSelected,
                    )
                }
                item {
                    HomeDynamicContentCard(
                        titleRes = labels.recentDynamicTitleRes,
                        emptyRes = labels.noRecentRes,
                        historyActionRes = labels.viewHistoryRes,
                        onHistoryClick = { onNavigate(AppDestinations.HISTORY) },
                    )
                }
                item {
                    Spacer(Modifier.height(90.dp).navigationBarsPadding())
                }
            }
        }
        HomeFabShortcut(
            iconRes = R.drawable.ic_dictionary_book,
            label = stringResource(labels.navDictionaryRes),
            onClick = { onNavigate(AppDestinations.DICTIONARY) },
            enabled = dictionaryFabProgress >= 0.99f,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 4.dp)
                .graphicsLayer {
                    alpha = dictionaryFabProgress
                    val animatedScale = 0.92f + 0.08f * dictionaryFabProgress
                    scaleX = animatedScale
                    scaleY = animatedScale
                },
        )
    }
}

@Composable
internal fun HomeFabShortcut(
    @androidx.annotation.DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            Surface(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = SoftGreen,
                contentColor = PrimaryGreen,
                shadowElevation = 5.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = label,
                        modifier = Modifier.size(29.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = label,
            color = PrimaryGreen,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun HomeRecordingScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onRecordingFinished: () -> Unit,
    autoStartRecording: Boolean = false,
) {
    val context = LocalContext.current
    val labels = viewModel.content.speechResult
    var actionHandled by remember { mutableStateOf(false) }
    var finishRequested by remember { mutableStateOf(false) }
    val recognitionFailed = viewModel.speechSessionFinished &&
        !viewModel.voiceInputReady &&
        !viewModel.isSpeechProcessing &&
        !viewModel.isRecording

    fun cancelOnce() {
        actionHandled = true
        finishRequested = false
        viewModel.cancelRecording()
        actionHandled = false
    }

    fun goBackOnce() {
        actionHandled = true
        viewModel.cancelRecording()
        onBack()
    }

    fun startRecordingOnce() {
        if (actionHandled || viewModel.isRecording) return
        viewModel.clearVoiceDraft()
        viewModel.startRecording()
    }

    fun completeRecordingOnce() {
        if (actionHandled) return
        actionHandled = true
        finishRequested = true
        if (viewModel.isRecording) {
            viewModel.stopRecording()
        } else if (!viewModel.voiceInputReady && !viewModel.isSpeechProcessing) {
            actionHandled = false
            finishRequested = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) startRecordingOnce() else cancelOnce()
    }

    fun requestPermissionOrStartRecording() {
        if (viewModel.isSpeechProcessing) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startRecordingOnce()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun toggleRecording() {
        if (viewModel.isRecording) viewModel.stopRecording() else requestPermissionOrStartRecording()
    }

    LaunchedEffect(autoStartRecording) {
        if (!autoStartRecording) return@LaunchedEffect
        requestPermissionOrStartRecording()
    }

    LaunchedEffect(viewModel.voiceInputReady, viewModel.speechSessionFinished, finishRequested) {
        if (!finishRequested) return@LaunchedEffect
        when {
            viewModel.voiceInputReady -> onRecordingFinished()
            viewModel.speechSessionFinished -> {
                actionHandled = false
                finishRequested = false
            }
        }
    }

    LaunchedEffect(viewModel.recordingTime) {
        if (viewModel.recordingTime >= 60L) {
            actionHandled = true
            finishRequested = true
        }
    }

    BackHandler(onBack = ::goBackOnce)

    Column(
        Modifier
            .fillMaxSize()
            .background(speechTranslationGradientBrush())
            .drawBehind {
                val center = Offset(size.width / 2f, size.height * 0.58f)
                val radius = size.width * 0.86f
                drawCircle(
                    brush = Brush.radialGradient(
                        0.00f to Color(0xFFEAEBD9).copy(alpha = 0.30f),
                        0.32f to Color(0xFFEAEBD9).copy(alpha = 0.23f),
                        0.58f to Color(0xFFEAEBD9).copy(alpha = 0.13f),
                        0.78f to Color(0xFFEAEBD9).copy(alpha = 0.055f),
                        0.94f to Color(0xFFEAEBD9).copy(alpha = 0.012f),
                        1.00f to Color.Transparent,
                        center = center,
                        radius = radius,
                    ),
                    center = center,
                    radius = radius,
                )
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .safeHeaderInsets()
                .padding(
                    start = 4.dp,
                    top = AppHeaderTitleTopPadding,
                    end = 16.dp,
                    bottom = 7.dp + (
                        AppHeaderBottomExtension - (AppHeaderTitleTopPadding - 10.dp)
                    ).coerceAtLeast(0.dp),
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = ::goBackOnce) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = WarmWhite,
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_lucide_mic),
                contentDescription = null,
                tint = WarmWhite.copy(alpha = 0.72f),
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(viewModel.content.voiceHeaderRes),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(viewModel.content.speechResult.speechSubtitleRes),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                    ),
                    fontWeight = FontWeight.Normal,
                )
            }
        }
        BoxWithConstraints(Modifier.weight(1f)) {
            val minimumHeroHeight = minOf(270.dp, maxHeight)
            val maximumHeroHeight = minOf(370.dp, maxHeight)
            val bottomHeroHeight = (maxHeight * 0.54f).coerceIn(
                minimumValue = minimumHeroHeight,
                maximumValue = maximumHeroHeight,
            )
            val availableContentHeight = maxHeight
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                        ),
                    )
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                TranslationLanguageBar(
                    swapLanguagesDescription = stringResource(
                        viewModel.content.speechResult.swapLanguagesDescriptionRes,
                    ),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .offset(y = (-50).dp)
                        .height(50.dp),
                    useHomeCardStyle = true,
                )

                BottomSpeechMicrophoneHero(
                    statusText = when {
                        viewModel.isSpeechProcessing -> ""
                        viewModel.isRecording -> stringResource(labels.listeningStatusRes)
                        viewModel.voiceInputReady || recognitionFailed -> ""
                        else -> stringResource(viewModel.content.home.speechSubtitleRes)
                    },
                    isProcessing = viewModel.isSpeechProcessing,
                    onClick = ::toggleRecording,
                    enabled = !viewModel.isSpeechProcessing &&
                        !recognitionFailed &&
                        !viewModel.voiceInputReady,
                    isRecording = viewModel.isRecording,
                    isStopped = !viewModel.isRecording && viewModel.voiceInputReady,
                    showCancelledIdleGlow = viewModel.wasRecordingCancelled,
                    recordingSeconds = viewModel.recordingTime.takeIf { viewModel.isRecording },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(bottomHeroHeight),
                )

                val liveTranscript = TranslationState.textToTranslate
                    .takeIf { viewModel.supportsLiveSpeechRecognition && it.isNotBlank() }
                if (liveTranscript != null) {
                    val transcriptBottomPadding = minOf(
                        bottomHeroHeight + 112.dp,
                        (availableContentHeight - 112.dp).coerceAtLeast(0.dp),
                    )
                    LiveSpeechTranscript(
                        text = liveTranscript,
                        isRecording = viewModel.isRecording,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxSize()
                            .padding(
                                top = 18.dp,
                                bottom = transcriptBottomPadding,
                                start = 18.dp,
                                end = 18.dp,
                            ),
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = (bottomHeroHeight - 20.dp).coerceAtLeast(0.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = when {
                            recognitionFailed -> viewModel.speechRecognitionError
                                ?: stringResource(labels.noSpeechRecognizedRes)
                            viewModel.voiceInputReady -> "Speech captured. Tap Finish to continue."
                            else -> ""
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (recognitionFailed || viewModel.voiceInputReady) {
                        Spacer(Modifier.height(12.dp))
                    }
                    when {
                        viewModel.isRecording || viewModel.voiceInputReady -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                ResultSecondaryAction(
                                    text = stringResource(labels.cancelRecordingRes),
                                    onClick = ::cancelOnce,
                                    modifier = Modifier.weight(1f),
                                )
                                ResultSecondaryAction(
                                    text = stringResource(labels.doneRecordingRes),
                                    onClick = ::completeRecordingOnce,
                                    modifier = Modifier.weight(1f),
                                    emphasized = true,
                                )
                            }
                        }
                        recognitionFailed -> {
                            Button(
                                onClick = ::requestPermissionOrStartRecording,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4F7045),
                                    contentColor = Color(0xFFFFFBF4),
                                ),
                            ) {
                                Text(
                                    stringResource(labels.tryAgainRes),
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            TextButton(onClick = ::goBackOnce) {
                                Text(
                                    stringResource(viewModel.content.goBackRes),
                                    color = PrimaryGreen,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun unchangedWordPrefixLength(previous: String, current: String): Int {
    val limit = minOf(previous.length, current.length)
    var commonLength = 0
    while (commonLength < limit && previous[commonLength] == current[commonLength]) {
        commonLength++
    }
    if (commonLength == previous.length &&
        (commonLength == current.length || current.getOrNull(commonLength)?.isWhitespace() == true)
    ) {
        return commonLength
    }
    while (commonLength > 0 && !current[commonLength - 1].isWhitespace()) {
        commonLength--
    }
    return commonLength
}

@Composable
private fun LiveSpeechTranscript(
    text: String,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val revealProgress = remember { Animatable(1f) }
    var previousText by remember { mutableStateOf("") }
    var animatedStart by remember { mutableStateOf(0) }
    var followNewestText by remember { mutableStateOf(true) }

    LaunchedEffect(text) {
        animatedStart = unchangedWordPrefixLength(previousText, text)
        previousText = text
        revealProgress.snapTo(0f)
        revealProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        )
    }
    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            followNewestText = false
        } else {
            followNewestText = scrollState.maxValue - scrollState.value <= 8
        }
    }
    LaunchedEffect(text, scrollState.maxValue, followNewestText) {
        if (followNewestText) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val safeAnimatedStart = animatedStart.coerceIn(0, text.length)
    val animatedText = buildAnnotatedString {
        append(text.substring(0, safeAnimatedStart))
        pushStyle(
            SpanStyle(
                color = Color.White.copy(alpha = 0.28f + revealProgress.value * 0.72f),
                baselineShift = BaselineShift((revealProgress.value - 1f) * 0.10f),
            ),
        )
        append(text.substring(safeAnimatedStart))
        pop()
    }
    val transcriptMask = if (isRecording) {
        Modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        0.00f to Color.Transparent,
                        0.14f to Color.Black.copy(alpha = 0.62f),
                        0.24f to Color.Black,
                        1.00f to Color.Black,
                    ),
                    blendMode = BlendMode.DstIn,
                )
            }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .then(transcriptMask),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            text = animatedText,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 16.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp,
                lineHeight = 25.sp,
            ),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BottomSpeechMicrophoneHero(
    statusText: String,
    isProcessing: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    isRecording: Boolean,
    isStopped: Boolean,
    showCancelledIdleGlow: Boolean,
    recordingSeconds: Long? = null,
    modifier: Modifier = Modifier,
) {
    val microphoneButtonSize = 104.dp
    val microphoneBottomPadding = 24.dp
    val transition = rememberInfiniteTransition(label = "bottomSpeechGlow")
    val recordingPulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bottomSpeechGlowPulse",
    )
    val processingRingProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "bottomSpeechProcessingRings",
    )
    val pulse = if (isRecording) recordingPulse else 0f
    val creamPreparationPulse = if (isProcessing) recordingPulse else 0f
    val preparationGlowAlpha = if (isProcessing) {
        0.78f + recordingPulse * 0.22f
    } else {
        1f
    }
    val idleToCancelled by animateFloatAsState(
        targetValue = if (showCancelledIdleGlow && !isRecording) 1f else 0f,
        animationSpec = tween(SPEECH_IDLE_GLOW_TRANSITION_MS, easing = FastOutSlowInEasing),
        label = "bottomSpeechCancelledTransition",
    )

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        Canvas(Modifier.fillMaxSize()) {
            val microphoneCenter = Offset(
                x = size.width / 2f,
                y = size.height - microphoneBottomPadding.toPx() - microphoneButtonSize.toPx() / 2f,
            )
            val atmosphereRadius = size.width * (0.82f + pulse * 0.01f)
            val softCreamRadius = size.width * (
                0.68f + pulse * 0.01f + creamPreparationPulse * 0.008f
            )
            val glowRadius = size.width * (
                0.46f + pulse * 0.008f + creamPreparationPulse * 0.006f
            )
            val glowCore = lerp(SpeechGlowConcentrated, RecordingRed, idleToCancelled)
            val glowOuter = lerp(SpeechGlowOuter, OuterVoiceRing, idleToCancelled)

            drawCircle(
                brush = Brush.radialGradient(
                    0.00f to Color(0xFFC8CFB3).copy(alpha = 0.06f),
                    0.16f to Color(0xFFABB99A).copy(alpha = 0.12f),
                    0.32f to Color(0xFF879B70).copy(alpha = 0.29f),
                    0.50f to Color(0xFF5E7A4A).copy(alpha = 0.48f),
                    0.68f to Color(0xFF5E7A4A).copy(alpha = 0.44f),
                    0.84f to Color(0xFF5E7A4A).copy(alpha = 0.25f),
                    0.95f to Color(0xFF5E7A4A).copy(alpha = 0.08f),
                    1.00f to Color.Transparent,
                    center = microphoneCenter,
                    radius = atmosphereRadius,
                ),
                radius = atmosphereRadius,
                center = microphoneCenter,
            )

            drawCircle(
                brush = Brush.radialGradient(
                    0.00f to glowOuter.copy(alpha = 0.54f * preparationGlowAlpha),
                    0.20f to glowOuter.copy(alpha = 0.47f * preparationGlowAlpha),
                    0.44f to glowOuter.copy(alpha = 0.33f * preparationGlowAlpha),
                    0.66f to glowOuter.copy(alpha = 0.17f * preparationGlowAlpha),
                    0.82f to glowOuter.copy(alpha = 0.07f * preparationGlowAlpha),
                    0.93f to glowOuter.copy(alpha = 0.018f * preparationGlowAlpha),
                    0.98f to glowOuter.copy(alpha = 0.003f * preparationGlowAlpha),
                    1.00f to Color.Transparent,
                    center = microphoneCenter,
                    radius = softCreamRadius,
                ),
                radius = softCreamRadius,
                center = microphoneCenter,
            )

            drawCircle(
                brush = Brush.radialGradient(
                    0.00f to glowCore.copy(alpha = 0.62f * preparationGlowAlpha),
                    0.10f to glowCore.copy(alpha = 0.57f * preparationGlowAlpha),
                    0.24f to glowCore.copy(alpha = 0.48f * preparationGlowAlpha),
                    0.40f to glowCore.copy(alpha = 0.36f * preparationGlowAlpha),
                    0.58f to glowOuter.copy(alpha = 0.24f * preparationGlowAlpha),
                    0.68f to glowOuter.copy(alpha = 0.17f * preparationGlowAlpha),
                    0.76f to glowOuter.copy(alpha = 0.11f * preparationGlowAlpha),
                    0.88f to glowOuter.copy(alpha = 0.052f * preparationGlowAlpha),
                    0.96f to glowOuter.copy(alpha = 0.012f * preparationGlowAlpha),
                    1.00f to Color.Transparent,
                    center = microphoneCenter,
                    radius = glowRadius,
                ),
                radius = glowRadius,
                center = microphoneCenter,
            )

            val outerRingRadius = minOf(
                size.width * (0.58f + pulse * 0.006f),
                microphoneCenter.y - 60.dp.toPx(),
            )
            val innerRingRadius = outerRingRadius * 0.73f
            if (isProcessing) {
                val startingRadius = microphoneButtonSize.toPx() * 0.58f
                listOf(
                    processingRingProgress,
                    (processingRingProgress + 0.5f) % 1f,
                ).forEach { phase ->
                    val easedPhase = FastOutSlowInEasing.transform(phase)
                    val radius = startingRadius + (outerRingRadius - startingRadius) * easedPhase
                    val fade = if (phase < 0.14f) {
                        phase / 0.14f
                    } else {
                        ((1f - phase) / 0.86f).pow(1.35f)
                    }
                    drawCircle(
                        color = Color(0xFFFFFBF4).copy(alpha = 0.20f * fade),
                        radius = radius,
                        center = microphoneCenter,
                        style = Stroke(width = 0.85.dp.toPx()),
                    )
                }
            } else {
                drawCircle(
                    color = Color(0xFFFFFBF4).copy(alpha = 0.20f - pulse * 0.015f),
                    radius = innerRingRadius,
                    center = microphoneCenter,
                    style = Stroke(width = 0.9.dp.toPx()),
                )
                drawCircle(
                    color = Color(0xFFFFFBF4).copy(alpha = 0.09f - pulse * 0.007f),
                    radius = outerRingRadius,
                    center = microphoneCenter,
                    style = Stroke(width = 0.75.dp.toPx()),
                )
            }
        }

        if (isProcessing || statusText.isNotEmpty()) {
            val isIdlePrompt = !isProcessing && !isRecording && recordingSeconds == null
            Column(
                modifier = if (isIdlePrompt) {
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = microphoneBottomPadding + microphoneButtonSize + 16.dp,
                        )
                } else {
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 76.dp, start = 16.dp, end = 16.dp)
                },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = Color.White.copy(alpha = 0.92f),
                        trackColor = Color.White.copy(alpha = 0.18f),
                        strokeWidth = 2.5.dp,
                    )
                } else {
                    Text(
                        text = statusText,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }
                if (recordingSeconds != null) {
                    val boundedSeconds = recordingSeconds.coerceIn(0L, 60L)
                    val remainingSeconds = 60L - boundedSeconds
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${boundedSeconds / 60}:${(boundedSeconds % 60).toString().padStart(2, '0')} / 1:00",
                        modifier = Modifier.width(108.dp),
                        color = Color.White.copy(alpha = 0.92f),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = when (remainingSeconds) {
                            1L -> "1 second left"
                            in 2L..10L -> "$remainingSeconds seconds left"
                            else -> ""
                        },
                        color = Color.White.copy(alpha = 0.68f),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Surface(
            onClick = onClick,
            enabled = enabled && !isStopped,
            modifier = Modifier
                .padding(bottom = microphoneBottomPadding)
                .size(microphoneButtonSize)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x331E3F20),
                    spotColor = Color(0x291E3F20),
            ),
            shape = CircleShape,
            color = Color(0xFFFFFBF4),
            border = BorderStroke(2.dp, SpeechGlowOuter.copy(alpha = 0.72f)),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_mic),
                    contentDescription = stringResource(R.string.feature_speech_subtitle),
                    tint = if (isStopped) WarmBrown else PrimaryGreen,
                    modifier = Modifier.size(38.dp),
                )
            }
        }
    }
}

private val CreamAura = Color(0xFFFFF3DA)
private val SpeechGlowConcentrated = Color(0xFFF3E7AC)
private val SpeechGlowOuter = Color(0xFFF7F3DE)
private val HomeReferenceGlowCore = Color(0xFFE8E1C7)
private val HomeReferenceGlowWarm = Color(0xFFE8E1C7)
private val HomeReferenceGlowMiddle = Color(0xFFE8E1C7)
private val HomeReferenceGlowSage = Color(0xFFC7D0AA)
private val HomeReferenceGlowOuter = Color(0xFF879B70)
private val MicrophoneGlowCore = Color(0xFFFFF3B0)
private val MicrophoneGlowMid = Color(0xFFDFE7A8)
private val MicrophoneGlowEdge = Color(0xFFDCE9D2)
internal const val SPEECH_AURA_SCREEN_WIDTH_FRACTION = 0.94f
internal const val SPEECH_AURA_MAX_SCREEN_WIDTH_FRACTION = 0.98f
private const val HOME_VISUAL_NOISE_FLOOR = 0.08f
private const val HOME_ENVELOPE_EXPANSION_COEFFICIENT = 0.12f
private const val HOME_ENVELOPE_CONTRACTION_COEFFICIENT = 0.055f
private const val HOME_RADIUS_CHANGE_THRESHOLD = 0.008f
private const val HOME_AURA_EXPANSION_DURATION_MS = 170
private const val HOME_AURA_CONTRACTION_DURATION_MS = 320
internal const val SPEECH_IDLE_GLOW_TRANSITION_MS = 300
private val HomeAuraMovementEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

private fun normalizeHomeMicrophoneAmplitude(rawAmplitude: Int): Float {
    val noiseFloor = 300f
    val cappedAmplitude = rawAmplitude.coerceIn(0, 32767).toFloat()
    if (cappedAmplitude <= noiseFloor) return 0f

    return (
        (ln(cappedAmplitude) - ln(noiseFloor)) / (ln(32767f) - ln(noiseFloor))
    ).coerceIn(0f, 1f)
}

@Composable
private fun HomeVoiceHero(
    @androidx.annotation.StringRes promptRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isRecording: Boolean = false,
    isStopped: Boolean = false,
    amplitudeProvider: () -> Int = { 0 },
    showPrompt: Boolean = true,
    useReferenceHomeGlow: Boolean = false,
    useSpeechRecordingAura: Boolean = false,
    showCancelledIdleGlow: Boolean = false,
    speechAuraDiameter: Dp = 280.dp,
    speechAuraMaxDiameter: Dp = 280.dp,
) {
    val homeReferenceAuraDiameter = (
        LocalConfiguration.current.screenWidthDp.dp * 0.90f
    ).coerceIn(300.dp, 380.dp)
    var visualEnvelope by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isRecording) {
        visualEnvelope = 0f
        if (isRecording) {
            var smoothedAmplitude = 0f
            while (isRecording) {
                val normalizedAmplitude = normalizeHomeMicrophoneAmplitude(amplitudeProvider())
                smoothedAmplitude = smoothedAmplitude * 0.72f + normalizedAmplitude * 0.28f
                val targetLevel = (
                    (smoothedAmplitude - HOME_VISUAL_NOISE_FLOOR) / (1f - HOME_VISUAL_NOISE_FLOOR)
                ).coerceIn(0f, 1f)
                val coefficient = if (targetLevel > visualEnvelope) {
                    HOME_ENVELOPE_EXPANSION_COEFFICIENT
                } else {
                    HOME_ENVELOPE_CONTRACTION_COEFFICIENT
                }
                visualEnvelope = (
                    visualEnvelope + (targetLevel - visualEnvelope) * coefficient
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
        } else if (abs(responsiveLevel - radiusTargetLevel) >= HOME_RADIUS_CHANGE_THRESHOLD) {
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
                        HOME_AURA_EXPANSION_DURATION_MS
                    } else {
                        HOME_AURA_CONTRACTION_DURATION_MS
                    },
                    easing = HomeAuraMovementEasing,
                ),
            )
        }
    }
    val buttonScale by animateFloatAsState(
        targetValue = if (isRecording) 1f + responsiveLevel * 0.06f else 1f,
        animationSpec = tween(durationMillis = 90),
        label = "homeMicrophoneButtonScale",
    )
    val cancelledIdleTransition by animateFloatAsState(
        targetValue = if (useSpeechRecordingAura && showCancelledIdleGlow && !isRecording) 1f else 0f,
        animationSpec = tween(
            durationMillis = SPEECH_IDLE_GLOW_TRANSITION_MS,
            easing = FastOutSlowInEasing,
        ),
        label = "speechCancelledIdleGlowTransition",
    )
    val ringMotion = rememberInfiniteTransition(label = "homeMicrophoneRingMotion")
    val recordingPulse by ringMotion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "speechRecordingGreenPulse",
    )
    val outerHighlightAngle by ringMotion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "outerRingHighlightAngle",
    )
    val middleHighlightAngle by ringMotion.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 17_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "middleRingHighlightAngle",
    )

    Box(modifier = modifier.size(254.dp), contentAlignment = Alignment.Center) {
        val showReferenceHomeGlow = useReferenceHomeGlow &&
            !isRecording &&
            !isStopped &&
            !showCancelledIdleGlow
        val activePulse = if (useSpeechRecordingAura && isRecording) recordingPulse else 0f
        val auraExpansion = animatedRadiusLevel.value * 38f + activePulse * 28f
        val auraDiameter = when {
            showReferenceHomeGlow -> homeReferenceAuraDiameter
            useSpeechRecordingAura -> speechAuraDiameter
            else -> 280.dp
        }
        val animatedAuraDiameter = if (useSpeechRecordingAura) {
            minOf(auraDiameter + auraExpansion.dp, speechAuraMaxDiameter)
        } else {
            auraDiameter + auraExpansion.dp
        }
        Canvas(
            if (showReferenceHomeGlow) {
                Modifier.requiredSize(animatedAuraDiameter)
            } else {
                Modifier.size(animatedAuraDiameter)
            },
        ) {
            if (showReferenceHomeGlow) {
                val auraRadius = size.minDimension / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        0.00f to HomeReferenceGlowSage.copy(alpha = 0.18f),
                        0.50f to HomeReferenceGlowSage.copy(alpha = 0.12f),
                        0.78f to HomeReferenceGlowOuter.copy(alpha = 0.06f),
                        0.94f to HomeReferenceGlowOuter.copy(alpha = 0.02f),
                        1.00f to Color.Transparent,
                        center = center,
                        radius = auraRadius,
                    ),
                    radius = auraRadius,
                    center = center,
                )
                val creamBloomRadius = auraRadius * 0.82f
                drawCircle(
                    brush = Brush.radialGradient(
                        0.00f to HomeReferenceGlowWarm.copy(alpha = 0.66f),
                        0.28f to HomeReferenceGlowWarm.copy(alpha = 0.58f),
                        0.58f to HomeReferenceGlowMiddle.copy(alpha = 0.32f),
                        0.82f to HomeReferenceGlowMiddle.copy(alpha = 0.12f),
                        1.00f to Color.Transparent,
                        center = center,
                        radius = creamBloomRadius,
                    ),
                    radius = creamBloomRadius,
                    center = center,
                )
                val brightCoreRadius = auraRadius * 0.60f
                drawCircle(
                    brush = Brush.radialGradient(
                        0.00f to HomeReferenceGlowCore.copy(alpha = 0.98f),
                        0.34f to HomeReferenceGlowCore.copy(alpha = 0.90f),
                        0.65f to HomeReferenceGlowWarm.copy(alpha = 0.52f),
                        0.86f to HomeReferenceGlowWarm.copy(alpha = 0.15f),
                        1.00f to Color.Transparent,
                        center = center,
                        radius = brightCoreRadius,
                    ),
                    radius = brightCoreRadius,
                    center = center,
                )
            } else {
                drawCircle(
                    brush = if (useSpeechRecordingAura) {
                    val auraRadius = size.minDimension / 2f
                    val glowCore = lerp(MicrophoneGlowCore, RecordingRed, cancelledIdleTransition)
                    val glowMid = lerp(MicrophoneGlowMid, InnerVoiceRing, cancelledIdleTransition)
                    val glowEdge = lerp(MicrophoneGlowEdge, OuterVoiceRing, cancelledIdleTransition)
                    Brush.radialGradient(
                        0.00f to glowCore.copy(alpha = 0.94f),
                        0.20f to glowCore.copy(alpha = 0.88f),
                        0.48f to glowMid.copy(alpha = 0.72f),
                        0.74f to glowEdge.copy(alpha = 0.52f),
                        0.90f to glowEdge.copy(alpha = 0.22f),
                        1.00f to Color.Transparent,
                        center = center,
                        radius = auraRadius,
                    )
                    } else {
                        Brush.radialGradient(
                            0.00f to CreamAura.copy(alpha = 0.80f),
                            0.45f to CreamAura.copy(alpha = 0.56f),
                            0.75f to CreamAura.copy(alpha = 0.30f),
                            1.00f to Color.Transparent,
                            center = center,
                            radius = 140.dp.toPx(),
                        )
                    },
                    radius = size.minDimension / 2f,
                    center = center,
                )
            }
        }
        if (!showReferenceHomeGlow) Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRingRadius = 127.dp.toPx()
            val middleRingRadius = 96.dp.toPx()
            val innerFillRadius = 56.dp.toPx()
            val ringStrokeWidth = 0.8.dp.toPx()

            drawCircle(CreamAura.copy(alpha = 0.04f), outerRingRadius, center, style = Fill)
            drawCircle(CreamAura.copy(alpha = 0.05f), middleRingRadius, center, style = Fill)
            drawCircle(CreamAura.copy(alpha = 0.06f), innerFillRadius, center, style = Fill)

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to CreamAura.copy(alpha = 0.08f),
                        0.45f to CreamAura.copy(alpha = 0.04f),
                        0.75f to CreamAura.copy(alpha = 0.02f),
                        1.00f to Color.Transparent,
                    ),
                    center = center,
                    radius = outerRingRadius,
                ),
                radius = outerRingRadius,
                center = center,
            )

            drawCircle(CreamAura.copy(alpha = 0.18f), outerRingRadius, center, style = Stroke(ringStrokeWidth))
            drawCircle(CreamAura.copy(alpha = 0.18f), middleRingRadius, center, style = Stroke(ringStrokeWidth))

            rotate(degrees = outerHighlightAngle, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        0.00f to Color.Transparent,
                        0.70f to Color.Transparent,
                        0.84f to CreamAura.copy(alpha = 0.04f),
                        0.91f to CreamAura.copy(alpha = 0.12f),
                        1.00f to Color.Transparent,
                        center = center,
                    ),
                    radius = outerRingRadius,
                    center = center,
                    style = Stroke(1.1.dp.toPx()),
                )
            }
            rotate(degrees = middleHighlightAngle, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        0.00f to Color.Transparent,
                        0.68f to Color.Transparent,
                        0.82f to CreamAura.copy(alpha = 0.035f),
                        0.90f to CreamAura.copy(alpha = 0.10f),
                        1.00f to Color.Transparent,
                        center = center,
                    ),
                    radius = middleRingRadius,
                    center = center,
                    style = Stroke(1.dp.toPx()),
                )
            }

            fun pointOnRing(radius: Float, angleDegrees: Float): Offset {
                val radians = Math.toRadians(angleDegrees.toDouble())
                return Offset(
                    x = center.x + radius * cos(radians).toFloat(),
                    y = center.y + radius * sin(radians).toFloat(),
                )
            }

            val outerHighlight = pointOnRing(outerRingRadius, outerHighlightAngle)
            val middleHighlight = pointOnRing(middleRingRadius, middleHighlightAngle)
            drawCircle(CreamAura.copy(alpha = 0.06f), 6.dp.toPx(), outerHighlight)
            drawCircle(CreamAura.copy(alpha = 0.22f), 2.5.dp.toPx(), outerHighlight)
            drawCircle(CreamAura.copy(alpha = 0.05f), 5.dp.toPx(), middleHighlight)
            drawCircle(CreamAura.copy(alpha = 0.20f), 2.2.dp.toPx(), middleHighlight)
        }
        Surface(
            onClick = onClick,
            enabled = enabled && !isStopped,
            modifier = Modifier
                .align(Alignment.Center)
                .size(112.dp)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .shadow(
                    elevation = 14.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x331E3F20),
                    spotColor = Color(0x291E3F20),
                )
                .clip(CircleShape),
            shape = CircleShape,
            color = WarmWhiteCard,
            border = androidx.compose.foundation.BorderStroke(6.dp, Cream.copy(alpha = 0.92f)),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_mic),
                    contentDescription = null,
                    tint = if (!useSpeechRecordingAura) {
                        Color(0xFF5A6A31)
                    } else if (isStopped) {
                        WarmBrown
                    } else {
                        PrimaryGreen
                    },
                    modifier = Modifier.size(38.dp),
                )
            }
        }
        if (showPrompt) {
            Text(
                text = stringResource(promptRes),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 198.dp),
                color = PrimaryGreen,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
