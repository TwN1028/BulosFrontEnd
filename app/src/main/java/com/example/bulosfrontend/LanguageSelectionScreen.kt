package com.example.bulosfrontend

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.Cream

private data class LanguageOption(val language: UiLanguage, @StringRes val promptRes: Int)

private val languageOptions = listOf(
    LanguageOption(UiLanguage.ENGLISH, R.string.language_option_english),
    LanguageOption(UiLanguage.FILIPINO, R.string.language_option_filipino),
    LanguageOption(UiLanguage.BULOS, R.string.language_option_bulos),
)

@Composable
fun LanguageSelectionScreen(
    @StringRes titleRes: Int,
    selectedLanguage: UiLanguage?,
    onLanguageSelected: (UiLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = Cream) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            item {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    stringResource(titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(28.dp))
            }
            items(languageOptions) { option ->
                LanguageOptionCard(
                    option = option,
                    selected = selectedLanguage == option.language,
                    onClick = { onLanguageSelected(option.language) },
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LanguageOptionCard(option: LanguageOption, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 84.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp), contentAlignment = Alignment.CenterStart) {
            Text(
                stringResource(option.promptRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
