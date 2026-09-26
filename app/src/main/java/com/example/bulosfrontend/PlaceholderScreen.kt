package com.example.bulosfrontend

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.ForestGreen
import com.example.bulosfrontend.ui.theme.HomeHelpCard
import com.example.bulosfrontend.ui.theme.SoftGreen
import com.example.bulosfrontend.ui.theme.WarmWhiteCard

@Composable
fun HelpOnboardingScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val help = viewModel.content.help
    Surface(Modifier.fillMaxSize(), color = HomeContentCream) {
        Column(Modifier.fillMaxSize()) {
            FeaturePatternHeader(
                title = stringResource(help.cardTitleRes),
                subtitle = stringResource(help.cardSubtitleRes),
                onBack = onBack,
                iconRes = R.drawable.ic_lucide_circle_help,
                headerBottomExtension = 24.dp,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = HomeHelpCard.copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, TranslationInputBorderColor),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Surface(
                                modifier = Modifier.size(58.dp),
                                shape = CircleShape,
                                color = WarmWhiteCard,
                                contentColor = ForestGreen,
                                shadowElevation = 3.dp,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_help_reference),
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = stringResource(help.screenTitleRes),
                                color = ForestGreen,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(help.messageRes),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = stringResource(help.stepsTitleRes),
                        color = ForestGreen,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                item {
                    HelpOnboardingStep(
                        number = 1,
                        iconRes = R.drawable.ic_lucide_mic,
                        title = stringResource(help.speechStepTitleRes),
                        body = stringResource(help.speechStepBodyRes),
                    )
                }
                item {
                    HelpOnboardingStep(
                        number = 2,
                        iconRes = R.drawable.ic_lucide_languages,
                        title = stringResource(help.textStepTitleRes),
                        body = stringResource(help.textStepBodyRes),
                    )
                }
                item {
                    HelpOnboardingStep(
                        number = 3,
                        iconRes = R.drawable.ic_saved_history_reference,
                        title = stringResource(help.resultStepTitleRes),
                        body = stringResource(help.resultStepBodyRes),
                    )
                }
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = SoftGreen.copy(alpha = 0.52f),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = stringResource(help.tipsTitleRes),
                                color = ForestGreen,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            HelpTip(stringResource(help.firstTipRes))
                            HelpTip(stringResource(help.secondTipRes))
                            HelpTip(stringResource(help.thirdTipRes))
                        }
                    }
                }
                item {
                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(17.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F7045),
                            contentColor = WarmWhiteCard,
                        ),
                    ) {
                        Text(stringResource(help.backRes), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpOnboardingStep(
    number: Int,
    @androidx.annotation.DrawableRes iconRes: Int,
    title: String,
    body: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = WarmWhiteCard,
        border = BorderStroke(1.dp, TranslationInputBorderColor),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = SoftGreen,
                    contentColor = ForestGreen,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(23.dp),
                        )
                    }
                }
                Surface(
                    modifier = Modifier.size(19.dp),
                    shape = CircleShape,
                    color = ForestGreen,
                    contentColor = WarmWhiteCard,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = number.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = title,
                    color = ForestGreen,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun HelpTip(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Text("•", color = ForestGreen, fontWeight = FontWeight.Bold)
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
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
