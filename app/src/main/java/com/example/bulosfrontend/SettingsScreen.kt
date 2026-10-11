package com.example.bulosfrontend

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.annotation.DrawableRes
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.InactiveButtonColor
import com.example.bulosfrontend.ui.theme.SharedActiveButtonColor

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onLanguageClick: () -> Unit,
    onHelpClick: () -> Unit,
    onBack: () -> Unit,
) {
    val content = viewModel.content
    val home = content.home
    Surface(Modifier.fillMaxSize(), color = Color(0xFFFFFBF4)) {
        OverlappingHeaderLayout(
            // The content has 16.dp top padding, leaving a visible 24.dp card overlap.
            overlap = 40.dp,
            modifier = Modifier.fillMaxSize(),
            header = {
            SharedTopAppBar(
                stringResource(home.settingsTitleRes),
                home.appLogoDescriptionRes,
                R.drawable.ic_lucide_settings,
                subtitle = stringResource(home.settingsSubtitleRes),
                onBack = onBack,
            )
            },
        ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding(),
        ) {
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
                    HomeFeatureIcon(
                        painter = painterResource(R.drawable.ic_lucide_language),
                    )
                    Spacer(Modifier.width(12.dp))
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
            SettingsSectionHeader(stringResource(home.networkRes))
            Spacer(Modifier.height(10.dp))
            OfflineModeCard(viewModel)
            Spacer(Modifier.height(24.dp))
            SettingsSectionHeader(stringResource(home.fontSizeRes))
            Spacer(Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth().heightIn(min = 104.dp),
                shape = HomeCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = HomeSupportingCardBorder,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIcon(R.drawable.ic_lucide_type)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(home.fontSizeRes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    FontSizeSelector(
                        selectedFontSize = viewModel.selectedFontSize,
                        smallLabel = stringResource(home.smallFontRes),
                        mediumLabel = stringResource(home.mediumFontRes),
                        largeLabel = stringResource(home.largeFontRes),
                        onFontSizeSelected = viewModel::selectFontSize,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            SettingsSectionHeader(stringResource(home.aboutRes))
            Spacer(Modifier.height(10.dp))
            AboutSettingsCard(
                aboutTitle = stringResource(home.aboutRes),
                version = stringResource(home.versionRes, BuildConfig.VERSION_NAME),
                helpTitle = stringResource(home.helpSupportRes),
                helpSubtitle = stringResource(content.help.cardSubtitleRes),
                onHelpClick = onHelpClick,
            )
        }
        }
    }
}

@Composable
private fun AboutSettingsCard(
    aboutTitle: String,
    version: String,
    helpTitle: String,
    helpSubtitle: String,
    onHelpClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = HomeSupportingCardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            SettingsInfoRow(
                iconRes = R.drawable.ic_lucide_info,
                title = aboutTitle,
                subtitle = version,
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 66.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            SettingsInfoRow(
                iconRes = R.drawable.ic_help_reference,
                title = helpTitle,
                subtitle = helpSubtitle,
                onClick = onHelpClick,
            )
        }
    }
}

@Composable
private fun SettingsInfoRow(
    @DrawableRes iconRes: Int,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(iconRes)
        Spacer(Modifier.width(12.dp))
        SettingsTitleAndSubtitle(title, subtitle, Modifier.weight(1f))
        if (onClick != null) {
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun SettingsIcon(@DrawableRes iconRes: Int) {
    HomeFeatureIcon(painter = painterResource(iconRes))
}

@Composable
private fun SettingsTitleAndSubtitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        modifier = Modifier.padding(horizontal = 4.dp),
        color = MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.labelLarge.copy(
            fontSize = 10.sp,
            letterSpacing = 1.35.sp,
        ),
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun OfflineModeCard(viewModel: MainViewModel) {
    val strings = offlineSpeechResourceStrings(viewModel.uiLanguage)
    val languages = listOf(UiLanguage.ENGLISH, UiLanguage.FILIPINO)
    val hasMissingSpeechResources = languages.any {
        viewModel.offlineSpeechResourceState(it) != OfflineSpeechResourceState.READY
    }
    val downloadInProgress = languages.any {
        viewModel.offlineSpeechResourceState(it) == OfflineSpeechResourceState.DOWNLOADING
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = HomeSupportingCardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SettingsIcon(R.drawable.ic_lucide_cloud_off)
                Spacer(Modifier.width(12.dp))
                SettingsTitleAndSubtitle(
                    title = stringResource(viewModel.content.home.offlineModeRes),
                    subtitle = stringResource(
                        if (viewModel.offlineModeEnabled) strings.activeRes else strings.automaticRes,
                    ),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = viewModel.offlineModeEnabled,
                    onCheckedChange = viewModel::selectOfflineMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HomeFeatureIconTint,
                        uncheckedThumbColor = HomeFeatureIconTint,
                    ),
                )
            }
            if (viewModel.offlineModeEnabled) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f),
                ) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Text(
                            stringResource(strings.resourcesRes),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(12.dp))
                        ResourceStatusRow(
                            label = stringResource(strings.dictionaryRes),
                            status = stringResource(
                                if (viewModel.isDictionaryLoaded) strings.readyRes else strings.notInstalledRes,
                            ),
                            ready = viewModel.isDictionaryLoaded,
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            stringResource(strings.speechRecognitionRes),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            stringResource(strings.availabilityRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        Spacer(Modifier.height(10.dp))
                        languages.forEachIndexed { index, language ->
                            OfflineSpeechModelRow(
                                language = language,
                                sizeMb = viewModel.offlineSpeechModelSizeMb(language),
                                state = viewModel.offlineSpeechResourceState(language),
                                downloadsEnabled = viewModel.isOnline && !downloadInProgress,
                                strings = strings,
                                onDownload = { viewModel.downloadOfflineSpeechModel(language) },
                            )
                            if (index != languages.lastIndex) Spacer(Modifier.height(10.dp))
                        }
                        if (hasMissingSpeechResources) {
                            Spacer(Modifier.height(14.dp))
                            Text(
                                stringResource(strings.downloadResourcesRes),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            if (!viewModel.isOnline) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    stringResource(strings.requiresInternetRes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceStatusRow(label: String, status: String, ready: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(12.dp))
        Text(
            status,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (ready) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun OfflineSpeechModelRow(
    language: UiLanguage,
    sizeMb: Int,
    state: OfflineSpeechResourceState,
    downloadsEnabled: Boolean,
    strings: OfflineSpeechResourceStrings,
    onDownload: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(language.displayNameRes),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = when {
                    state == OfflineSpeechResourceState.DOWNLOADING -> stringResource(strings.downloadingRes)
                    state == OfflineSpeechResourceState.READY -> stringResource(strings.readyRes)
                    state == OfflineSpeechResourceState.FAILED -> stringResource(strings.failedRes)
                    else -> "${stringResource(strings.notInstalledRes)} · $sizeMb MB"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (state == OfflineSpeechResourceState.READY) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                },
            )
        }
        Spacer(Modifier.width(12.dp))
        when {
            state == OfflineSpeechResourceState.DOWNLOADING ->
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            state != OfflineSpeechResourceState.READY -> OutlinedButton(
                onClick = onDownload,
                enabled = downloadsEnabled,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_download),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(
                        if (state == OfflineSpeechResourceState.FAILED) strings.retryRes else strings.downloadRes,
                    ),
                )
            }
        }
    }
}

private data class OfflineSpeechResourceStrings(
    val activeRes: Int,
    val automaticRes: Int,
    val resourcesRes: Int,
    val dictionaryRes: Int,
    val speechRecognitionRes: Int,
    val availabilityRes: Int,
    val readyRes: Int,
    val notInstalledRes: Int,
    val downloadingRes: Int,
    val downloadRes: Int,
    val failedRes: Int,
    val retryRes: Int,
    val downloadResourcesRes: Int,
    val requiresInternetRes: Int,
)

private fun offlineSpeechResourceStrings(language: UiLanguage) = when (language) {
    UiLanguage.FILIPINO -> OfflineSpeechResourceStrings(
        R.string.ui_offline_mode_active_fil,
        R.string.ui_offline_mode_automatic_fil,
        R.string.ui_offline_resources_fil,
        R.string.ui_offline_dictionary_fil,
        R.string.ui_speech_recognition_fil,
        R.string.ui_offline_speech_availability_fil,
        R.string.ui_resource_ready_fil,
        R.string.ui_speech_model_not_installed_fil,
        R.string.ui_speech_model_downloading_fil,
        R.string.ui_speech_model_download_fil,
        R.string.ui_speech_model_failed_fil,
        R.string.ui_speech_model_retry_fil,
        R.string.ui_download_offline_speech_resources_fil,
        R.string.ui_speech_model_requires_internet_fil,
    )
    UiLanguage.BULOS -> OfflineSpeechResourceStrings(
        R.string.ui_offline_mode_active_bul,
        R.string.ui_offline_mode_automatic_bul,
        R.string.ui_offline_resources_bul,
        R.string.ui_offline_dictionary_bul,
        R.string.ui_speech_recognition_bul,
        R.string.ui_offline_speech_availability_bul,
        R.string.ui_resource_ready_bul,
        R.string.ui_speech_model_not_installed_bul,
        R.string.ui_speech_model_downloading_bul,
        R.string.ui_speech_model_download_bul,
        R.string.ui_speech_model_failed_bul,
        R.string.ui_speech_model_retry_bul,
        R.string.ui_download_offline_speech_resources_bul,
        R.string.ui_speech_model_requires_internet_bul,
    )
    UiLanguage.ENGLISH -> OfflineSpeechResourceStrings(
        R.string.ui_offline_mode_active_eng,
        R.string.ui_offline_mode_automatic_eng,
        R.string.ui_offline_resources_eng,
        R.string.ui_offline_dictionary_eng,
        R.string.ui_speech_recognition_eng,
        R.string.ui_offline_speech_availability_eng,
        R.string.ui_resource_ready_eng,
        R.string.ui_speech_model_not_installed_eng,
        R.string.ui_speech_model_downloading_eng,
        R.string.ui_speech_model_download_eng,
        R.string.ui_speech_model_failed_eng,
        R.string.ui_speech_model_retry_eng,
        R.string.ui_download_offline_speech_resources_eng,
        R.string.ui_speech_model_requires_internet_eng,
    )
}

@Composable
private fun FontSizeSelector(
    selectedFontSize: AppFontSize,
    smallLabel: String,
    mediumLabel: String,
    largeLabel: String,
    onFontSizeSelected: (AppFontSize) -> Unit,
) {
    val options = listOf(
        AppFontSize.SMALL to smallLabel,
        AppFontSize.MEDIUM to mediumLabel,
        AppFontSize.LARGE to largeLabel,
    )
    Row(
        modifier = Modifier.fillMaxWidth().height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        options.forEach { (fontSize, label) ->
            ThemeOption(
                label = label,
                selected = selectedFontSize == fontSize,
                onClick = { onFontSizeSelected(fontSize) },
                modifier = Modifier.weight(1f),
            )
        }
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
        color = if (selected) SharedActiveButtonColor else InactiveButtonColor,
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
