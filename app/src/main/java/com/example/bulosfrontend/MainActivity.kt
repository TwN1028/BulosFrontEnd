package com.example.bulosfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.example.bulosfrontend.ui.theme.BulosFrontEndTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val target = intent.getStringExtra("target_destination")?.let {
            try { AppDestinations.valueOf(it) } catch (_: Exception) { AppDestinations.HOME }
        } ?: AppDestinations.HOME

        setContent {
            BulosFrontEndTheme {
                BulosFrontEndApp(initialDestination = target)
            }
        }
    }
}

@Preview(name = "Phone", device = Devices.PIXEL_7, showSystemUi = true, showBackground = true)
@Composable
fun BulosFrontEndApp(initialDestination: AppDestinations = AppDestinations.HOME) {
    var currentDestination by rememberSaveable { mutableStateOf(initialDestination) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = stringResource(it.labelRes),
                        )
                    },
                    label = { Text(stringResource(it.labelRes)) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it },
                )
            }
        },
    ) {
        Scaffold(
            topBar = { BulosTopAppBar() },
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            Greeting(
                name = stringResource(R.string.default_name),
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulosTopAppBar() {
    TopAppBar(
        title = { Text(stringResource(R.string.app_header_title)) },
        navigationIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.app_logo_content_description),
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp),
            )
        },
    )
}

enum class AppDestinations(
    val labelRes: Int,
    val icon: Int,
) {
    HOME(R.string.nav_home, R.drawable.ic_home),
    FAVORITES(R.string.nav_favorites, R.drawable.ic_favorite),
    PROFILE(R.string.nav_profile, R.drawable.ic_account_box),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.greeting_format, name),
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BulosFrontEndTheme {
        Greeting(stringResource(R.string.default_name))
    }
}
