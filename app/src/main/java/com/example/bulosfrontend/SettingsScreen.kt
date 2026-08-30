package com.example.bulosfrontend

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(viewModel: MainViewModel, onLanguageClick: () -> Unit) {
    val content = viewModel.content
    val home = content.home
    Scaffold(
        topBar = {
            SharedTopAppBar(
                stringResource(home.settingsTitleRes),
                home.appLogoDescriptionRes,
                R.drawable.ic_lucide_settings,
            )
        },
    ) { padding ->
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
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(home.appearanceRes).uppercase(),
                modifier = Modifier.padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 10.sp,
                    letterSpacing = 1.35.sp,
                ),
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (viewModel.selectedAppTheme == AppTheme.DARK) {
                                    Icons.Default.DarkMode
                                } else {
                                    Icons.Default.LightMode
                                },
                                contentDescription = null,
                                modifier = Modifier.size(19.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(home.themeRes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = stringResource(
                                if (viewModel.selectedAppTheme == AppTheme.DARK) home.darkThemeRes else home.lightThemeRes,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    ThemeSelector(
                        selectedTheme = viewModel.selectedAppTheme,
                        lightLabel = stringResource(home.lightThemeRes),
                        darkLabel = stringResource(home.darkThemeRes),
                        onThemeSelected = viewModel::selectAppTheme,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSelector(
    selectedTheme: AppTheme,
    lightLabel: String,
    darkLabel: String,
    onThemeSelected: (AppTheme) -> Unit,
) {
    Row(
        modifier = Modifier
            .height(34.dp)
            .width(124.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ThemeOption(
            label = lightLabel,
            selected = selectedTheme == AppTheme.LIGHT,
            onClick = { onThemeSelected(AppTheme.LIGHT) },
            modifier = Modifier.weight(1f),
        )
        ThemeOption(
            label = darkLabel,
            selected = selectedTheme == AppTheme.DARK,
            onClick = { onThemeSelected(AppTheme.DARK) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxHeight().clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
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
