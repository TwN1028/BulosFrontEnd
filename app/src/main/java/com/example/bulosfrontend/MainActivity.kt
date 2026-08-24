package com.example.bulosfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.bulosfrontend.ui.theme.BulosFrontEndTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BulosFrontEndTheme {
                if (!viewModel.isLanguagePreferenceLoaded) {
                    BrandedSplashScreen()
                    return@BulosFrontEndTheme
                }
                val navController = rememberNavController()
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                val bottomNavigationRoutes = setOf(
                    AppDestinations.HOME,
                    AppDestinations.TEXT,
                    AppDestinations.VOICE,
                    AppDestinations.DICTIONARY,
                    AppDestinations.MORE,
                )
                val showBottomNavigation = currentRoute in bottomNavigationRoutes

                fun navigateFromBottomBar(route: String) {
                    when (route) {
                        AppDestinations.TEXT, AppDestinations.VOICE -> navController.navigate(route)
                        else -> navController.navigate(route) {
                            popUpTo(AppDestinations.HOME)
                            launchSingleTop = true
                        }
                    }
                }

                Box(Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = if (viewModel.selectedUiLanguage == null) {
                            AppDestinations.LANGUAGE_SELECTION
                        } else {
                            AppDestinations.HOME
                        },
                        modifier = Modifier.fillMaxSize().padding(bottom = if (showBottomNavigation) 78.dp else 0.dp),
                    ) {
                        composable(AppDestinations.LANGUAGE_SELECTION) {
                            LanguageSelectionScreen(
                                titleRes = DialogueProvider.getDialogue(UiLanguage.ENGLISH).home.selectionTitleRes,
                                selectedLanguage = null,
                                onLanguageSelected = { language ->
                                    viewModel.selectUiLanguage(language)
                                    navController.navigate(AppDestinations.HOME) {
                                        popUpTo(AppDestinations.LANGUAGE_SELECTION) { inclusive = true }
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
                            PlaceholderScreen(
                                viewModel.content.home.dictionaryTitleRes,
                                viewModel.content.home.dictionaryMessageRes,
                                viewModel.content.home.placeholderDescriptionRes,
                                viewModel.content.home.appLogoDescriptionRes,
                            )
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
                    if (showBottomNavigation) {
                        AppBottomNavigation(
                            currentRoute = currentRoute,
                            content = viewModel.content.home,
                            onNavigate = ::navigateFromBottomBar,
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }
            }
        }
    }
}
