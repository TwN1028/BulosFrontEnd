package com.example.bulosfrontend

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.Cream
import com.example.bulosfrontend.ui.theme.DeepForestGreen
import com.example.bulosfrontend.ui.theme.ForestGreen
import com.example.bulosfrontend.ui.theme.HomeSpeechGreen
import com.example.bulosfrontend.ui.theme.SoftGreen
import com.example.bulosfrontend.ui.theme.WarmWhiteCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreservationIntroScreen(
    content: PreservationIntroDialogueContent,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var animationStarted by remember { mutableStateOf(false) }
    val iconScale = remember { Animatable(0.95f) }
    val iconAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "preservationIconAlpha",
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 220, delayMillis = 80),
        label = "preservationTitleAlpha",
    )
    val chipProgress = List(3) { index ->
        animateFloatAsState(
            targetValue = if (animationStarted) 1f else 0f,
            animationSpec = tween(durationMillis = 210, delayMillis = 190 + (index * 65)),
            label = "preservationChip$index",
        ).value
    }
    val bodyProgress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 250, delayMillis = 360),
        label = "preservationBodyProgress",
    )
    val quoteProgress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 230, delayMillis = 530),
        label = "preservationQuoteProgress",
    )
    val buttonAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 200, delayMillis = 680),
        label = "preservationButtonAlpha",
    )

    LaunchedEffect(Unit) {
        animationStarted = true
        iconScale.animateTo(1.02f, tween(durationMillis = 240))
        iconScale.animateTo(1f, tween(durationMillis = 160))
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val headerHeight = if (maxHeight >= 800.dp) 208.dp else 196.dp
            val majorSpacing = if (maxHeight >= 800.dp) 28.dp else 24.dp

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = headerHeight)
                            .clip(
                                RoundedCornerShape(
                                    bottomStart = 28.dp,
                                    bottomEnd = 28.dp,
                                ),
                            ),
                    ) {
                        GeometricGreenBackground(
                            modifier = Modifier.matchParentSize(),
                            cellSize = 52.dp,
                            patternAlpha = 0.10f,
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            HomeSpeechGreen.copy(alpha = 0.16f),
                                        ),
                                    ),
                                ),
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .graphicsLayer {
                                        alpha = iconAlpha
                                        scaleX = iconScale.value
                                        scaleY = iconScale.value
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .shadow(2.dp, CircleShape),
                                    shape = CircleShape,
                                    color = SoftGreen.copy(alpha = 0.13f),
                                ) {}
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = SoftGreen.copy(alpha = 0.25f),
                                    contentColor = WarmWhiteCard,
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = stringResource(content.iconDescriptionRes),
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Column(
                                modifier = Modifier.graphicsLayer { alpha = titleAlpha },
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = stringResource(content.titleRes),
                                    color = WarmWhiteCard,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = 22.sp,
                                        lineHeight = 27.sp,
                                        letterSpacing = (-0.3).sp,
                                    ),
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    text = stringResource(content.supportRes),
                                    color = WarmWhiteCard.copy(alpha = 0.80f),
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 17.sp),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(majorSpacing),
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            PreservationConceptChip(
                                icon = Icons.Default.Language,
                                labelRes = content.indigenousLanguageRes,
                                progress = chipProgress[0],
                            )
                            PreservationConceptChip(
                                icon = Icons.Default.FavoriteBorder,
                                labelRes = content.preservationRes,
                                progress = chipProgress[1],
                            )
                            PreservationConceptChip(
                                icon = Icons.Default.PhoneAndroid,
                                labelRes = content.digitalAccessRes,
                                progress = chipProgress[2],
                            )
                        }

                        Column(
                            modifier = Modifier.graphicsLayer {
                                alpha = bodyProgress
                                translationY = (1f - bodyProgress) * 10.dp.toPx()
                            },
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            PreservationParagraph(content.firstParagraphRes)
                            PreservationParagraph(content.secondParagraphRes)
                            PreservationParagraph(content.thirdParagraphRes)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .graphicsLayer {
                                    alpha = quoteProgress
                                    translationY = (1f - quoteProgress) * 8.dp.toPx()
                                },
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(ForestGreen),
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = stringResource(content.finalLeadRes),
                                    color = ForestGreen,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        lineHeight = 20.sp,
                                        letterSpacing = (-0.1).sp,
                                    ),
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = stringResource(content.finalDetailRes),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 21.sp),
                                )
                            }
                        }

                        Button(
                            onClick = onContinue,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .widthIn(max = 480.dp)
                                .fillMaxWidth()
                                .height(56.dp)
                                .graphicsLayer { alpha = buttonAlpha },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestGreen,
                                contentColor = WarmWhiteCard,
                            ),
                        ) {
                            Text(
                                text = stringResource(content.continueRes),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreservationParagraph(textRes: Int) {
    Text(
        text = stringResource(textRes),
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 21.sp),
    )
}

@Composable
private fun PreservationConceptChip(
    icon: ImageVector,
    labelRes: Int,
    progress: Float,
) {
    Surface(
        modifier = Modifier
            .widthIn(max = 220.dp)
            .graphicsLayer {
                alpha = progress
                translationY = (1f - progress) * 6.dp.toPx()
            },
        shape = RoundedCornerShape(50),
        color = SoftGreen.copy(alpha = 0.62f),
        contentColor = ForestGreen,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(7.dp))
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    letterSpacing = 0.sp,
                ),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
