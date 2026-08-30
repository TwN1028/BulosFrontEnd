package com.example.bulosfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.bulosfrontend.ui.theme.BulosFrontEndTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BulosFrontEndTheme(darkTheme = viewModel.selectedAppTheme == AppTheme.DARK) {
                var minimumSplashDurationElapsed by remember { mutableStateOf(false) }
                val preferenceRepository = remember { LanguagePreferenceRepository(applicationContext) }
                val hasCompletedPreservationIntro by preferenceRepository.hasCompletedPreservationIntro
                    .collectAsState(initial = null)
                val coroutineScope = rememberCoroutineScope()
                LaunchedEffect(Unit) {
                    delay(1_800L)
                    minimumSplashDurationElapsed = true
                }

                if (
                    !viewModel.isLanguagePreferenceLoaded ||
                    !viewModel.isThemePreferenceLoaded ||
                    hasCompletedPreservationIntro == null ||
                    !minimumSplashDurationElapsed
                ) {
                    BrandedSplashScreen()
                    return@BulosFrontEndTheme
                }
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val featureRoutes = setOf(
                    AppDestinations.VOICE,
                    AppDestinations.TEXT,
                    AppDestinations.HISTORY,
                    AppDestinations.DICTIONARY,
                    AppDestinations.MORE,
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (currentRoute in featureRoutes) {
                            AppBottomNavigation(
                                currentRoute = currentRoute,
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
                            HomeScreen(viewModel) { route -> navController.navigate(route) }
                        }
                        composable(AppDestinations.TEXT) {
                            TranslateTextScreen(
                                viewModel,
                                onTranslate = { navController.navigate(AppDestinations.RESULT) },
                            ) {
                                navController.popBackStack()
                            }
                        }
                        composable(AppDestinations.VOICE) {
                            TranslateVoiceScreen(
                                viewModel,
                                onTranslate = { navController.navigate(AppDestinations.RESULT) },
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
}
