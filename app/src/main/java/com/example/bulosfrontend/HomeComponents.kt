package com.example.bulosfrontend

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

private val switchLanguageCardBrush = Brush.verticalGradient(
    0.00f to Color(0xFFFDFBF9),
    0.50f to Color(0xFFFDFCF9),
    1.00f to Color(0xFFFCFCFA),
)

@Composable
fun HomeIdentityHeader(title: String, badge: String, modifier: Modifier = Modifier) {
    val darkTheme = LocalBulosDarkTheme.current
    Column(modifier) {
        Text(
            title,
            color = if (darkTheme) DarkWarmText else WarmWhiteCard,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 23.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.3).sp,
            ),
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Surface(
            modifier = Modifier.height(26.dp),
            color = if (darkTheme) DarkHomeHeaderMid.copy(alpha = 0.82f) else SoftGreen.copy(alpha = 0.2f),
            contentColor = if (darkTheme) DarkWarmText else WarmWhiteCard.copy(alpha = 0.82f),
            shape = RoundedCornerShape(50),
        ) {
            Box(
                modifier = Modifier.fillMaxHeight().padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    badge,
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
}

@Composable
fun FeatureCard(feature: HomeFeature, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isTextFeature = feature.route == AppDestinations.TEXT
    val cardBorderColor = Color(0xFFFFFBF4)
    val textColor = if (isTextFeature) cardBorderColor else Color.Black
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val cardElevation by animateDpAsState(
        targetValue = if (pressed) 1.dp else 4.dp,
        animationSpec = tween(durationMillis = 100),
        label = "featureCardElevation",
    )
    val cardShape = RoundedCornerShape(16.dp)
    val iconColor = when {
        isTextFeature -> Color(0xFFB66D0B)
        feature.route == AppDestinations.VOICE -> Color(0xFF2F6530)
        else -> Color(0xFF315F32)
    }
    Card(
        onClick = onClick,
        modifier = modifier
            .height(118.dp)
            .shadow(
                elevation = cardElevation,
                shape = cardShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            ),
        interactionSource = interactionSource,
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            width = 1.dp,
            color = cardBorderColor,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(feature.backgroundColor)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(40.dp),
                shape = CircleShape,
                color = Color(0xFFFFFEFA),
                border = BorderStroke(0.75.dp, Color(0xFFE8E2D8)),
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = feature.icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 58.dp),
            ) {
                Text(
                    stringResource(feature.titleRes),
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        letterSpacing = 0.sp,
                    ),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    stringResource(feature.subtitleRes),
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.sp,
                    ),
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
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
    selectedLanguage: UiLanguage,
    onLanguageSelected: (UiLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(16.dp)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(63.dp)
            .shadow(
                elevation = 4.dp,
                shape = cardShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFFFFFBF4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(switchLanguageCardBrush)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(titleRes).uppercase(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    letterSpacing = 2.sp,
                ),
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            val bulos = stringResource(R.string.language_name_bulos)
            val filipino = stringResource(R.string.language_name_filipino)
            val english = stringResource(R.string.language_name_english)
            val languageStyle = MaterialTheme.typography.titleLarge.copy(
                fontSize = 14.sp,
                lineHeight = 18.sp,
                letterSpacing = (-0.1).sp,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                LanguageSwitchLabel(
                    text = bulos,
                    language = UiLanguage.BULOS,
                    selectedLanguage = selectedLanguage,
                    onClick = onLanguageSelected,
                    style = languageStyle,
                )
                Text("  ·  ", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f), style = languageStyle)
                LanguageSwitchLabel(
                    text = filipino,
                    language = UiLanguage.FILIPINO,
                    selectedLanguage = selectedLanguage,
                    onClick = onLanguageSelected,
                    style = languageStyle,
                )
                Text("  ·  ", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f), style = languageStyle)
                LanguageSwitchLabel(
                    text = english,
                    language = UiLanguage.ENGLISH,
                    selectedLanguage = selectedLanguage,
                    onClick = onLanguageSelected,
                    style = languageStyle,
                )
            }
        }
    }
}

@Composable
private fun LanguageSwitchLabel(
    text: String,
    language: UiLanguage,
    selectedLanguage: UiLanguage,
    onClick: (UiLanguage) -> Unit,
    style: androidx.compose.ui.text.TextStyle,
) {
    val color by animateColorAsState(
        targetValue = if (language == selectedLanguage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "languageSwitchColor",
    )
    Text(
        text = text,
        modifier = Modifier.clickable { onClick(language) },
        color = color,
        style = style,
        fontWeight = FontWeight.Bold,
    )
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
    val cardShape = RoundedCornerShape(16.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(63.dp)
            .shadow(
                elevation = 4.dp,
                shape = cardShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFFFFFBF4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeRecentTranslationCard)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(titleRes).uppercase(),
                    color = PrimaryGreen,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        letterSpacing = 2.sp,
                    ),
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(2.dp))

                if (recentItems.isEmpty()) {
                    Text(
                        stringResource(emptyRes),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                        ),
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    recentItems.forEachIndexed { index, historyItem ->
                        RecentTranslationRow(historyItem)
                        if (index < recentItems.lastIndex) {
                            Spacer(Modifier.height(6.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }
            HomeCardAction(
                text = stringResource(historyActionRes),
                iconPainter = painterResource(R.drawable.ic_lucide_history),
                onClick = onHistoryClick,
                modifier = Modifier.align(Alignment.CenterVertically),
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
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 13.sp,
            lineHeight = 17.sp,
            letterSpacing = (-0.15).sp,
        ),
        fontWeight = FontWeight.Bold,
    )
    // Translated text — secondary
    Text(
        item.translatedText,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.sp,
        ),
    )
    // Timestamp
    Text(
        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(item.timestamp)),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
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
        modifier = modifier.width(108.dp).height(30.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreen,
            contentColor = WarmWhiteCard,
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = null,
            modifier = Modifier.size(18.dp).align(Alignment.CenterVertically),
        )
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .clipToBounds(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    animationMode = MarqueeAnimationMode.Immediately,
                    repeatDelayMillis = 1_200,
                    initialDelayMillis = 1_200,
                    spacing = MarqueeSpacing(20.dp),
                    velocity = 22.dp,
                ),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 15.sp),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }
    }
}
