package com.example.bulosfrontend

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.Aileron

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

val TranslationInputBorderColor = Color(0xFFE5DDD2).copy(alpha = 0.65f)
val TranslationInputBorderWidth = 0.5.dp

@Composable
fun TranslationInputCard(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    editingEnabled: Boolean = true,
    footer: @Composable (() -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 106.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(TranslationInputBorderWidth, TranslationInputBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = editingEnabled,
                modifier = Modifier.fillMaxWidth().height(252.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = Aileron,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default,
                ),
                decorationBox = { innerTextField ->
                    Box(Modifier.fillMaxSize()) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Aileron),
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (footer != null) {
                Spacer(Modifier.height(8.dp))
                footer()
            }
        }
    }
}

@Composable
fun ConnectivityStatusPill(isOnline: Boolean, isServerReady: Boolean = true, modifier: Modifier = Modifier) {
    val containerColor = when {
        !isOnline -> MaterialTheme.colorScheme.surfaceVariant
        !isServerReady -> Color(0xFFFFF8E1) // Amber for waking up
        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    }
    val contentColor = when {
        !isOnline -> MaterialTheme.colorScheme.onSurfaceVariant
        !isServerReady -> Color(0xFFFFA000) // Amber for waking up
        else -> MaterialTheme.colorScheme.primary
    }
    val dotColor = when {
        !isOnline -> Color(0xFF9E9E9E)
        !isServerReady -> Color(0xFFFFC107) // Amber dot
        else -> Color(0xFF4CAF50)
    }
    val statusText = when {
        !isOnline -> "Offline"
        !isServerReady -> "Waking up..."
        else -> "Online"
    }

    Surface(
        modifier = modifier.height(24.dp),
        color = containerColor,
        shape = RoundedCornerShape(50),
        border = BorderStroke(0.5.dp, contentColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                Modifier
                    .size(6.dp)
                    .background(dotColor, CircleShape)
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
internal fun Modifier.safeHeaderInsets(): Modifier = windowInsetsPadding(
    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
)

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
    isOnline: Boolean? = null,
    isServerReady: Boolean = true,
    trailingContent: @Composable RowScope.() -> Unit = {},
    selectionMode: Boolean = false,
    selectionContent: @Composable RowScope.() -> Unit = {},
) = Column(Modifier.background(appHeaderGradientBrush())) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .safeHeaderInsets()
            .padding(start = 4.dp, top = AppHeaderTitleTopPadding, end = 16.dp, bottom = 7.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(
            targetState = selectionMode,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                (fadeIn(tween(160)) togetherWith fadeOut(tween(120))).using(
                    SizeTransform(clip = false, sizeAnimationSpec = { _, _ -> snap() }),
                )
            },
            label = "sharedHeaderMode",
        ) { selecting ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selecting) {
                    selectionContent()
                } else {
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
                    if (isOnline != null) {
                        ConnectivityStatusPill(isOnline, isServerReady)
                        Spacer(Modifier.width(8.dp))
                    }
                    trailingContent()
                }
            }
        }
    }
    Spacer(Modifier.height(AppHeaderTailHeight))
}

@Composable
fun PoppingIconButton(onClick: () -> Unit, icon: ImageVector, contentDescription: String?, modifier: Modifier = Modifier) {
    var isClicked by remember { mutableStateOf(value = false) }
    val scale by animateFloatAsState(
        targetValue = if (isClicked) 1.2f else 1f,
        animationSpec = tween(durationMillis = 100),
        finishedListener = { isClicked = false },
        label = "Pop",
    )
    Button(
        onClick = {
            isClicked = true
            onClick()
        },
        modifier = modifier.size(48.dp).scale(scale),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
    ) {
        Icon(icon, contentDescription, Modifier.size(24.dp))
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

@Composable
fun StandardFooter(textRes: Int) = Text(
    text = stringResource(textRes),
    modifier = Modifier.padding(bottom = 16.dp).alpha(0.7f),
    textAlign = TextAlign.Center,
    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Light, fontFamily = Aileron),
)
