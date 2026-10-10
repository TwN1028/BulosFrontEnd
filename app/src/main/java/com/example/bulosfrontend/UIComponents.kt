package com.example.bulosfrontend

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.Aileron
import com.example.bulosfrontend.ui.theme.NavigationCream

val LocalConnectionStatus = staticCompositionLocalOf { ConnectionStatus.OFFLINE }
val LocalConnectionUiLanguage = staticCompositionLocalOf { UiLanguage.ENGLISH }

@Composable
fun ConnectionStatusIndicator(
    modifier: Modifier = Modifier,
    status: ConnectionStatus = LocalConnectionStatus.current,
    language: UiLanguage = LocalConnectionUiLanguage.current,
) {
    val labelRes = when (language) {
        UiLanguage.ENGLISH -> when (status) {
            ConnectionStatus.ONLINE -> R.string.connection_online
            ConnectionStatus.OFFLINE -> R.string.connection_offline
            ConnectionStatus.OFFLINE_MODE -> R.string.connection_offline_mode
            ConnectionStatus.WAKING_UP -> R.string.connection_waking_up
            ConnectionStatus.SERVER_UNAVAILABLE -> R.string.connection_server_unavailable
        }
        UiLanguage.FILIPINO -> when (status) {
            ConnectionStatus.ONLINE -> R.string.connection_online_fil
            ConnectionStatus.OFFLINE -> R.string.connection_offline_fil
            ConnectionStatus.OFFLINE_MODE -> R.string.connection_offline_mode_fil
            ConnectionStatus.WAKING_UP -> R.string.connection_waking_up_fil
            ConnectionStatus.SERVER_UNAVAILABLE -> R.string.connection_server_unavailable_fil
        }
        UiLanguage.BULOS -> when (status) {
            ConnectionStatus.ONLINE -> R.string.connection_online_bul
            ConnectionStatus.OFFLINE -> R.string.connection_offline_bul
            ConnectionStatus.OFFLINE_MODE -> R.string.connection_offline_mode_bul
            ConnectionStatus.WAKING_UP -> R.string.connection_waking_up_bul
            ConnectionStatus.SERVER_UNAVAILABLE -> R.string.connection_server_unavailable_bul
        }
    }
    val dotColor = when (status) {
        ConnectionStatus.ONLINE -> Color(0xFF2E7D32)
        ConnectionStatus.OFFLINE -> Color(0xFF757575)
        ConnectionStatus.OFFLINE_MODE -> Color(0xFF5F6F52)
        ConnectionStatus.WAKING_UP -> Color(0xFFD08A19)
        ConnectionStatus.SERVER_UNAVAILABLE -> Color(0xFFB5483D)
    }
    Surface(
        modifier = modifier,
        color = NavigationCream.copy(alpha = 0.94f),
        contentColor = Color(0xFF29452E),
        shape = RoundedCornerShape(50),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(Modifier.size(7.dp).background(dotColor, CircleShape))
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun OfflineSuggestionList(
    suggestions: List<OfflineTranslationSuggestion>,
    onSelect: (OfflineTranslationSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (suggestions.isEmpty()) return
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.offline_suggestions_title),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF36583A),
        )
        suggestions.forEach { suggestion ->
            OutlinedButton(
                onClick = { onSelect(suggestion) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF4F7045).copy(alpha = 0.35f)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "${suggestion.sourceText} → ${suggestion.translatedText}",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(
                            R.string.offline_suggestion_metrics,
                            (suggestion.similarity * 100).toInt(),
                            suggestion.editDistance,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
internal fun Modifier.safeHeaderInsets(): Modifier = windowInsetsPadding(
    WindowInsets.statusBars
        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
)

@Composable
internal fun AdaptiveHeaderRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Layout(
        modifier = modifier
            .fillMaxWidth()
            .safeHeaderInsets(),
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        },
    ) { measurables, constraints ->
        val row = measurables.single().measure(constraints.copy(minHeight = 0))
        // Scale breathing room from the measured row instead of assigning a
        // device-specific y-coordinate. Larger fonts therefore grow the header.
        val verticalSpace = row.height * 7 / 12
        layout(constraints.maxWidth, row.height + verticalSpace * 2) {
            row.placeRelative(0, verticalSpace)
        }
    }
}

@Composable
internal fun OverlappingHeaderLayout(
    overlap: Dp,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val headerPlaceable = subcompose("header") { header() }
            .first()
            .measure(constraints.copy(minHeight = 0))
        val overlapPx = overlap.roundToPx().coerceAtMost(headerPlaceable.height)
        val contentTop = headerPlaceable.height - overlapPx
        val contentHeight = (constraints.maxHeight - contentTop).coerceAtLeast(0)
        val contentPlaceable = subcompose("content") { content() }
            .first()
            .measure(
                constraints.copy(
                    minHeight = contentHeight,
                    maxHeight = contentHeight,
                ),
            )
        layout(constraints.maxWidth, constraints.maxHeight) {
            headerPlaceable.placeRelative(0, 0)
            contentPlaceable.placeRelative(0, contentTop)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTopAppBar(
    title: String,
    @androidx.annotation.StringRes logoDescriptionRes: Int = R.string.app_logo_content_description,
    @androidx.annotation.DrawableRes iconRes: Int? = null,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailingContent: @Composable RowScope.() -> Unit = {},
    selectionMode: Boolean = false,
    selectionContent: @Composable RowScope.() -> Unit = {},
) = Box {
    Box(Modifier.matchParentSize().background(appHeaderGradientBrush()))
    Column {
    AdaptiveHeaderRow {
        AnimatedContent(
            targetState = selectionMode,
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                (fadeIn(tween(160)) togetherWith fadeOut(tween(120))).using(
                    SizeTransform(clip = false, sizeAnimationSpec = { _, _ -> snap() }),
                )
            },
            label = "sharedHeaderMode",
        ) { selecting ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selecting) {
                    selectionContent()
                } else {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color.White,
                            )
                        }
                    }
                    Icon(
                        painter = painterResource(iconRes ?: R.drawable.ic_launcher_foreground),
                        contentDescription = stringResource(logoDescriptionRes),
                        tint = Color.White,
                        modifier = if (iconRes != null) Modifier.size(48.dp).padding(12.dp) else Modifier.size(48.dp).padding(8.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                ),
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                            )
                        }
                    }
                    trailingContent()
                }
            }
        }
    }
    Spacer(Modifier.height(AppHeaderTailHeight))
    }
}

@Composable
fun TopToastNotification(visible: Boolean, message: String, innerPadding: PaddingValues) {
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = innerPadding.calculateTopPadding() + 16.dp),
        ) {
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(16.dp), tonalElevation = 4.dp) {
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}
