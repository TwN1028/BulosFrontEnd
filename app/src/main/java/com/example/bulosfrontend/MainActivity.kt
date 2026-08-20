package com.example.bulosfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.*
import com.example.bulosfrontend.ui.theme.BulosFrontEndTheme

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BulosFrontEndTheme {
                val nav = rememberNavController()
                NavHost(nav, "opener") {
                    composable("opener") { OpenerScreen(vm) { nav.navigate("home") } }
                    composable("home") { HomeScreen(vm) { nav.navigate(it) } }
                    composable("text") { TranslateTextScreen(vm, { nav.navigate("result") }) { nav.popBackStack() } }
                    composable("voice") { TranslateVoiceScreen(vm, { nav.navigate("result") }) { nav.popBackStack() } }
                    composable("result") { ResultScreen(vm) { nav.navigate("home") { popUpTo("home") { inclusive = true } } } }
                    composable("history") { HistoryScreen(vm) { nav.popBackStack() } }
                }
            }
        }
    }
}
