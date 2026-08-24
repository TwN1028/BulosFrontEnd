package com.example.bulosfrontend

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(viewModel: MainViewModel, onLanguageClick: () -> Unit) {
    val content = viewModel.content
    val home = content.home
    Scaffold(topBar = { SharedTopAppBar(stringResource(home.settingsTitleRes), home.appLogoDescriptionRes) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(
                onClick = onLanguageClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(home.languageRes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(3.dp))
                        Text(stringResource(home.languageSummaryRes), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.height(3.dp))
                        Text(
                            stringResource(home.currentLanguageRes, stringResource(viewModel.uiLanguage.displayNameRes)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
fun LanguageSettingsScreen(viewModel: MainViewModel, onLanguageSelected: (UiLanguage) -> Unit) {
    val content = viewModel.content
    LanguageSelectionScreen(
        titleRes = content.home.languageScreenTitleRes,
        selectedLanguage = viewModel.selectedUiLanguage,
        onLanguageSelected = onLanguageSelected,
    )
}
