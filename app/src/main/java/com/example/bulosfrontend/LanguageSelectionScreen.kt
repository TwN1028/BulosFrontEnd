package com.example.bulosfrontend

import android.animation.ValueAnimator
import android.os.Build
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.HomeCardBorder
import com.example.bulosfrontend.ui.theme.HomeSpeechGreen
import com.example.bulosfrontend.ui.theme.InactiveButtonColor
import com.example.bulosfrontend.ui.theme.MainText
import com.example.bulosfrontend.ui.theme.MutedText
import com.example.bulosfrontend.ui.theme.PrimaryGreen
import com.example.bulosfrontend.ui.theme.WarmWhiteCard
import kotlinx.coroutines.delay

private val GlobeGold = Color(0xFFF5C47C)
private val BulosSelectedMatte = Color(0xFF6B5138)
private val FilipinoSelectedMatte = Color(0xFFA8643C)
private val BulosLanguageName = Color(0xFF7B5E4A)
private val FilipinoLanguageName = Color(0xFFD1B89A)

private fun matteContinueColor(color: Color): Color {
    val warmBlend = 0.80f
    return Color(
        red = color.red + (WarmWhiteCard.red - color.red) * warmBlend,
        green = color.green + (WarmWhiteCard.green - color.green) * warmBlend,
        blue = color.blue + (WarmWhiteCard.blue - color.blue) * warmBlend,
        alpha = 1f,
    )
}

private data class LanguageOption(
    val language: UiLanguage,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val selectedColor: Color,
    val nameColor: Color,
)

private val languageOptions = listOf(
    LanguageOption(
        UiLanguage.BULOS,
        R.string.language_name_bulos,
        R.string.language_option_bulos,
        BulosSelectedMatte,
        BulosLanguageName,
    ),
    LanguageOption(
        UiLanguage.FILIPINO,
        R.string.language_name_filipino,
        R.string.language_option_filipino,
        FilipinoSelectedMatte,
        FilipinoLanguageName,
    ),
    LanguageOption(
        UiLanguage.ENGLISH,
        R.string.language_name_english,
        R.string.language_option_english,
        HomeSpeechGreen,
        MainText,
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
    val continueButtonColor = languageOptions
        .firstOrNull { it.language == pendingLanguage }
        ?.selectedColor
        ?.let(::matteContinueColor)
        ?: PrimaryGreen

    Box(modifier = modifier.fillMaxSize().background(splashGradientBrush())) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Surface(color = Color.Transparent, tonalElevation = 0.dp, shadowElevation = 0.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 32.dp, top = 12.dp, end = 32.dp, bottom = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        onClick = { pendingLanguage?.let(onLanguageSelected) },
                        enabled = pendingLanguage != null,
                        modifier = Modifier
                            .widthIn(max = 480.dp)
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = continueButtonColor,
                            contentColor = MainText,
                            disabledContainerColor = InactiveButtonColor,
                            disabledContentColor = MutedText,
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
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
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                                fontWeight = if (pendingLanguage == null) FontWeight.ExtraBold else FontWeight.Bold,
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .safeHeaderInsets()
                            .padding(start = 24.dp, top = 40.dp, end = 24.dp, bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = stringResource(R.string.language_selection_globe_description),
                                    modifier = Modifier.size(22.dp),
                                    tint = LocalContentColor.current.copy(alpha = 0.86f),
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        RotatingLocalizedHeader(
                            englishTitleRes = titleRes,
                            color = Color.White,
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 48.dp, end = 32.dp, bottom = 24.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 480.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        languageOptions.forEach { option ->
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
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp)
            .semantics { this.selected = selected },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HomeContentCream,
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = HomeCardBorder,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 0.5.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 84.dp)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(option.nameRes),
                modifier = Modifier.fillMaxWidth(),
                color = option.nameColor,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    letterSpacing = (-0.1).sp,
                ),
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(option.descriptionRes),
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RotatingLocalizedHeader(
    @StringRes englishTitleRes: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val rotatingHeaderResources = remember(englishTitleRes) {
        listOf(
            englishTitleRes,
            R.string.language_selection_subtitle_filipino,
            R.string.language_selection_subtitle_bulos,
        )
    }
    val context = LocalContext.current
    val animationsEnabled = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ValueAnimator.areAnimatorsEnabled()
        } else {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) > 0f
        }
    }
    var headerIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(animationsEnabled) {
        if (!animationsEnabled) {
            headerIndex = 0
            return@LaunchedEffect
        }
        while (true) {
            delay(3_000L)
            headerIndex = (headerIndex + 1) % rotatingHeaderResources.size
        }
    }

    Box(
        modifier = modifier.fillMaxWidth().heightIn(min = 72.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Crossfade(
            targetState = headerIndex,
            animationSpec = tween(
                durationMillis = 500,
                easing = LinearEasing,
            ),
            label = "languageHeaderCrossfade",
        ) { index ->
            Text(
                text = stringResource(rotatingHeaderResources[index]),
                modifier = Modifier.fillMaxWidth(),
                color = color,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    letterSpacing = (-0.2).sp,
                ),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 3,
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
    Surface(
        modifier = modifier.fillMaxSize().background(splashGradientBrush()),
        color = Color.Transparent,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            item {
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.White.copy(alpha = 0.86f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                RotatingLocalizedHeader(
                    englishTitleRes = titleRes,
                    color = Color.White,
                )
                Spacer(Modifier.height(20.dp))
            }
            items(languageOptions) { option ->
                ReferenceLanguageCard(
                    option = option,
                    selected = selectedLanguage == option.language,
                    onClick = { onLanguageSelected(option.language) },
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
