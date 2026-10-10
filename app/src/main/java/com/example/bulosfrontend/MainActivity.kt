package com.example.bulosfrontend

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.example.bulosfrontend.ui.theme.BulosFrontEndTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val StartupProgressCompletionHoldMillis = 1_000L

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        applyWhiteStatusBarContent()
        setContent {
            BulosFrontEndTheme(
                fontScale = viewModel.selectedFontSize.scaleFactor,
            ) {
                SideEffect { applyWhiteStatusBarContent() }
                val preferenceRepository = remember { LanguagePreferenceRepository(applicationContext) }
                val coroutineScope = rememberCoroutineScope()
                var showStartupSplash by remember { mutableStateOf(true) }
                var isStartupProgressComplete by remember { mutableStateOf(false) }
                LaunchedEffect(
                    TranslationState.sourceLanguage,
                    viewModel.isOfflineModePreferenceLoaded,
                    viewModel.offlineModeEnabled,
                ) {
                    if (viewModel.isOfflineModePreferenceLoaded) {
                        viewModel.preloadOfflineSpeechModel(TranslationState.sourceLanguage)
                    }
                }
                LaunchedEffect(viewModel.isStartupReady) {
                    if (!viewModel.isStartupReady) {
                        isStartupProgressComplete = false
                    }
                }
                LaunchedEffect(viewModel.isStartupReady, isStartupProgressComplete) {
                    if (viewModel.isStartupReady && isStartupProgressComplete) {
                        withFrameNanos { }
                        delay(StartupProgressCompletionHoldMillis)
                        showStartupSplash = false
                    }
                }
                val splashProgress = if (viewModel.isStartupReady) {
                    1f
                } else {
                    viewModel.startupCompletedTaskCount.toFloat() / viewModel.startupTaskCount
                }
                val hasCompletedPreservationIntro = viewModel.hasCompletedPreservationIntro == true
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                var resultOriginRoute by remember { mutableStateOf<String?>(null) }
                var showFloatingNavigation by remember { mutableStateOf(false) }
                var textScreenAllowsFloatingNavigation by remember { mutableStateOf(true) }
                var displayedNavigationRoute by remember { mutableStateOf<String?>(null) }
                val dictionaryFabProgress = remember { Animatable(0f) }
                val featureRoutes = setOf(
                    AppDestinations.VOICE,
                    AppDestinations.TEXT,
                    AppDestinations.RESULT,
                    AppDestinations.HISTORY,
                    AppDestinations.DICTIONARY,
                    AppDestinations.MORE,
                )
                LaunchedEffect(currentRoute) {
                    if (currentRoute in featureRoutes) {
                        displayedNavigationRoute = currentRoute
                        if (!showFloatingNavigation) {
                            delay(260L)
                            showFloatingNavigation = true
                        }
                    } else {
                        showFloatingNavigation = false
                    }
                }
                LaunchedEffect(currentRoute) {
                    if (currentRoute == AppDestinations.HOME) {
                        if (dictionaryFabProgress.value == 0f) delay(260L)
                        dictionaryFabProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = 320,
                                easing = FastOutSlowInEasing,
                            ),
                        )
                    } else {
                        dictionaryFabProgress.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(
                                durationMillis = 260,
                                easing = FastOutSlowInEasing,
                            ),
                        )
                    }
                }
                Box(Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = viewModel.isStartupReady,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None,
                    modifier = Modifier.fillMaxSize(),
                ) {
                CompositionLocalProvider(
                    LocalConnectionStatus provides viewModel.connectionStatus,
                    LocalConnectionUiLanguage provides viewModel.uiLanguage,
                ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = HomeContentCream,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                ) { contentPadding ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                    ) {
                        NavHost(
                        navController = navController,
                        startDestination = when {
                            viewModel.selectedUiLanguage == null -> AppDestinations.LANGUAGE_SELECTION
                            hasCompletedPreservationIntro == false -> AppDestinations.PRESERVATION_INTRO
                            else -> AppDestinations.HOME
                        },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        composable(AppDestinations.LANGUAGE_SELECTION) {
                            LanguageSelectionScreen(
                                titleRes = DialogueProvider.getDialogue(UiLanguage.ENGLISH).home.selectionTitleRes,
                                selectedLanguage = null,
                                onLanguageSelected = { language ->
                                    viewModel.selectHomeUiLanguage(language)
                                    navController.navigate(AppDestinations.PRESERVATION_INTRO) {
                                        popUpTo(AppDestinations.LANGUAGE_SELECTION) { inclusive = true }
                                    }
                                },
                                confirmationRequired = true,
                            )
                        }
                        composable(AppDestinations.PRESERVATION_INTRO) {
                            PreservationIntroScreen(
                                content = viewModel.content.preservationIntro,
                                onContinue = {
                                    coroutineScope.launch {
                                        preferenceRepository.markPreservationIntroCompleted()
                                        navController.navigate(AppDestinations.HOME) {
                                            launchSingleTop = true
                                            popUpTo(AppDestinations.PRESERVATION_INTRO) { inclusive = true }
                                        }
                                    }
                                },
                            )
                        }
                        composable(AppDestinations.HOME) {
                            HomeScreen(
                                viewModel = viewModel,
                                dictionaryFabProgress = dictionaryFabProgress.value,
                            ) { route ->
                                navController.navigate(route) { launchSingleTop = true }
                            }
                        }
                        composable(
                            route = AppDestinations.HOME_RECORDING,
                            enterTransition = {
                                fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.96f)
                            },
                            exitTransition = {
                                fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.96f)
                            },
                            popEnterTransition = {
                                fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.96f)
                            },
                            popExitTransition = {
                                fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.96f)
                            },
                        ) {
                            HomeRecordingScreen(
                                viewModel = viewModel,
                                onBack = {
                                    navController.navigate(AppDestinations.HOME) {
                                        launchSingleTop = true
                                        popUpTo(AppDestinations.HOME_RECORDING) { inclusive = true }
                                    }
                                },
                                onRecordingFinished = {
                                    navController.navigate(AppDestinations.VOICE) {
                                        launchSingleTop = true
                                        popUpTo(AppDestinations.HOME_RECORDING) { inclusive = true }
                                    }
                                },
                            )
                        }
                        composable(
                            route = AppDestinations.TEXT,
                            enterTransition = {
                                fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.96f)
                            },
                            exitTransition = {
                                fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.96f)
                            },
                            popEnterTransition = {
                                fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.96f)
                            },
                            popExitTransition = {
                                fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.96f)
                            },
                        ) {
                            TranslateTextScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onFloatingNavigationVisibilityChanged = {
                                    textScreenAllowsFloatingNavigation = it
                                },
                            )
                        }
                        composable(AppDestinations.VOICE) {
                            TranslateVoiceScreen(
                                viewModel,
                                onTranslate = {
                                    resultOriginRoute = AppDestinations.HOME_RECORDING
                                    navController.navigate(AppDestinations.RESULT)
                                },
                            ) {
                                viewModel.clearVoiceDraft()
                                navController.popBackStack()
                            }
                        }
                        composable(AppDestinations.RESULT) {
                            ResultScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onTranslateAgain = { navController.popBackStack() },
                            )
                        }
                        composable(AppDestinations.HISTORY) {
                            HistoryScreen(viewModel) { navController.popBackStack() }
                        }
                        composable(AppDestinations.RECENT) {
                            RecentTranslationScreen(viewModel) { navController.popBackStack() }
                        }
                        composable(AppDestinations.DICTIONARY) {
                            DictionaryScreen(viewModel) { navController.popBackStack() }
                        }
                        composable(AppDestinations.MORE) {
                            SettingsScreen(
                                viewModel = viewModel,
                                onLanguageClick = {
                                    navController.navigate(AppDestinations.LANGUAGE_SETTINGS)
                                },
                                onHelpClick = {
                                    navController.navigate(AppDestinations.HELP)
                                },
                                onBack = { navController.popBackStack() },
                            )
                        }
                        composable(AppDestinations.HELP) {
                            HelpOnboardingScreen(viewModel, onBack = { navController.popBackStack() })
                        }
                        composable(AppDestinations.LANGUAGE_SETTINGS) {
                            LanguageSettingsScreen(viewModel) { language ->
                                viewModel.selectUiLanguage(language)
                                navController.popBackStack()
                            }
                        }
                        }

                        if (
                            showFloatingNavigation &&
                            (currentRoute != AppDestinations.TEXT || textScreenAllowsFloatingNavigation)
                        ) {
                            val activeFunctionRoute = when (displayedNavigationRoute) {
                                AppDestinations.HOME_RECORDING,
                                AppDestinations.VOICE,
                                -> AppDestinations.HOME_RECORDING

                                AppDestinations.RESULT -> resultOriginRoute
                                else -> displayedNavigationRoute
                            }
                            AppFloatingNavigation(
                                currentRoute = displayedNavigationRoute,
                                activeFunctionRoute = activeFunctionRoute,
                                content = viewModel.content.home,
                                onNavigate = { route ->
                                    if (route != currentRoute) {
                                        navController.navigate(route) {
                                            launchSingleTop = true
                                            restoreState = true
                                            popUpTo(AppDestinations.HOME) { saveState = true }
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
                }
                }
                AnimatedVisibility(
                    visible = showStartupSplash,
                    enter = EnterTransition.None,
                    exit = fadeOut(tween(durationMillis = 350)),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    BrandedSplashScreen(
                        progress = splashProgress,
                        errorMessage = viewModel.startupError,
                        onRetry = viewModel::retryStartup,
                        onProgressCompleted = { isStartupProgressComplete = true },
                    )
                }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyWhiteStatusBarContent()
        viewModel.refreshConnectionStatus()
    }

    private fun applyWhiteStatusBarContent() {
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
    }
}
