package com.example.bulosfrontend

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun VoiceGuidedDemoScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val help = viewModel.content.help
    Scaffold(
        containerColor = Color(0xFFFFFBF4),
        topBar = {
            SharedTopAppBar(
                title = stringResource(help.screenTitleRes),
                logoDescriptionRes = help.cardTitleRes,
                iconRes = R.drawable.ic_lucide_circle_help,
                subtitle = stringResource(help.cardSubtitleRes),
                isOnline = viewModel.isOnline,
                isServerReady = viewModel.isServerReady
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(help.messageRes),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onBack) {
                Text(stringResource(help.backRes))
            }
        }
    }
}

@Composable
fun PlaceholderScreen(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    @StringRes descriptionRes: Int,
    @StringRes logoDescriptionRes: Int = R.string.app_logo_content_description,
) {
    Scaffold(topBar = { SharedTopAppBar(stringResource(titleRes), logoDescriptionRes, R.drawable.ic_lucide_book_open) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(stringResource(messageRes), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(descriptionRes),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
