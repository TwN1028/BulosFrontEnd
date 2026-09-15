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
import com.example.bulosfrontend.ui.theme.*

@Composable
fun SettingsScreen(viewModel: MainViewModel, onLanguageClick: () -> Unit) {
    val content = viewModel.content
    val home = content.home
    Scaffold(
        topBar = { SharedTopAppBar(stringResource(home.settingsTitleRes), home.appLogoDescriptionRes, R.drawable.ic_lucide_settings) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(Design.PaddingElement)) {
            Card(
                onClick = onLanguageClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(Design.ElevationDefault),
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(Design.PaddingElement))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(home.languageRes), style = MaterialTheme.typography.titleMedium, fontWeight = Design.FontBold)
                        Spacer(Modifier.height(3.dp))
                        Text(stringResource(home.languageSummaryRes), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.height(3.dp))
                        Text(stringResource(home.currentLanguageRes, stringResource(viewModel.uiLanguage.displayNameRes)), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.secondary)
                }
            }
            Spacer(Modifier.height(Design.PaddingHorizontal))
            Text(stringResource(home.appearanceRes).uppercase(), Modifier.padding(horizontal = Design.PaddingMicro), color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp, letterSpacing = 1.35.sp), fontWeight = Design.FontBold)
            Spacer(Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(Design.ElevationDefault),
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary) {
                        Box(contentAlignment = Alignment.Center) { Icon(if (viewModel.selectedAppTheme == AppTheme.DARK) Icons.Default.DarkMode else Icons.Default.LightMode, null, modifier = Modifier.size(19.dp)) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(home.themeRes), style = MaterialTheme.typography.titleMedium, fontWeight = Design.FontBold)
                        Spacer(Modifier.height(3.dp))
                        Text(stringResource(if (viewModel.selectedAppTheme == AppTheme.DARK) home.darkThemeRes else home.lightThemeRes), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(Modifier.width(10.dp))
                    ThemeSelector(viewModel.selectedAppTheme, stringResource(home.lightThemeRes), stringResource(home.darkThemeRes), viewModel::selectAppTheme)
                }
            }
        }
    }
}

@Composable
private fun ThemeSelector(sel: AppTheme, light: String, dark: String, onSel: (AppTheme) -> Unit) = Row(Modifier.height(34.dp).width(124.dp), Arrangement.spacedBy(4.dp)) {
    ThemeOption(light, sel == AppTheme.LIGHT, { onSel(AppTheme.LIGHT) }, Modifier.weight(1f))
    ThemeOption(dark, sel == AppTheme.DARK, { onSel(AppTheme.DARK) }, Modifier.weight(1f))
}

@Composable
private fun ThemeOption(label: String, sel: Boolean, onClick: () -> Unit, mod: Modifier = Modifier) = Surface(
    modifier = mod.fillMaxHeight().clickable(onClick = onClick), shape = RoundedCornerShape(10.dp),
    color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
    contentColor = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(contentAlignment = Alignment.Center) { Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = Design.FontBold, maxLines = 1) }
}

@Composable
fun LanguageSettingsScreen(viewModel: MainViewModel, onLanguageSelected: (UiLanguage) -> Unit) {
    LanguageSelectionScreen(viewModel.content.home.languageScreenTitleRes, viewModel.selectedUiLanguage, onLanguageSelected)
}
