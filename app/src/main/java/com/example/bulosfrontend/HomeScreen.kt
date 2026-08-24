package com.example.bulosfrontend

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.*

@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    val content = viewModel.content
    val labels = content.home
    val features = listOf(
        HomeFeature(labels.speechTitleRes, labels.speechSubtitleRes, painterResource(R.drawable.ic_lucide_mic), SpeechCardGreen, WarmWhiteCard, AppDestinations.VOICE),
        HomeFeature(labels.textTitleRes, labels.textSubtitleRes, painterResource(R.drawable.ic_lucide_languages), TextTranslationCard, GoldenAccent, AppDestinations.TEXT),
        HomeFeature(labels.dictionaryTitleRes, labels.dictionarySubtitleRes, painterResource(R.drawable.ic_lucide_book_open), DictionaryCard, MainText, AppDestinations.DICTIONARY),
        HomeFeature(labels.historyTitleRes, labels.historySubtitleRes, painterResource(R.drawable.ic_lucide_history), HistoryCard, PrimaryGreen, AppDestinations.HISTORY),
    )

    Box(Modifier.fillMaxSize()) {
        GeometricGreenBackground(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            cellSize = 52.dp,
            patternAlpha = 0.18f,
        )
        HomeIdentityHeader(
            title = stringResource(content.mainHeaderRes),
            badge = stringResource(labels.homeBadgeRes),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, top = 24.dp, end = 20.dp),
        )
        Surface(
            modifier = Modifier.fillMaxSize().padding(top = 180.dp),
            color = AppBackground,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        ) {}
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 156.dp, end = 20.dp, bottom = 24.dp),
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
    }
}
