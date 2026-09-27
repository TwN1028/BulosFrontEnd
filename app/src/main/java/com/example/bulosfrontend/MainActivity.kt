package com.example.bulosfrontend

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.example.bulosfrontend.ui.theme.BulosFrontEndTheme
import com.example.bulosfrontend.ui.theme.LocalBulosDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                darkTheme = viewModel.selectedAppTheme == AppTheme.DARK,
                fontScale = viewModel.selectedFontSize.scaleFactor,
            ) {
                SideEffect { applyWhiteStatusBarContent() }
                var minimumSplashDurationElapsed by remember { mutableStateOf(false) }
                val preferenceRepository = remember { LanguagePreferenceRepository(applicationContext) }
                val hasCompletedPreservationIntro by preferenceRepository.hasCompletedPreservationIntro
                    .collectAsState(initial = null)
                val coroutineScope = rememberCoroutineScope()
                LaunchedEffect(Unit) {
                    delay(3_000L)
                    minimumSplashDurationElapsed = true
                }

                if (
                    !viewModel.isLanguagePreferenceLoaded ||
                    !viewModel.isThemePreferenceLoaded ||
                    !viewModel.isFontSizePreferenceLoaded ||
                    hasCompletedPreservationIntro == null ||
                    !minimumSplashDurationElapsed
                ) {
                    BrandedSplashScreen()
                    return@BulosFrontEndTheme
                }
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                var resultOriginRoute by remember { mutableStateOf<String?>(null) }
                var showBottomNavigation by remember { mutableStateOf(false) }
                var displayedBottomRoute by remember { mutableStateOf<String?>(null) }
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
                        displayedBottomRoute = currentRoute
                        if (!showBottomNavigation) {
                            delay(260L)
                            showBottomNavigation = true
                        }
                    } else {
                        showBottomNavigation = false
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
                val darkTheme = LocalBulosDarkTheme.current

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = if (darkTheme) {
                        MaterialTheme.colorScheme.background
                    } else {
                        HomeContentCream
                    },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showBottomNavigation,
                            enter = fadeIn(
                                tween(320, easing = FastOutSlowInEasing),
                            ) + slideInVertically(
                                tween(360, easing = FastOutSlowInEasing),
                                initialOffsetY = { it / 6 },
                            ) + expandVertically(
                                tween(360, easing = FastOutSlowInEasing),
                                expandFrom = Alignment.Bottom,
                            ),
                            exit = fadeOut(
                                tween(300, easing = FastOutSlowInEasing),
                            ) + slideOutVertically(
                                tween(340, easing = FastOutSlowInEasing),
                                targetOffsetY = { it / 6 },
                            ) + shrinkVertically(
                                tween(360, easing = FastOutSlowInEasing),
                                shrinkTowards = Alignment.Bottom,
                            ),
                        ) {
                            val activeFunctionRoute = when (displayedBottomRoute) {
                                AppDestinations.HOME_RECORDING,
                                AppDestinations.VOICE,
                                -> AppDestinations.HOME_RECORDING

                                AppDestinations.RESULT -> resultOriginRoute

                                else -> displayedBottomRoute
                            }
                            AppBottomNavigation(
                                currentRoute = displayedBottomRoute,
                                activeFunctionRoute = activeFunctionRoute,
                                content = viewModel.content.home,
                                onNavigate = { route ->
                                    if (route != currentRoute) {
                                        navController.navigate(route) {
                                            launchSingleTop = true
                                            restoreState = true
                                            popUpTo(AppDestinations.HOME) {
                                                saveState = true
                                            }
                                        }
                                    }
                                },
                            )
                        }
                    },
                ) { navigationPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = when {
                            viewModel.selectedUiLanguage == null -> AppDestinations.LANGUAGE_SELECTION
                            hasCompletedPreservationIntro == false -> AppDestinations.PRESERVATION_INTRO
                            else -> AppDestinations.HOME
                        },
                        modifier = Modifier.fillMaxSize().padding(navigationPadding),
                    ) {
                        composable(AppDestinations.LANGUAGE_SELECTION) {
                            LanguageSelectionScreen(
                                titleRes = DialogueProvider.getDialogue(UiLanguage.ENGLISH).home.selectionTitleRes,
                                selectedLanguage = null,
                                onLanguageSelected = { language ->
                                    viewModel.selectUiLanguage(language)
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
                                onHome = {
                                    navController.navigate(AppDestinations.HOME) {
                                        launchSingleTop = true
                                        popUpTo(AppDestinations.HOME)
                                    }
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
                        composable(AppDestinations.DICTIONARY) {
                            DictionaryScreen(viewModel)
                        }
                        composable(AppDestinations.MORE) {
                            SettingsScreen(viewModel) { navController.navigate(AppDestinations.LANGUAGE_SETTINGS) }
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
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyWhiteStatusBarContent()
    }

    private fun applyWhiteStatusBarContent() {
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
    }
}
