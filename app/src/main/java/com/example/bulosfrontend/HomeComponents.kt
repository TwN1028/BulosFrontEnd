package com.example.bulosfrontend

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.*

data class HomeFeature(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val icon: Painter,
    val backgroundColor: Color,
    val accentColor: Color,
    val route: String,
)

private val switchLanguageCardBrush = Brush.verticalGradient(
    colors = listOf(HomeRecentTranslationCard, HomeRecentTranslationCard),
)

internal val HomeFeatureIconTint = Color(0xFF315F32)
private val HomeFeatureIconContainerColor = Color(0xFFFFFEFA)
private val HomeFeatureIconBorder = BorderStroke(0.75.dp, Color(0xFFE8E2D8))
private val HomeFeatureIconContainerSize = 40.dp
private val HomeFeatureIconGlyphSize = 24.dp

internal val HomeCardShape = RoundedCornerShape(16.dp)
internal val HomeSupportingCardBorder = BorderStroke(
    width = 0.5.dp,
    color = Color(0xFFE5DDD2).copy(alpha = 0.65f),
)
internal val LanguageSelectorCardShape = RoundedCornerShape(16.dp)
internal val LanguageSelectorCardFill = Brush.verticalGradient(
    0.00f to Color(0xFFFFFEFD),
    0.42f to Color(0xFFFFFDF9),
    1.00f to HomeContentCream,
)
internal val LanguageSelectorCardBorder = Brush.verticalGradient(
    0.00f to Color.White,
    0.48f to Color(0xFFFFFDF9),
    1.00f to Color(0xFFDBD9D2),
)

@Composable
fun HomeIdentityHeader(title: String, badge: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.Top) {
      Column(Modifier.weight(1f)) {
        Text(
            title,
            color = WarmWhiteCard,
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
            color = SoftGreen.copy(alpha = 0.2f),
            contentColor = WarmWhiteCard.copy(alpha = 0.82f),
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
      Spacer(Modifier.width(8.dp))
      ConnectionStatusIndicator()
    }
}

@Composable
fun FeatureCard(feature: HomeFeature, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isTextFeature = feature.route == AppDestinations.TEXT
    val isHelpFeature = feature.route == AppDestinations.HELP
    val isHistoryFeature = feature.route == AppDestinations.HISTORY
    val iconContainerSize = when {
        isHelpFeature -> 43.dp
        isHistoryFeature -> 44.dp
        else -> 40.dp
    }
    val iconGlyphSize = when {
        isHelpFeature -> 27.dp
        else -> 24.dp
    }
    val cardBorderColor = Color(0xFFFFFBF4)
    val textColor = if (isTextFeature) cardBorderColor else Color.Black
    val subtitleColor = if (feature.route == AppDestinations.MORE) {
        TranslationInputTextColor
    } else {
        textColor
    }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val cardElevation by animateDpAsState(
        targetValue = if (pressed) 1.dp else 4.dp,
        animationSpec = tween(durationMillis = 100),
        label = "featureCardElevation",
    )
    val cardShape = HomeCardShape
    val iconColor = when {
        isTextFeature -> Color(0xFFB66D0B)
        feature.route == AppDestinations.VOICE -> Color(0xFF2F6530)
        else -> HomeFeatureIconTint
    }
    Card(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 118.dp)
            .shadow(
                elevation = cardElevation,
                shape = cardShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            ),
        interactionSource = interactionSource,
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = feature.backgroundColor),
        border = BorderStroke(
            width = 1.dp,
            color = cardBorderColor,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            HomeFeatureIcon(
                painter = feature.icon,
                tint = iconColor,
                containerSize = iconContainerSize,
                glyphSize = iconGlyphSize,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(feature.titleRes),
                color = textColor,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    letterSpacing = 0.sp,
                ),
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                stringResource(feature.subtitleRes),
                color = subtitleColor,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.sp,
                ),
                fontWeight = FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun HomeFeatureIcon(
    painter: Painter,
    modifier: Modifier = Modifier,
    tint: Color = HomeFeatureIconTint,
    containerSize: Dp = HomeFeatureIconContainerSize,
    glyphSize: Dp = HomeFeatureIconGlyphSize,
) {
    Surface(
        modifier = modifier.size(containerSize),
        shape = CircleShape,
        color = HomeFeatureIconContainerColor,
        border = HomeFeatureIconBorder,
        shadowElevation = 2.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(glyphSize),
            )
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
    val cardShape = HomeCardShape
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
        border = HomeSupportingCardBorder,
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
            Spacer(Modifier.height(4.dp))
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
        targetValue = if (language == selectedLanguage) MaterialTheme.colorScheme.primary else Color.Black,
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
    val cardShape = HomeCardShape

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
        border = HomeSupportingCardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeContentCream)
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
            }
            HomeCardAction(
                text = stringResource(historyActionRes),
                iconPainter = painterResource(R.drawable.ic_saved_history_reference),
                onClick = onHistoryClick,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }
    }
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
        modifier = modifier.height(30.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SharedActiveButtonColor,
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
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 15.sp),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
        )
    }
}
