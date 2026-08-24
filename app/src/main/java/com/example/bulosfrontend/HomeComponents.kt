package com.example.bulosfrontend

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.*
import java.text.DateFormat
import java.util.Date

data class HomeFeature(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val icon: Painter,
    val backgroundColor: Color,
    val accentColor: Color,
    val route: String,
)

@Composable
fun HomeIdentityHeader(title: String, badge: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            title,
            color = WarmWhiteCard,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 22.sp,
                lineHeight = 27.sp,
                letterSpacing = (-0.3).sp,
            ),
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(10.dp))
        Surface(
            color = SoftGreen.copy(alpha = 0.2f),
            contentColor = WarmWhiteCard.copy(alpha = 0.82f),
            shape = RoundedCornerShape(50),
        ) {
            Text(
                badge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    letterSpacing = 0.sp,
                ),
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
            )
        }
    }
}

@Composable
fun FeatureCard(feature: HomeFeature, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isSpeechFeature = feature.route == AppDestinations.VOICE
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 116.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = feature.backgroundColor),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSpeechFeature) Color.White.copy(alpha = 0.20f) else MainText.copy(alpha = 0.05f),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSpeechFeature) 4.dp else 2.dp,
            pressedElevation = 1.dp,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isSpeechFeature) WarmWhiteCard.copy(alpha = 0.2f) else WarmWhiteCard,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = feature.icon,
                    contentDescription = null,
                    tint = feature.accentColor,
                    modifier = Modifier.size(if (isSpeechFeature) 25.dp else 22.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Column {
                Text(
                    stringResource(feature.titleRes),
                    color = feature.accentColor,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        letterSpacing = (-0.1).sp,
                    ),
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(feature.subtitleRes),
                    color = if (isSpeechFeature) {
                        WarmWhiteCard.copy(alpha = 0.78f)
                    } else {
                        MutedText
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        letterSpacing = 0.sp,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun SupportedLanguagesCard(
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
        border = BorderStroke(1.dp, HomeCardBorder.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                stringResource(titleRes).uppercase(),
                color = MutedText,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    letterSpacing = 1.35.sp,
                ),
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(10.dp))
            val bulos = stringResource(R.string.language_name_bulos)
            val filipino = stringResource(R.string.language_name_filipino)
            val english = stringResource(R.string.language_name_english)
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = ForestGreen, fontWeight = FontWeight.Bold)) { append(bulos) }
                    withStyle(SpanStyle(color = MutedText.copy(alpha = 0.45f))) { append("  ·  ") }
                    withStyle(SpanStyle(color = MainText, fontWeight = FontWeight.Bold)) { append(filipino) }
                    withStyle(SpanStyle(color = MutedText.copy(alpha = 0.45f))) { append("  ·  ") }
                    withStyle(SpanStyle(color = MainText, fontWeight = FontWeight.Bold)) { append(english) }
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    letterSpacing = (-0.1).sp,
                ),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(descriptionRes),
                color = MutedText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 0.sp,
                ),
            )
        }
    }
}

@Composable
fun HomeDynamicContentCard(
    @StringRes titleRes: Int,
    @StringRes emptyRes: Int,
    @StringRes historyActionRes: Int,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Show up to 3 most recent items. HistoryProvider.history is newest-first.
    val recentItems = HistoryProvider.history.take(3)

    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))) {
        GeometricGreenBackground(Modifier.matchParentSize(), cellSize = 52.dp, patternAlpha = 0.10f)
        Column(Modifier.padding(horizontal = 16.dp, vertical = 15.dp)) {
            Text(
                stringResource(titleRes).uppercase(),
                color = SoftGreen.copy(alpha = 0.84f),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    letterSpacing = 1.25.sp,
                ),
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))

            if (recentItems.isEmpty()) {
                // Empty state
                Text(
                    stringResource(emptyRes),
                    color = WarmWhite,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    ),
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                // Up to 3 compact translation rows
                recentItems.forEachIndexed { index, historyItem ->
                    RecentTranslationRow(historyItem)
                    if (index < recentItems.lastIndex) {
                        Spacer(Modifier.height(7.dp))
                        HorizontalDivider(
                            color = SoftGreen.copy(alpha = 0.18f),
                            thickness = 0.5.dp,
                        )
                        Spacer(Modifier.height(7.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HomeCardAction(
                text = stringResource(historyActionRes),
                iconPainter = painterResource(R.drawable.ic_lucide_history),
                onClick = onHistoryClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RecentTranslationRow(item: HistoryItem) {
    // Direction label: "English → Bulos"
    Text(
        stringResource(R.string.direction_format, item.sourceLang, item.targetLang),
        color = GoldenAccent,
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, lineHeight = 15.sp),
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(3.dp))
    // Input text — primary display
    Text(
        item.inputText,
        color = WarmWhiteCard,
        style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 13.sp,
            lineHeight = 17.sp,
            letterSpacing = (-0.15).sp,
        ),
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
    // Translated text — secondary
    Text(
        item.translatedText,
        color = WarmWhiteCard.copy(alpha = 0.70f),
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.sp,
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
    // Timestamp
    Text(
        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(item.timestamp)),
        color = SoftGreen.copy(alpha = 0.65f),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 13.sp),
    )
}

@Composable
private fun HomeCardAction(
    text: String,
    iconPainter: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = SoftGreen.copy(alpha = 0.22f),
            contentColor = WarmWhiteCard,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Icon(painter = iconPainter, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(7.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, lineHeight = 15.sp),
            fontWeight = FontWeight.Bold,
            maxLines = 2,
        )
    }
}
