package com.example.bulosfrontend

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.*

@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    val listState = rememberLazyListState()
    Crossfade(
        targetState = viewModel.uiLanguage,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "homeLanguageCrossfade",
    ) { language ->
        HomeScreenContent(
            content = DialogueProvider.getDialogue(language),
            selectedLanguage = viewModel.uiLanguage,
            onLanguageSelected = viewModel::selectUiLanguage,
            onNavigate = onNavigate,
            listState = listState,
        )
    }
}

@Composable
private fun HomeScreenContent(
    content: DialogueContent,
    selectedLanguage: UiLanguage,
    onLanguageSelected: (UiLanguage) -> Unit,
    onNavigate: (String) -> Unit,
    listState: LazyListState,
) {
    val darkTheme = LocalBulosDarkTheme.current
    val labels = content.home
    val features = listOf(
        HomeFeature(labels.speechTitleRes, labels.speechSubtitleRes, painterResource(R.drawable.ic_lucide_mic), SpeechCardGreen, WarmWhiteCard, AppDestinations.VOICE),
        HomeFeature(labels.textTitleRes, labels.textSubtitleRes, painterResource(R.drawable.ic_lucide_languages), TextTranslationCard, GoldenAccent, AppDestinations.TEXT),
        HomeFeature(labels.historyTitleRes, labels.historySubtitleRes, painterResource(R.drawable.ic_lucide_history), DictionaryCard, MainText, AppDestinations.HISTORY),
        HomeFeature(labels.settingsTitleRes, labels.settingsSubtitleRes, painterResource(R.drawable.ic_lucide_settings), HistoryCard, PrimaryGreen, AppDestinations.MORE),
    )

    Box(Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().height(216.dp)) {
            GeometricGreenBackground(
                modifier = Modifier.matchParentSize(),
                cellSize = 52.dp,
                patternAlpha = if (darkTheme) 0.08f else 0.18f,
                backgroundColor = if (darkTheme) DarkHomeHeader else PrimaryGreen,
                patternColor = if (darkTheme) DarkWarmText else PatternGreen,
            )
            if (darkTheme) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    DarkHomeHeaderMid.copy(alpha = 0.18f),
                                    DarkHomeBody.copy(alpha = 0.34f),
                                ),
                            ),
                        ),
                )
            }
        }
        HomeIdentityHeader(
            title = stringResource(content.mainHeaderRes),
            badge = stringResource(labels.homeBadgeRes),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, top = 40.dp, end = 20.dp),
        )
        Surface(
            modifier = Modifier.fillMaxSize().padding(top = 196.dp),
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        ) {}
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 172.dp, end = 20.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            items(2) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    features.subList(rowIndex * 2, rowIndex * 2 + 2).forEach { feature ->
                        FeatureCard(
                            feature = feature,
                            onClick = { onNavigate(feature.route) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(4.dp))
                SupportedLanguagesCard(
                    titleRes = labels.supportedLanguagesRes,
                    descriptionRes = labels.supportedLanguagesDescriptionRes,
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = onLanguageSelected,
                )
            }
            item {
                HomeDynamicContentCard(
                    titleRes = labels.recentDynamicTitleRes,
                    emptyRes = labels.noRecentRes,
                    historyActionRes = labels.viewHistoryRes,
                    onHistoryClick = { onNavigate(AppDestinations.HISTORY) },
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FloatingActionButton(
                onClick = { onNavigate(AppDestinations.DICTIONARY) },
                modifier = Modifier.size(56.dp).shadow(7.dp, CircleShape),
                shape = CircleShape,
                containerColor = ForestGreen,
                contentColor = WarmWhiteCard,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_dictionary_book),
                    contentDescription = stringResource(labels.navDictionaryRes),
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.height(7.dp))
            Text(
                text = stringResource(labels.navDictionaryRes),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
