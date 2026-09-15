package com.example.bulosfrontend

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.*
import kotlinx.coroutines.delay

private val GlobeGold = Color(0xFFF5C47C)
private val BulosSelectedMatte = Color(0xFF6B5138)
private val FilipinoSelectedMatte = Color(0xFFA8643C)

private fun matteContinueColor(color: Color): Color {
    val warmBlend = 0.80f
    return Color(
        red = color.red + (WarmWhiteCard.red - color.red) * warmBlend,
        green = color.green + (WarmWhiteCard.green - color.green) * warmBlend,
        blue = color.blue + (WarmWhiteCard.blue - color.blue) * warmBlend,
        alpha = 1f,
    )
}

private val rotatingHeadingResources = listOf(
    R.string.language_selection_title,
    R.string.language_selection_heading_filipino,
    R.string.language_selection_subtitle_bulos,
)

private data class LanguageOption(
    val language: UiLanguage,
    @StringRes val badgeRes: Int,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val color: Color,
    val selectedColor: Color,
)

private val languageOptions = listOf(
    LanguageOption(
        UiLanguage.ENGLISH,
        R.string.language_badge_english,
        R.string.language_name_english,
        R.string.language_option_english,
        Design.DarkForestGreen,
        HomeSpeechGreen,
    ),
    LanguageOption(
        UiLanguage.FILIPINO,
        R.string.language_badge_filipino,
        R.string.language_name_filipino,
        R.string.language_option_filipino,
        GoldenAccent,
        FilipinoSelectedMatte,
    ),
    LanguageOption(
        UiLanguage.BULOS,
        R.string.language_badge_bulos,
        R.string.language_name_bulos,
        R.string.language_option_bulos,
        Design.ButtonBrown,
        BulosSelectedMatte,
    ),
)

@Composable
fun LanguageSelectionScreen(
    @StringRes titleRes: Int,
    selectedLanguage: UiLanguage?,
    onLanguageSelected: (UiLanguage) -> Unit,
    modifier: Modifier = Modifier,
    confirmationRequired: Boolean = false,
) {
    if (!confirmationRequired) {
        ImmediateLanguageSelectionScreen(
            titleRes = titleRes,
            selectedLanguage = selectedLanguage,
            onLanguageSelected = onLanguageSelected,
            modifier = modifier,
        )
        return
    }

    var pendingLanguage by rememberSaveable { mutableStateOf(selectedLanguage) }
    var headingIndex by rememberSaveable { mutableStateOf(0) }
    val headingAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2_500L)
            headingAlpha.animateTo(0f, animationSpec = tween(durationMillis = 500))
            headingIndex = (headingIndex + 1) % rotatingHeadingResources.size
            headingAlpha.animateTo(1f, animationSpec = tween(durationMillis = 500))
        }
    }
    val continueButtonColor = languageOptions
        .firstOrNull { it.language == pendingLanguage }
        ?.selectedColor
        ?.let(::matteContinueColor)
        ?: Design.DarkForestGreen

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = Design.PaddingScreen, top = 12.dp, end = Design.PaddingScreen, bottom = Design.PaddingElement),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        onClick = { pendingLanguage?.let(onLanguageSelected) },
                        enabled = pendingLanguage != null,
                        modifier = Modifier
                            .widthIn(max = 480.dp)
                            .fillMaxWidth()
                            .height(Design.ButtonHeightAction),
                        shape = Design.CardShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = continueButtonColor,
                            contentColor = MainText,
                            disabledContainerColor = Sand.copy(alpha = Design.AlphaSecondary),
                            disabledContentColor = MutedText,
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = Design.ElevationDefault,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp,
                        ),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.language_selection_continue),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (pendingLanguage == null) Design.FontExtraBold else Design.FontBold,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                }
            },
        ) { scaffoldPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding),
            ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clipToBounds(),
                ) {
                    GeometricGreenBackground(
                        modifier = Modifier.matchParentSize(),
                        cellSize = 52.dp,
                        patternAlpha = Design.AlphaPatternLarge,
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = Design.PaddingHorizontal, top = 40.dp, end = Design.PaddingHorizontal, bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Surface(
                            modifier = Modifier.size(Design.IconSizeSmall),
                            shape = Design.CardShape,
                            color = Color.White.copy(alpha = 0.15f),
                            contentColor = GlobeGold,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = stringResource(R.string.language_selection_globe_description),
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Design.IconSizeSmall)
                                .graphicsLayer { alpha = headingAlpha.value },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(rotatingHeadingResources[headingIndex]),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = Design.FontExtraBold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Design.PaddingScreen, top = Design.IconSizeSmall, end = Design.PaddingScreen, bottom = Design.PaddingHorizontal),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 480.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Design.SpacingSmall + 4.dp),
                    ) {
                        languageOptions.asReversed().forEach { option ->
                            ReferenceLanguageCard(
                                option = option,
                                selected = pendingLanguage == option.language,
                                onClick = { pendingLanguage = option.language },
                            )
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun ReferenceLanguageCard(
    option: LanguageOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "languageCardScale",
    )
    val contentColor = if (selected) WarmWhiteCard else MaterialTheme.colorScheme.onSurface
    val descriptionColor = if (selected) WarmWhiteCard.copy(alpha = 0.82f) else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp)
            .scale(scale),
        shape = Design.CardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) option.selectedColor else MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = 0.75.dp,
            color = if (selected) Color.White.copy(alpha = 0.42f) else MaterialTheme.colorScheme.outlineVariant,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) Design.ElevationDefault else Design.ElevationLow,
            pressedElevation = 0.5.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 84.dp)
                .padding(horizontal = Design.PaddingElement, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(Design.IconSizeSmall),
                shape = Design.ButtonShape,
                color = if (selected) WarmWhiteCard.copy(alpha = 0.20f) else option.color.copy(alpha = 0.09f),
                contentColor = if (selected) WarmWhiteCard else option.color,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(option.badgeRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = Design.FontExtraBold,
                    )
                }
            }
            Spacer(Modifier.width(Design.PaddingElement))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(option.nameRes),
                    color = contentColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = Design.FontBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(Design.PaddingMicro))
                Text(
                    text = stringResource(option.descriptionRes),
                    color = descriptionColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.width(12.dp))
            LanguageSelectionControl(selected = selected, selectedColor = option.color)
        }
    }
}

@Composable
private fun LanguageSelectionControl(selected: Boolean, selectedColor: Color) {
    Box(
        modifier = Modifier
            .size(Design.IconSizeMini)
            .then(
                if (selected) {
                    Modifier.background(Color.White, CircleShape)
                } else {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = selectedColor,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun ImmediateLanguageSelectionScreen(
    @StringRes titleRes: Int,
    selectedLanguage: UiLanguage?,
    onLanguageSelected: (UiLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Design.PaddingHorizontal, vertical = Design.PaddingScreen),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            item {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    stringResource(titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = Design.FontBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(Design.SpacingLarge + 4.dp))
            }
            items(languageOptions) { option ->
                Card(
                    onClick = { onLanguageSelected(option.language) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 84.dp),
                    shape = Design.CardShape.copy(all = CornerSize(18.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedLanguage == option.language) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = Design.ElevationDefault),
                ) {
                    Box(
                        Modifier.fillMaxWidth().padding(horizontal = Design.PaddingHorizontalSmall, vertical = 22.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            stringResource(option.descriptionRes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Spacer(Modifier.height(Design.PaddingElement))
            }
        }
    }
}
