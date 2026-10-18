package com.example.bulosfrontend

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.ForestGreen
import com.example.bulosfrontend.ui.theme.PrimaryGreen
import com.example.bulosfrontend.ui.theme.SharedActiveButtonColor
import com.example.bulosfrontend.ui.theme.SoftGreen
import com.example.bulosfrontend.ui.theme.SplashSage as ThemeSplashSage
import com.example.bulosfrontend.ui.theme.WarmWhite
import com.example.bulosfrontend.ui.theme.WarmWhiteCard
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

private data class OnboardingPage(@StringRes val titleRes: Int, @StringRes val bodyRes: Int)

private val DemoFeatureActionShape = RoundedCornerShape(24.dp)
private val DemoFeatureActionBrush = Brush.horizontalGradient(
    listOf(ThemeSplashSage, SharedActiveButtonColor, ForestGreen),
)
private val DemoFeatureActionBorder = BorderStroke(1.dp, WarmWhite.copy(alpha = 0.42f))

@StringRes
private fun demoTranslationNoticeRes(language: UiLanguage): Int = when (language) {
    UiLanguage.ENGLISH -> R.string.ui_demo_translation_notice_eng
    UiLanguage.FILIPINO -> R.string.ui_demo_translation_notice_fil
    UiLanguage.BULOS -> R.string.ui_demo_translation_notice_bul
}

@StringRes
private fun demoTextRes(language: UiLanguage, @StringRes englishRes: Int, @StringRes filipinoRes: Int): Int =
    if (language == UiLanguage.ENGLISH) englishRes else filipinoRes

private data class OnboardingLabels(
    @StringRes val skipRes: Int,
    @StringRes val backRes: Int,
    @StringRes val nextRes: Int,
    @StringRes val startRes: Int,
    @StringRes val guideRes: Int,
    @StringRes val openLiveScreenRes: Int,
    val pages: List<OnboardingPage>,
)

private fun onboardingLabels(language: UiLanguage): OnboardingLabels {
    val suffix = when (language) {
        UiLanguage.ENGLISH -> "eng"
        UiLanguage.FILIPINO -> "fil"
        UiLanguage.BULOS -> "bul"
    }
    fun id(name: String): Int = when ("${name}_$suffix") {
        "ui_onboarding_skip_eng" -> R.string.ui_onboarding_skip_eng
        "ui_onboarding_skip_fil" -> R.string.ui_onboarding_skip_fil
        "ui_onboarding_skip_bul" -> R.string.ui_onboarding_skip_bul
        "ui_onboarding_back_eng" -> R.string.ui_onboarding_back_eng
        "ui_onboarding_back_fil" -> R.string.ui_onboarding_back_fil
        "ui_onboarding_back_bul" -> R.string.ui_onboarding_back_bul
        "ui_onboarding_next_eng" -> R.string.ui_onboarding_next_eng
        "ui_onboarding_next_fil" -> R.string.ui_onboarding_next_fil
        "ui_onboarding_next_bul" -> R.string.ui_onboarding_next_bul
        "ui_onboarding_start_eng" -> R.string.ui_onboarding_start_eng
        "ui_onboarding_start_fil" -> R.string.ui_onboarding_start_fil
        "ui_onboarding_start_bul" -> R.string.ui_onboarding_start_bul
        "ui_onboarding_guide_eng" -> R.string.ui_onboarding_guide_eng
        "ui_onboarding_guide_fil" -> R.string.ui_onboarding_guide_fil
        "ui_onboarding_open_live_screen_eng" -> R.string.ui_onboarding_open_live_screen_eng
        "ui_onboarding_open_live_screen_fil" -> R.string.ui_onboarding_open_live_screen_fil
        "ui_onboarding_open_live_screen_bul" -> R.string.ui_onboarding_open_live_screen_bul
        else -> R.string.ui_onboarding_guide_bul
    }
    fun page(name: String): OnboardingPage = when (suffix) {
        "fil" -> OnboardingPage(
            if (name == "welcome") R.string.ui_onboarding_welcome_title_fil else if (name == "speech") R.string.ui_onboarding_speech_title_fil else if (name == "text") R.string.ui_onboarding_text_title_fil else if (name == "dictionary") R.string.ui_onboarding_dictionary_title_fil else if (name == "saved" || name == "favorites") R.string.ui_onboarding_saved_title_fil else if (name == "settings") R.string.ui_onboarding_settings_title_fil else R.string.ui_onboarding_offline_title_fil,
            if (name == "welcome") R.string.ui_onboarding_welcome_body_fil else if (name == "speech") R.string.ui_onboarding_speech_body_fil else if (name == "text") R.string.ui_onboarding_text_body_fil else if (name == "dictionary") R.string.ui_onboarding_dictionary_body_fil else if (name == "saved" || name == "favorites") R.string.ui_onboarding_saved_body_fil else if (name == "settings") R.string.ui_onboarding_settings_body_fil else R.string.ui_onboarding_offline_body_fil,
        )
        "bul" -> OnboardingPage(
            if (name == "welcome") R.string.ui_onboarding_welcome_title_bul else if (name == "speech") R.string.ui_onboarding_speech_title_bul else if (name == "text") R.string.ui_onboarding_text_title_bul else if (name == "dictionary") R.string.ui_onboarding_dictionary_title_bul else if (name == "saved" || name == "favorites") R.string.ui_onboarding_saved_title_bul else if (name == "settings") R.string.ui_onboarding_settings_title_bul else R.string.ui_onboarding_offline_title_bul,
            if (name == "welcome") R.string.ui_onboarding_welcome_body_bul else if (name == "speech") R.string.ui_onboarding_speech_body_bul else if (name == "text") R.string.ui_onboarding_text_body_bul else if (name == "dictionary") R.string.ui_onboarding_dictionary_body_bul else if (name == "saved" || name == "favorites") R.string.ui_onboarding_saved_body_bul else if (name == "settings") R.string.ui_onboarding_settings_body_bul else R.string.ui_onboarding_offline_body_bul,
        )
        else -> OnboardingPage(
            if (name == "welcome") R.string.ui_onboarding_welcome_title_eng else if (name == "speech") R.string.ui_onboarding_speech_title_eng else if (name == "text") R.string.ui_onboarding_text_title_eng else if (name == "dictionary") R.string.ui_onboarding_dictionary_title_eng else if (name == "saved" || name == "favorites") R.string.ui_onboarding_saved_title_eng else if (name == "settings") R.string.ui_onboarding_settings_title_eng else R.string.ui_onboarding_offline_title_eng,
            if (name == "welcome") R.string.ui_onboarding_welcome_body_eng else if (name == "speech") R.string.ui_onboarding_speech_body_eng else if (name == "text") R.string.ui_onboarding_text_body_eng else if (name == "dictionary") R.string.ui_onboarding_dictionary_body_eng else if (name == "saved" || name == "favorites") R.string.ui_onboarding_saved_body_eng else if (name == "settings") R.string.ui_onboarding_settings_body_eng else R.string.ui_onboarding_offline_body_eng,
        )
    }
    return OnboardingLabels(
        skipRes = id("ui_onboarding_skip"),
        backRes = id("ui_onboarding_back"),
        nextRes = id("ui_onboarding_next"),
        startRes = id("ui_onboarding_start"),
        guideRes = id("ui_onboarding_guide"),
        openLiveScreenRes = id("ui_onboarding_open_live_screen"),
        pages = listOf("welcome", "speech", "text", "saved", "favorites", "dictionary", "settings", "complete").map(::page),
    )
}

@Composable
fun OnboardingScreen(
    uiLanguage: UiLanguage,
    isReplay: Boolean,
    sttWalkthroughCompleted: Boolean,
    onComplete: () -> Unit,
    onExit: () -> Unit,
    onOpenFeature: (String) -> Unit,
    onStartSttWalkthrough: () -> Unit,
) {
    val labels = onboardingLabels(uiLanguage)
    val pagerState = rememberPagerState(pageCount = { labels.pages.size })
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val content = DialogueProvider.getDialogue(uiLanguage)
    var textDemoInteracted by rememberSaveable { mutableStateOf(false) }

    androidx.activity.compose.BackHandler {
        if (pagerState.currentPage > 0) {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
        } else {
            onExit()
        }
    }

    Surface(Modifier.fillMaxSize(), color = HomeContentCream) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            // Replayed onboarding behaves as Help; first-run onboarding cannot be skipped.
            if (isReplay) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = onExit,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmWhiteCard,
                        contentColor = ForestGreen,
                    ),
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.22f)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                ) {
                    Text(
                        stringResource(labels.backRes),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                pageSpacing = 12.dp,
            ) { pageIndex ->
                val page = labels.pages[pageIndex]
                val active = pagerState.currentPage == pageIndex
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    val titleAndBody: @Composable () -> Unit = {
                        AnimatedVisibility(
                            visible = active && pageIndex != 0,
                            enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 8 },
                            exit = fadeOut(tween(120)),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    stringResource(page.titleRes),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ForestGreen,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    stringResource(page.bodyRes),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TranslationInputTextColor,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                    val liveSlide: @Composable () -> Unit = {
                        OnboardingLiveSlide(
                            page = pageIndex,
                            content = content,
                            uiLanguage = uiLanguage,
                            onOpenFeature = onOpenFeature,
                            onStartSttWalkthrough = onStartSttWalkthrough,
                            onTextDemoInteracted = { textDemoInteracted = true },
                        )
                    }

                    if (pageIndex == 0) {
                        liveSlide()
                        Spacer(Modifier.height(28.dp))
                        titleAndBody()
                    } else if (pageIndex == labels.pages.lastIndex) {
                        liveSlide()
                    } else {
                        titleAndBody()
                        Spacer(Modifier.height(40.dp))
                        liveSlide()
                    }
                }
            }
            if (pagerState.currentPage == 0) {
                Spacer(Modifier.height(18.dp))
                OnboardingProgress(
                    currentPage = pagerState.currentPage,
                    pageCount = labels.pages.size,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SharedActiveButtonColor,
                        contentColor = WarmWhite,
                    ),
                ) {
                    Text(
                        stringResource(
                            demoTextRes(uiLanguage, R.string.ui_demo_get_started_eng, R.string.ui_demo_get_started_fil),
                        ),
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(7.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
            if (pagerState.currentPage > 0) {
                Spacer(Modifier.height(18.dp))
                OnboardingProgress(currentPage = pagerState.currentPage, pageCount = labels.pages.size)
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(labels.backRes), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.weight(1f))
                    val isFinalOnboardingPage = pagerState.currentPage == labels.pages.lastIndex
                    val canAdvanceFromCurrentPage = when (pagerState.currentPage) {
                        // The speech walkthrough has its own completion tracking.
                        1 -> sttWalkthroughCompleted
                        // Require engagement with the actual input field before proceeding.
                        2 -> textDemoInteracted
                        else -> true
                    }
                    Button(
                        enabled = canAdvanceFromCurrentPage,
                        onClick = {
                            if (isFinalOnboardingPage) onComplete()
                            else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        },
                        modifier = Modifier
                            .height(54.dp)
                            .widthIn(min = 150.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SharedActiveButtonColor,
                            contentColor = WarmWhite,
                        ),
                    ) {
                        Text(
                            stringResource(if (isFinalOnboardingPage) labels.startRes else labels.nextRes),
                            fontWeight = FontWeight.Bold,
                        )
                        if (!isFinalOnboardingPage) {
                            Spacer(Modifier.width(7.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(17.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun onboardingRoute(page: Int): String = when (page) {
    0 -> AppDestinations.HOME
    1 -> AppDestinations.HOME_RECORDING
    2 -> AppDestinations.TEXT
    3 -> AppDestinations.HISTORY
    4 -> AppDestinations.HISTORY
    5 -> AppDestinations.DICTIONARY
    6 -> AppDestinations.MORE
    else -> AppDestinations.HOME
}

@Composable
private fun OnboardingLiveSlide(
    page: Int,
    content: DialogueContent,
    uiLanguage: UiLanguage,
    onOpenFeature: (String) -> Unit,
    onStartSttWalkthrough: () -> Unit,
    onTextDemoInteracted: () -> Unit,
) {
    when (page) {
        0 -> ExploreBulosSlide(uiLanguage)
        1 -> SpeechWalkthroughSlide(content, uiLanguage, onStartSttWalkthrough)
        2 -> TextWalkthroughSlide(content, uiLanguage, onTextDemoInteracted)
        3 -> SavedWalkthroughSlide(uiLanguage, onOpenFeature)
        4 -> SavedFiltersWalkthroughSlide(uiLanguage, onOpenFeature)
        5 -> DictionaryWalkthroughSlide(content, onOpenFeature)
        6 -> SettingsWalkthroughSlide(content, onOpenFeature)
        else -> TutorialCompleteSlide(uiLanguage)
    }
}

@Composable
private fun ExploreBulosSlide(uiLanguage: UiLanguage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Magiano Kamu Di?",
                color = ForestGreen,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(demoTextRes(uiLanguage, R.string.ui_demo_explore_heading_eng, R.string.ui_demo_explore_heading_fil)),
                color = ForestGreen,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 34.sp,
                    lineHeight = 37.sp,
                ),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(demoTextRes(uiLanguage, R.string.ui_demo_explore_language_eng, R.string.ui_demo_explore_language_fil)),
                color = ThemeSplashSage,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 34.sp,
                    lineHeight = 37.sp,
                ),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = stringResource(demoTextRes(uiLanguage, R.string.ui_demo_explore_body_eng, R.string.ui_demo_explore_body_fil)),
                color = TranslationInputTextColor,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TutorialCompleteSlide(uiLanguage: UiLanguage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(demoTextRes(uiLanguage, R.string.ui_demo_complete_title_eng, R.string.ui_demo_complete_title_fil)),
                color = ForestGreen,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(demoTextRes(uiLanguage, R.string.ui_demo_ready_heading_eng, R.string.ui_demo_ready_heading_fil)),
                color = ForestGreen,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 34.sp,
                    lineHeight = 37.sp,
                ),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(demoTextRes(uiLanguage, R.string.ui_demo_ready_body_eng, R.string.ui_demo_ready_body_fil)),
                color = ThemeSplashSage,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 34.sp,
                    lineHeight = 37.sp,
                ),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = stringResource(demoTextRes(uiLanguage, R.string.ui_demo_complete_body_eng, R.string.ui_demo_complete_body_fil)),
                color = TranslationInputTextColor,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SpeechWalkthroughSlide(
    content: DialogueContent,
    uiLanguage: UiLanguage,
    onStartSttWalkthrough: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = HomeCardShape,
        color = HomeContentCream,
        border = HomeSupportingCardBorder,
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            HomeVoiceHero(
                promptRes = content.home.speechSubtitleRes,
                onClick = onStartSttWalkthrough,
                modifier = Modifier.fillMaxWidth().height(190.dp),
                // Reuse the real idle STT microphone renderer, including its
                // subtle ambient ring motion, for the Help preview.
                isRecording = false,
                useSpeechRecordingAura = true,
                showMicrophoneButtonBorder = false,
            )
            Text(
                stringResource(demoTextRes(uiLanguage, R.string.ui_demo_speech_body_eng, R.string.ui_demo_speech_body_fil)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                color = TranslationInputTextColor,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
            )
            Button(
                onClick = onStartSttWalkthrough,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(DemoFeatureActionBrush, DemoFeatureActionShape),
                shape = DemoFeatureActionShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = WarmWhite,
                ),
                border = DemoFeatureActionBorder,
            ) {
                Text(
                    stringResource(demoTextRes(uiLanguage, R.string.ui_demo_try_speech_eng, R.string.ui_demo_try_speech_fil)),
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(7.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}

@Composable
private fun TextWalkthroughSlide(
    content: DialogueContent,
    uiLanguage: UiLanguage,
    onInteracted: () -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    var showDemoOutcome by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = HomeCardShape,
        color = HomeContentCream,
        border = HomeSupportingCardBorder,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TranslationLanguageBar(
                swapLanguagesDescription = stringResource(content.speechResult.swapLanguagesDescriptionRes),
                uiLanguage = uiLanguage,
            )
            TranslationInputCard(
                label = TranslationState.sourceLanguage.uppercase(),
                value = input,
                placeholder = stringResource(
                    content.textTranslation.inputPlaceholderRes,
                    TranslationState.sourceLanguage,
                ),
                onValueChange = { input = it },
                textColor = TranslationInputTextColor,
                minimumHeight = 106.dp,
                textFieldHeight = 42.dp,
                onFocusChanged = { focused ->
                    if (focused) onInteracted()
                },
            )
            Button(
                onClick = { showDemoOutcome = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SharedActiveButtonColor, contentColor = WarmWhite),
            ) {
                Text(
                    stringResource(
                        content.textTranslation.translateToRes,
                        TranslationState.targetLanguage,
                    ),
                    fontWeight = FontWeight.Bold,
                )
            }
            TranslationOutputCard(
                label = TranslationState.targetLanguage.uppercase(),
                translatedText = null,
                placeholder = if (showDemoOutcome) {
                    stringResource(demoTranslationNoticeRes(uiLanguage))
                } else {
                    ""
                },
                textColor = TranslationInputTextColor,
                minimumHeight = 100.dp,
                isSaved = saved,
                onToggleSaved = { saved = !saved },
            )
        }
    }
}

@Composable
private fun DictionaryWalkthroughSlide(
    content: DialogueContent,
    onOpenFeature: (String) -> Unit,
) {
    val home = content.home
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = HomeCardShape,
        color = HomeContentCream,
        border = HomeSupportingCardBorder,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = LanguageSelectorCardShape,
                color = Color(0xFFFFFEFA),
                border = HomeSupportingCardBorder,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_dictionary_book),
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(home.dictionarySearchRes),
                        color = TranslationInputTextColor,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DictionaryDemoLanguagePill(stringResource(UiLanguage.ENGLISH.displayNameRes), Modifier.weight(1f))
                DictionaryDemoLanguagePill(stringResource(UiLanguage.BULOS.displayNameRes), Modifier.weight(1f))
            }
            Button(
                onClick = { onOpenFeature(AppDestinations.DICTIONARY) },
                modifier = Modifier.fillMaxWidth().height(48.dp).background(DemoFeatureActionBrush, DemoFeatureActionShape),
                shape = DemoFeatureActionShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = WarmWhite,
                ),
                border = DemoFeatureActionBorder,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_dictionary_book),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(stringResource(home.dictionaryTitleRes), fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(7.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun DictionaryDemoLanguagePill(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFFFEFA),
        border = HomeSupportingCardBorder,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(label, color = ForestGreen, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = ForestGreen)
        }
    }
}

@Composable
private fun SavedWalkthroughSlide(uiLanguage: UiLanguage, onOpenFeature: (String) -> Unit) {
    var saved by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = HomeCardShape,
        color = HomeContentCream,
        border = HomeSupportingCardBorder,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            TranslationOutputCard(
                label = TranslationState.targetLanguage.uppercase(),
                translatedText = "Tuntonin",
                placeholder = "",
                textColor = TranslationInputTextColor,
                minimumHeight = 112.dp,
                isSaved = saved,
                onToggleSaved = { saved = !saved },
            )
            Text(
                stringResource(
                    demoTextRes(
                        uiLanguage,
                        if (saved) R.string.ui_demo_saved_confirmation_eng else R.string.ui_demo_saved_hint_eng,
                        if (saved) R.string.ui_demo_saved_confirmation_fil else R.string.ui_demo_saved_hint_fil,
                    ),
                ),
                color = TranslationInputTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { onOpenFeature(AppDestinations.HISTORY) },
                modifier = Modifier.fillMaxWidth().height(48.dp).background(DemoFeatureActionBrush, DemoFeatureActionShape),
                shape = DemoFeatureActionShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = WarmWhite),
                border = DemoFeatureActionBorder,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_bookmark_outline),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    stringResource(demoTextRes(uiLanguage, R.string.ui_demo_open_saved_eng, R.string.ui_demo_open_saved_fil)),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SavedFiltersWalkthroughSlide(
    uiLanguage: UiLanguage,
    onOpenFeature: (String) -> Unit,
) {
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = HomeCardShape,
        color = HomeContentCream,
        border = HomeSupportingCardBorder,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HistoryFilterControl(
                selectedFilter = selectedFilter,
                uiLanguage = uiLanguage,
                onFilterSelected = { selectedFilter = it },
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = HomeCardShape,
                color = WarmWhiteCard,
                border = HomeSupportingCardBorder,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = if (selectedFilter == HistoryFilter.FAVORITES) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = if (selectedFilter == HistoryFilter.FAVORITES) {
                            stringResource(demoTextRes(uiLanguage, R.string.ui_demo_favorites_body_eng, R.string.ui_demo_favorites_body_fil))
                        } else {
                            stringResource(demoTextRes(uiLanguage, R.string.ui_demo_all_saved_body_eng, R.string.ui_demo_all_saved_body_fil))
                        },
                        modifier = Modifier.weight(1f),
                        color = TranslationInputTextColor,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Button(
                onClick = { onOpenFeature(AppDestinations.HISTORY) },
                modifier = Modifier.fillMaxWidth().height(48.dp).background(DemoFeatureActionBrush, DemoFeatureActionShape),
                shape = DemoFeatureActionShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = WarmWhite,
                ),
                border = DemoFeatureActionBorder,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_bookmark_outline),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    stringResource(demoTextRes(uiLanguage, R.string.ui_demo_open_saved_eng, R.string.ui_demo_open_saved_fil)),
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(7.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun SettingsWalkthroughSlide(
    content: DialogueContent,
    onOpenFeature: (String) -> Unit,
) {
    var offlineEnabled by remember { mutableStateOf(false) }
    var selectedSize by remember { mutableStateOf(AppFontSize.MEDIUM) }
    val home = content.home

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = HomeCardShape,
        color = HomeContentCream,
        border = HomeSupportingCardBorder,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = HomeCardShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFAF2)),
                border = HomeSupportingCardBorder,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lucide_cloud_off),
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(home.offlineModeRes),
                            style = MaterialTheme.typography.titleMedium,
                            color = ForestGreen,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            stringResource(home.offlineModeSummaryRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = TranslationInputTextColor,
                        )
                    }
                    androidx.compose.material3.Switch(
                        checked = offlineEnabled,
                        onCheckedChange = { offlineEnabled = it },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = WarmWhite,
                            checkedTrackColor = SharedActiveButtonColor,
                            checkedBorderColor = SharedActiveButtonColor,
                            uncheckedThumbColor = WarmWhite,
                            uncheckedTrackColor = Color(0xFFF1E8DA),
                            uncheckedBorderColor = Color(0xFFE5DDD2).copy(alpha = 0.65f),
                        ),
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = HomeCardShape,
                colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
                border = HomeSupportingCardBorder,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_type),
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(home.fontSizeRes),
                            style = MaterialTheme.typography.titleMedium,
                            color = ForestGreen,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        listOf(
                            AppFontSize.SMALL to stringResource(home.smallFontRes),
                            AppFontSize.MEDIUM to stringResource(home.mediumFontRes),
                            AppFontSize.LARGE to stringResource(home.largeFontRes),
                        ).forEach { (size, label) ->
                            Button(
                                onClick = { selectedSize = size },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedSize == size) SharedActiveButtonColor else Color(0xFFF2F0EA),
                                    contentColor = if (selectedSize == size) WarmWhite else ForestGreen,
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                            ) {
                                Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            }
            Button(
                onClick = { onOpenFeature(AppDestinations.MORE) },
                modifier = Modifier.fillMaxWidth().height(48.dp).background(DemoFeatureActionBrush, DemoFeatureActionShape),
                shape = DemoFeatureActionShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = WarmWhite),
                border = DemoFeatureActionBorder,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_settings),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(stringResource(home.settingsTitleRes), fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(7.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun LiveWalkthroughLauncher(
    route: String,
    @StringRes labelRes: Int,
    onOpenFeature: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = HomeCardShape,
        color = Color(0xFFF1F4E7),
        border = HomeSupportingCardBorder,
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(
                    when (route) {
                        AppDestinations.HOME_RECORDING -> R.drawable.ic_lucide_mic
                        AppDestinations.TEXT -> R.drawable.ic_lucide_languages
                        AppDestinations.DICTIONARY -> R.drawable.ic_dictionary_book
                        AppDestinations.HISTORY -> R.drawable.ic_bookmark_outline
                        AppDestinations.MORE -> R.drawable.ic_lucide_settings
                        else -> R.drawable.ic_lucide_home
                    },
                ),
                contentDescription = null,
                tint = ForestGreen,
                modifier = Modifier.size(44.dp),
            )
            Button(
                onClick = { onOpenFeature(route) },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SharedActiveButtonColor,
                    contentColor = WarmWhite,
                ),
            ) {
                Text(stringResource(labelRes), fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(7.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun OnboardingProgress(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val width by animateFloatAsState(if (index == currentPage) 24f else 7f, tween(220), label = "guideProgress$index")
            Box(
                Modifier
                    .width(width.dp)
                    .height(7.dp)
                    .background(
                        if (index == currentPage) SharedActiveButtonColor else Color(0xFFD8D5CA),
                        CircleShape,
                    ),
            )
        }
    }
}

@Composable
private fun OnboardingFeaturePreview(
    page: Int,
    active: Boolean,
    content: DialogueContent,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(if (active) 1f else 0.96f, tween(260), label = "guidePreviewScale$page")
    GuidePreviewFrame(
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
    ) {
        when (page) {
            0 -> HomeGuidePreview(active, content)
            1 -> SpeechGuidePreview(active, content)
            2 -> TextGuidePreview(active, content)
            3 -> DictionaryGuidePreview(active, content)
            4 -> SavedGuidePreview(active, content)
            else -> OfflineGuidePreview(active, content)
        }
    }
}

@Composable
private fun GuidePreviewFrame(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.heightIn(min = 280.dp, max = 350.dp).shadow(10.dp, HomeCardShape),
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = HomeSupportingCardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFFFFFEFC), Color(0xFFF1F4E7))))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun HomeGuidePreview(active: Boolean, content: DialogueContent) {
    Text("Bulos Translator", color = ForestGreen, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    GuideMic(active)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        GuideFeatureTile(R.drawable.ic_lucide_languages, stringResource(content.home.textTitleRes), active, Modifier.weight(1f))
        GuideFeatureTile(R.drawable.ic_dictionary_book, stringResource(content.home.dictionaryTitleRes), active, Modifier.weight(1f))
    }
}

@Composable
private fun SpeechGuidePreview(active: Boolean, content: DialogueContent) {
    Text(stringResource(content.voiceHeaderRes), color = ForestGreen, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    GuideMic(active)
    Surface(shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.82f), border = HomeSupportingCardBorder) {
        Text(
            stringResource(content.speechResult.recognizedTextLabelRes),
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            color = TranslationInputTextColor,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun TextGuidePreview(active: Boolean, content: DialogueContent) {
    GuideLanguageStrip()
    Surface(shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.82f), border = HomeSupportingCardBorder) {
        Column(Modifier.padding(14.dp)) {
            Text("English", color = ForestGreen, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(9.dp))
            Text("Rule", color = TranslationInputTextColor, style = MaterialTheme.typography.bodyLarge)
        }
    }
    Button(
        onClick = {}, enabled = false,
        modifier = Modifier.fillMaxWidth().height(44.dp),
        colors = ButtonDefaults.buttonColors(disabledContainerColor = SharedActiveButtonColor, disabledContentColor = WarmWhite),
    ) { Text(stringResource(content.textTranslation.translateToRes, "Bulos"), fontWeight = FontWeight.Bold) }
    AnimatedVisibility(visible = active, enter = fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 5 }) {
        Surface(shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.88f), border = HomeSupportingCardBorder) {
            Column(Modifier.padding(14.dp)) {
                Text("BULOS", color = ForestGreen, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(7.dp))
                Text("Tuntonin", color = ForestGreen, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun DictionaryGuidePreview(active: Boolean, content: DialogueContent) {
    Text(stringResource(content.home.dictionaryTitleRes), color = ForestGreen, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Surface(shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.82f), border = HomeSupportingCardBorder) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_dictionary_book), null, tint = ForestGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(9.dp))
            Text("Rule", color = TranslationInputTextColor, style = MaterialTheme.typography.bodyMedium)
        }
    }
    AnimatedVisibility(visible = active, enter = fadeIn(tween(240))) {
        Surface(shape = RoundedCornerShape(14.dp), color = SoftGreen.copy(alpha = 0.52f)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Rule", color = ForestGreen, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Bulos  •  Tuntonin", color = TranslationInputTextColor, style = MaterialTheme.typography.bodyMedium)
                Text("Filipino  •  Tuntunin", color = TranslationInputTextColor, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SavedGuidePreview(active: Boolean, content: DialogueContent) {
    Text(stringResource(content.home.historyTitleRes), color = ForestGreen, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    GuideHistoryRow(active, saved = true, favorite = true)
    GuideHistoryRow(active, saved = false, favorite = false)
}

@Composable
private fun OfflineGuidePreview(active: Boolean, content: DialogueContent) {
    Text(stringResource(content.home.settingsTitleRes), color = ForestGreen, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Surface(shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.86f), border = HomeSupportingCardBorder) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_lucide_cloud_off), null, tint = ForestGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(9.dp))
                Text(stringResource(content.home.offlineModeRes), modifier = Modifier.weight(1f), color = ForestGreen, fontWeight = FontWeight.Bold)
                Surface(shape = CircleShape, color = if (active) SharedActiveButtonColor else Color.LightGray, modifier = Modifier.size(32.dp)) {}
            }
            GuideResourceRow("Dictionary", true)
            GuideResourceRow("English", true)
            GuideResourceRow("Filipino", false)
        }
    }
}

@Composable
private fun GuideMic(active: Boolean) {
    val scale by animateFloatAsState(if (active) 1.08f else 0.94f, tween(600), label = "guideMic")
    Box(Modifier.fillMaxWidth().height(104.dp), contentAlignment = Alignment.Center) {
        Surface(modifier = Modifier.size(82.dp).graphicsLayer { scaleX = scale; scaleY = scale }, shape = CircleShape, color = SoftGreen.copy(alpha = 0.62f)) {}
        Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = Color.White, contentColor = PrimaryGreen, shadowElevation = 4.dp) {
            Box(contentAlignment = Alignment.Center) { Icon(painterResource(R.drawable.ic_lucide_mic), null, modifier = Modifier.size(28.dp)) }
        }
    }
}

@Composable
private fun GuideFeatureTile(iconRes: Int, text: String, active: Boolean, modifier: Modifier = Modifier) {
    val alpha by animateFloatAsState(if (active) 1f else 0.45f, tween(260), label = "guideTile$text")
    Surface(modifier = modifier.graphicsLayer { this.alpha = alpha }, shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.82f), border = HomeSupportingCardBorder) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(painterResource(iconRes), null, tint = ForestGreen, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(text, color = ForestGreen, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun GuideLanguageStrip() {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.78f), border = HomeSupportingCardBorder) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("English", color = ForestGreen, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = ForestGreen, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Bulos", color = ForestGreen, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun GuideHistoryRow(active: Boolean, saved: Boolean, favorite: Boolean) {
    Surface(shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.82f), border = HomeSupportingCardBorder) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Rule", color = ForestGreen, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Tuntonin", color = TranslationInputTextColor, style = MaterialTheme.typography.bodySmall)
            }
            Icon(painterResource(if (saved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline), null, tint = if (saved && active) ForestGreen else TranslationInputTextColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Icon(if (favorite && active) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (favorite && active) ForestGreen else TranslationInputTextColor, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun GuideResourceRow(label: String, ready: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), color = ForestGreen, style = MaterialTheme.typography.bodyMedium)
        Surface(shape = CircleShape, color = if (ready) SoftGreen else Color(0xFFF4ECDD)) {
            Text(if (ready) "Ready" else "Download", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = if (ready) ForestGreen else TranslationInputTextColor, style = MaterialTheme.typography.labelSmall)
        }
    }
}
