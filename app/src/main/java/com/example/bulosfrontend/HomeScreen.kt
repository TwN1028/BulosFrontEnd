package com.example.bulosfrontend

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.*

@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
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
            onLanguageSelected = viewModel::selectUiLanguage,
            onNavigate = onNavigate,
            listState = listState,
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
) {
    val darkTheme = LocalBulosDarkTheme.current
    val density = LocalDensity.current
    val labels = content.home
    val features = listOf(
        HomeFeature(labels.textTitleRes, labels.textSubtitleRes, painterResource(R.drawable.ic_lucide_languages), HomeTextTranslationCard, GoldenAccent, AppDestinations.TEXT),
        HomeFeature(labels.historyTitleRes, labels.historySubtitleRes, painterResource(R.drawable.ic_lucide_history), HomeSavedHistoryCard, MainText, AppDestinations.HISTORY),
        HomeFeature(labels.settingsTitleRes, labels.settingsSubtitleRes, painterResource(R.drawable.ic_lucide_settings), HomeSettingsCard, PrimaryGreen, AppDestinations.MORE),
        HomeFeature(R.string.ui_feature_help_title, R.string.ui_feature_help_subtitle, painterResource(R.drawable.ic_lucide_circle_help), HomeHelpCard, PrimaryGreen, AppDestinations.HELP),
    )

    Box(Modifier.fillMaxSize()) {
        val firstRowCardTop = 402.dp
        val creamBackgroundTop = firstRowCardTop + 24.dp
        val contentFadeStart = creamBackgroundTop - firstRowCardTop
        val hasScrollOffset by remember(listState) {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
            }
        }
        val featureGridAlpha by remember(listState, density) {
            derivedStateOf {
                val scrollDp = if (listState.firstVisibleItemIndex == 0) {
                    with(density) { listState.firstVisibleItemScrollOffset.toDp().value }
                } else {
                    152f
                }
                featureGridAlphaForScroll(scrollDp)
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(creamBackgroundTop)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0.00f to Color(0xFF1E3F20),
                            0.35f to Color(0xFF5E7A4A),
                            0.65f to Color(0xFFEAEBD9),
                            0.90f to Color(0xFFF7F8F2),
                            1.00f to Color(0xFFF7F8F2),
                        ),
                    ),
            )
            GeometricGreenBackground(
                modifier = Modifier.matchParentSize(),
                cellSize = 52.dp,
                patternAlpha = 0.08f,
                backgroundColor = Color.Transparent,
                patternColor = Color(0xFF5E7A4A),
            )
        }
        HomeIdentityHeader(
            title = stringResource(content.mainHeaderRes),
            badge = stringResource(labels.homeBadgeRes),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 22.dp, top = 32.dp, end = 22.dp),
        )
        HomeVoiceHero(
            promptRes = labels.speechSubtitleRes,
            onClick = { onNavigate(AppDestinations.VOICE) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 101.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = creamBackgroundTop)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(
                    if (darkTheme) MaterialTheme.colorScheme.background else Color(0xFFFFFBF4),
                ),
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
                    .padding(horizontal = 30.dp)
                    .then(
                        if (hasScrollOffset) {
                            Modifier
                                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                .drawWithContent {
                                    drawContent()
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            0f to Color.Transparent,
                                            0.30f to Color(0x55000000),
                                            1f to Color.Black,
                                            startY = contentFadeStart.toPx(),
                                            endY = (contentFadeStart + 72.dp).toPx(),
                                        ),
                                        blendMode = BlendMode.DstIn,
                                    )
                                }
                        } else {
                            Modifier
                        },
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                overscrollEffect = null,
            ) {
                items(2) { rowIndex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(118.dp)
                            .graphicsLayer { alpha = featureGridAlpha },
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
            if (hasScrollOffset) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = contentFadeStart)
                        .height(64.dp)
                        .blur(18.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                        .background(
                            Brush.verticalGradient(
                                0.00f to Color(0xEBF7F1DE),
                                0.24f to Color(0xDDF8F3E5),
                                0.52f to Color(0xA6FAF7ED),
                                0.78f to Color(0x66FCFAF5),
                                1.00f to Color.Transparent,
                            ),
                        ),
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                onClick = { onNavigate(AppDestinations.DICTIONARY) },
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = SoftGreen,
                contentColor = PrimaryGreen,
                shadowElevation = 5.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_dictionary_book),
                        contentDescription = stringResource(labels.navDictionaryRes),
                        modifier = Modifier.size(29.dp),
                    )
                }
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = stringResource(labels.navDictionaryRes),
                color = PrimaryGreen,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun featureGridAlphaForScroll(scrollDp: Float): Float {
    val stops = arrayOf(
        0f to 1f,
        32f to 0.94f,
        68f to 0.78f,
        104f to 0.50f,
        132f to 0.22f,
        152f to 0f,
    )
    if (scrollDp <= stops.first().first) return stops.first().second
    if (scrollDp >= stops.last().first) return stops.last().second
    val upperIndex = stops.indexOfFirst { scrollDp <= it.first }
    val lower = stops[upperIndex - 1]
    val upper = stops[upperIndex]
    val fraction = (scrollDp - lower.first) / (upper.first - lower.first)
    val easedFraction = FastOutSlowInEasing.transform(fraction)
    return lower.second + (upper.second - lower.second) * easedFraction
}

private val CreamAura = Color(0xFFFFF3DA)

@Composable
private fun HomeVoiceHero(@androidx.annotation.StringRes promptRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(254.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(280.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    0.00f to CreamAura.copy(alpha = 0.80f),
                    0.45f to CreamAura.copy(alpha = 0.56f),
                    0.75f to CreamAura.copy(alpha = 0.30f),
                    1.00f to Color.Transparent,
                    center = center,
                    radius = 140.dp.toPx(),
                ),
                radius = 140.dp.toPx(),
                center = center,
            )
        }
        Canvas(Modifier.fillMaxSize()) {
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
            drawCircle(CreamAura.copy(alpha = 0.24f), 2.5.dp.toPx(), Offset(center.x - 86.dp.toPx(), center.y - 51.dp.toPx()))
            drawCircle(CreamAura.copy(alpha = 0.24f), 2.5.dp.toPx(), Offset(center.x + 79.dp.toPx(), center.y + 69.dp.toPx()))
        }
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .size(112.dp)
                .shadow(14.dp, CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            shape = CircleShape,
            color = WarmWhiteCard,
            border = androidx.compose.foundation.BorderStroke(6.dp, Cream.copy(alpha = 0.92f)),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_mic),
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(38.dp),
                )
            }
        }
        Text(
            text = stringResource(promptRes),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 190.dp),
            color = PrimaryGreen,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
            fontWeight = FontWeight.SemiBold,
        )
    }
}
