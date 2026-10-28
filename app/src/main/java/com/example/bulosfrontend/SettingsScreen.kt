package com.example.bulosfrontend

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.annotation.DrawableRes
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.InactiveButtonColor
import com.example.bulosfrontend.ui.theme.SharedActiveButtonColor
import com.example.bulosfrontend.ui.theme.WarmWhite

private val OfflineModeCardBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFEFC), Color(0xFFFFFAF2)),
)
private val OfflineResourcesBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFDCE9CF),
        Color(0xFFEEF2D9),
        Color(0xFFFFF7E7),
    ),
)
private val OfflineResourceTextColor = Color(0xFF10492F)
private val OfflineResourceSupportingColor = Color(0xFF765F48)

internal enum class OfflineResourceNotice {
    ALL_READY,
    PARTIALLY_AVAILABLE,
    MISSING,
    DOWNLOADING,
    FAILED,
}

internal fun offlineResourceNotice(
    isDictionaryReady: Boolean,
    speechStates: Collection<OfflineSpeechResourceState>,
): OfflineResourceNotice = when {
    speechStates.any { it == OfflineSpeechResourceState.FAILED } -> OfflineResourceNotice.FAILED
    speechStates.any { it == OfflineSpeechResourceState.DOWNLOADING } -> OfflineResourceNotice.DOWNLOADING
    isDictionaryReady && speechStates.all { it == OfflineSpeechResourceState.READY } ->
        OfflineResourceNotice.ALL_READY
    isDictionaryReady || speechStates.any { it == OfflineSpeechResourceState.READY } ->
        OfflineResourceNotice.PARTIALLY_AVAILABLE
    else -> OfflineResourceNotice.MISSING
}

internal fun offlineResourceNoticeText(notice: OfflineResourceNotice, language: UiLanguage): String = when (notice) {
    OfflineResourceNotice.ALL_READY -> when (language) {
        UiLanguage.FILIPINO -> "Lahat ng kinakailangang offline resource ay na-download na."
        UiLanguage.BULOS -> "Pesan a kailangan a offline resource ay na-download di."
        UiLanguage.ENGLISH -> "All required offline resources are downloaded and ready."
    }
    OfflineResourceNotice.PARTIALLY_AVAILABLE -> when (language) {
        UiLanguage.FILIPINO -> "Maaaring mag-download ng karagdagang speech model para sa iba pang wika."
        UiLanguage.BULOS -> "Pede mag-download ni karagdagan a speech model para de kakmukan a sorot."
        UiLanguage.ENGLISH -> "Some offline resources are ready. You can download additional speech models below."
    }
    OfflineResourceNotice.MISSING -> when (language) {
        UiLanguage.FILIPINO -> "Kailangan ng internet upang ma-download ang mga offline speech model."
        UiLanguage.BULOS -> "Kailangan ni internet para ma-download i mga offline speech model."
        UiLanguage.ENGLISH -> "Offline speech models are not downloaded. Connect to internet to download them."
    }
    OfflineResourceNotice.DOWNLOADING -> when (language) {
        UiLanguage.FILIPINO -> "Nagda-download ng offline resource..."
        UiLanguage.BULOS -> "Ge-download ni offline resource..."
        UiLanguage.ENGLISH -> "Downloading offline resources..."
    }
    OfflineResourceNotice.FAILED -> when (language) {
        UiLanguage.FILIPINO -> "Hindi natuloy ang pag-download. Subukang muli."
        UiLanguage.BULOS -> "Eyen natuloy i pag-download. Peliwetin muli."
        UiLanguage.ENGLISH -> "Download failed. Please check your internet connection and try again."
    }
}

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onLanguageClick: () -> Unit,
    onHelpClick: () -> Unit,
    onBack: () -> Unit,
) {
    val content = viewModel.content
    val home = content.home
    val scrollState = rememberScrollState()
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
                .sharedHeaderScrollFade(scrollState.value, overlap = 40.dp)
                .verticalScroll(scrollState)
                .padding(16.dp)
                .navigationBarsPadding(),
        ) {
            Card(
                onClick = onLanguageClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
                shape = HomeCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = HomeSupportingCardBorder,
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
                        Text(stringResource(home.languageSummaryRes), style = MaterialTheme.typography.bodyMedium, color = TranslationInputTextColor)
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
            SettingsSectionHeader("Workflow & Translation Settings")
            Spacer(Modifier.height(10.dp))
            WorkflowSettingsCard(viewModel)
            Spacer(Modifier.height(24.dp))
            SettingsSectionHeader(stringResource(appearanceSectionRes(viewModel.uiLanguage)))
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

private fun appearanceSectionRes(language: UiLanguage): Int = when (language) {
    UiLanguage.ENGLISH -> R.string.ui_appearance_eng
    UiLanguage.FILIPINO -> R.string.ui_appearance_fil
    UiLanguage.BULOS -> R.string.ui_appearance_bul
}

@Composable
private fun WorkflowSettingsCard(viewModel: MainViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = HomeSupportingCardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SettingsIcon(R.drawable.ic_lucide_mic)
                Spacer(Modifier.width(12.dp))
                SettingsTitleAndSubtitle(
                    title = "Skip transcription screen",
                    subtitle = "Go straight from recording to output screen",
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = viewModel.skipTranscriptionScreen,
                    onCheckedChange = viewModel::selectSkipTranscriptionScreen,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = WarmWhite,
                        checkedTrackColor = SharedActiveButtonColor,
                        checkedBorderColor = SharedActiveButtonColor,
                        uncheckedThumbColor = WarmWhite,
                        uncheckedTrackColor = Color(0xFFF1E8DA),
                        uncheckedBorderColor = Color(0xFFE5DDD2).copy(alpha = 0.65f),
                    ),
                )
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFE5DDD2).copy(alpha = 0.65f))
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SettingsIcon(R.drawable.ic_saved_history_reference)
                Spacer(Modifier.width(12.dp))
                SettingsTitleAndSubtitle(
                    title = "Save entries by default",
                    subtitle = "Automatically save translation history entries",
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = viewModel.autoSaveEnabled,
                    onCheckedChange = viewModel::selectAutoSaveEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = WarmWhite,
                        checkedTrackColor = SharedActiveButtonColor,
                        checkedBorderColor = SharedActiveButtonColor,
                        uncheckedThumbColor = WarmWhite,
                        uncheckedTrackColor = Color(0xFFF1E8DA),
                        uncheckedBorderColor = Color(0xFFE5DDD2).copy(alpha = 0.65f),
                    ),
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
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = TranslationInputTextColor,
) {
    Column(modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = titleColor,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = subtitleColor,
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
internal fun OfflineModeCard(viewModel: MainViewModel) {
    val strings = offlineSpeechResourceStrings(viewModel.uiLanguage)
    val languages = listOf(UiLanguage.ENGLISH, UiLanguage.FILIPINO)
    val speechStates = languages.map(viewModel::offlineSpeechResourceState)
    val downloadInProgress = languages.any {
        viewModel.offlineSpeechResourceState(it) == OfflineSpeechResourceState.DOWNLOADING
    }
    val resourceNotice = offlineResourceNotice(
        isDictionaryReady = viewModel.isDictionaryLoaded,
        speechStates = speechStates,
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = HomeSupportingCardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(OfflineModeCardBrush)
                .padding(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SettingsIcon(R.drawable.ic_lucide_cloud_off)
                Spacer(Modifier.width(12.dp))
                SettingsTitleAndSubtitle(
                    title = stringResource(viewModel.content.home.offlineModeRes),
                    subtitle = stringResource(viewModel.content.home.offlineModeSummaryRes),
                    modifier = Modifier.weight(1f),
                    titleColor = OfflineResourceTextColor,
                    subtitleColor = TranslationInputTextColor,
                )
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = viewModel.offlineModeEnabled,
                    onCheckedChange = viewModel::selectOfflineMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = WarmWhite,
                        checkedTrackColor = SharedActiveButtonColor,
                        checkedBorderColor = SharedActiveButtonColor,
                        uncheckedThumbColor = WarmWhite,
                        uncheckedTrackColor = Color(0xFFF1E8DA),
                        uncheckedBorderColor = Color(0xFFE5DDD2).copy(alpha = 0.65f),
                    ),
                )
            }
            if (viewModel.offlineModeEnabled) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(OfflineResourcesBrush)
                            .padding(14.dp),
                    ) {
                        Text(
                            stringResource(strings.resourcesRes),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = OfflineResourceTextColor,
                        )
                        Spacer(Modifier.height(14.dp))
                        ResourceStatusRow(
                            label = stringResource(strings.dictionaryRes),
                            status = stringResource(
                                if (viewModel.isDictionaryLoaded) strings.readyRes else strings.notInstalledRes,
                            ),
                            ready = viewModel.isDictionaryLoaded,
                            textColor = OfflineResourceTextColor,
                            supportingColor = OfflineResourceSupportingColor,
                        )
                        Spacer(Modifier.height(16.dp))
                        OfflineModeDivider()
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(strings.speechRecognitionRes),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = OfflineResourceTextColor,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(strings.availabilityRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = OfflineResourceSupportingColor,
                        )
                        Spacer(Modifier.height(14.dp))
                        languages.forEachIndexed { index, language ->
                            OfflineSpeechModelRow(
                                language = language,
                                sizeMb = viewModel.offlineSpeechModelSizeMb(language),
                                state = viewModel.offlineSpeechResourceState(language),
                                downloadsEnabled = viewModel.isOnline && !downloadInProgress,
                                strings = strings,
                                onDownload = { viewModel.downloadOfflineSpeechModel(language) },
                                textColor = OfflineResourceTextColor,
                                supportingColor = OfflineResourceSupportingColor,
                            )
                            if (index != languages.lastIndex) Spacer(Modifier.height(10.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        OfflineDownloadNotice(
                            text = offlineResourceNoticeText(resourceNotice, viewModel.uiLanguage),
                            textColor = OfflineResourceSupportingColor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineModeDivider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = OfflineResourceTextColor.copy(alpha = 0.14f),
    )
}

@Composable
private fun ResourceStatusRow(
    label: String,
    status: String,
    ready: Boolean,
    textColor: Color,
    supportingColor: Color,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
        )
        Spacer(Modifier.width(12.dp))
        if (ready) {
            ReadyResourceStatus(status, textColor)
        } else {
            Text(
                status,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = supportingColor,
            )
        }
    }
}

@Composable
private fun ReadyResourceStatus(label: String, textColor: Color) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFEAF1D8),
        contentColor = OfflineResourceTextColor,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}

@Composable
private fun OfflineDownloadNotice(text: String, textColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFFFF7E7).copy(alpha = 0.78f),
        border = BorderStroke(0.5.dp, OfflineResourceTextColor.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_lucide_info),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        }
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
    textColor: Color,
    supportingColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(language.displayNameRes),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
            )
        }
        if (state == OfflineSpeechResourceState.READY) {
            Spacer(Modifier.width(12.dp))
            ReadyResourceStatus(stringResource(strings.readyRes), textColor)
        }
    }
    if (state != OfflineSpeechResourceState.READY) {
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            when (state) {
                OfflineSpeechResourceState.DOWNLOADING -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = SharedActiveButtonColor,
                    strokeWidth = 2.dp,
                )
                OfflineSpeechResourceState.NOT_DOWNLOADED,
                OfflineSpeechResourceState.FAILED -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "$sizeMb MB",
                        style = MaterialTheme.typography.labelSmall,
                        color = OfflineResourceSupportingColor,
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onDownload,
                        enabled = downloadsEnabled,
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = OfflineResourceSupportingColor,
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFFE3D1BE)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_download),
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            stringResource(
                                if (state == OfflineSpeechResourceState.FAILED) strings.retryRes else strings.downloadRes,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

private data class OfflineSpeechResourceStrings(
    val resourcesRes: Int,
    val dictionaryRes: Int,
    val speechRecognitionRes: Int,
    val availabilityRes: Int,
    val readyRes: Int,
    val notInstalledRes: Int,
    val downloadRes: Int,
    val retryRes: Int,
)

private fun offlineSpeechResourceStrings(language: UiLanguage) = when (language) {
    UiLanguage.FILIPINO -> OfflineSpeechResourceStrings(
        R.string.ui_offline_resources_fil,
        R.string.ui_offline_dictionary_fil,
        R.string.ui_speech_recognition_fil,
        R.string.ui_offline_speech_availability_fil,
        R.string.ui_resource_ready_fil,
        R.string.ui_speech_model_not_installed_fil,
        R.string.ui_speech_model_download_fil,
        R.string.ui_speech_model_retry_fil,
    )
    UiLanguage.BULOS -> OfflineSpeechResourceStrings(
        R.string.ui_offline_resources_bul,
        R.string.ui_offline_dictionary_bul,
        R.string.ui_speech_recognition_bul,
        R.string.ui_offline_speech_availability_bul,
        R.string.ui_resource_ready_bul,
        R.string.ui_speech_model_not_installed_bul,
        R.string.ui_speech_model_download_bul,
        R.string.ui_speech_model_retry_bul,
    )
    UiLanguage.ENGLISH -> OfflineSpeechResourceStrings(
        R.string.ui_offline_resources_eng,
        R.string.ui_offline_dictionary_eng,
        R.string.ui_speech_recognition_eng,
        R.string.ui_offline_speech_availability_eng,
        R.string.ui_resource_ready_eng,
        R.string.ui_speech_model_not_installed_eng,
        R.string.ui_speech_model_download_eng,
        R.string.ui_speech_model_retry_eng,
    )
}

@Composable
internal fun FontSizeSelector(
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
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
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
        modifier = modifier
            .fillMaxHeight()
            .defaultMinSize(minHeight = 40.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) SharedActiveButtonColor else InactiveButtonColor,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        border = if (selected) null else BorderStroke(1.dp, TranslationInputBorderColor),
        shadowElevation = if (selected) 4.dp else 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                textAlign = TextAlign.Center,
                maxLines = 2,
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
