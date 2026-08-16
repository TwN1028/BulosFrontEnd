package com.example.bulosfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bulosfrontend.ui.theme.BulosFrontEndTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BulosFrontEndTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "opener") {
                    composable("opener") {
                        OpenerScreen(viewModel) { navController.navigate("home") }
                    }
                    composable("home") {
                        HomeScreen(viewModel) { route -> navController.navigate(route) }
                    }
                    composable("text") {
                        TranslateTextScreen(viewModel, onTranslate = { navController.navigate("result") }) {
                            navController.popBackStack()
                        }
                    }
                    composable("voice") {
                        TranslateVoiceScreen(viewModel, onStop = { navController.navigate("result") }) {
                            navController.popBackStack()
                        }
                    }
                    composable("result") {
                        ResultScreen(viewModel) {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
}
