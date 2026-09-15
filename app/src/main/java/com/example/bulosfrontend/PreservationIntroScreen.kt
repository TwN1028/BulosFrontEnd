package com.example.bulosfrontend

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.bulosfrontend.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreservationIntroScreen(
    content: PreservationIntroDialogueContent,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var animationStarted by remember { mutableStateOf(false) }
    val iconScale = remember { Animatable(0.95f) }
    val iconAlpha by animateFloatAsState(if (animationStarted) 1f else 0f, tween(200), label = "iconAlpha")
    val titleAlpha by animateFloatAsState(if (animationStarted) 1f else 0f, tween(220, 80), label = "titleAlpha")
    val chipProgress = List(3) { index -> animateFloatAsState(if (animationStarted) 1f else 0f, tween(210, 190 + (index * 65)), label = "chip$index").value }
    val bodyProgress by animateFloatAsState(if (animationStarted) 1f else 0f, tween(Design.DurationMedium, 360), label = "bodyProgress")
    val quoteProgress by animateFloatAsState(if (animationStarted) 1f else 0f, tween(230, 530), label = "quoteProgress")
    val buttonAlpha by animateFloatAsState(if (animationStarted) 1f else 0f, tween(200, 680), label = "buttonAlpha")

    LaunchedEffect(Unit) {
        animationStarted = true
        iconScale.animateTo(1.02f, tween(240))
        iconScale.animateTo(1f, tween(160))
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val headerHeight = if (maxHeight >= 800.dp) 208.dp else 196.dp
            val majorSpacing = if (maxHeight >= 800.dp) 28.dp else Design.PaddingHorizontal

            LazyColumn(Modifier.fillMaxSize().navigationBarsPadding(), contentPadding = PaddingValues(bottom = Design.PaddingHorizontal)) {
                item {
                    Box(Modifier.fillMaxWidth().heightIn(min = headerHeight).clip(Design.LargeCardShape)) {
                        GeometricGreenBackground(Modifier.matchParentSize(), cellSize = 52.dp, patternAlpha = Design.AlphaPatternMedium)
                        Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, HomeSpeechGreen.copy(alpha = 0.16f)))))
                        Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Design.PaddingHorizontal, vertical = Design.PaddingElement), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(60.dp).graphicsLayer { alpha = iconAlpha; scaleX = iconScale.value; scaleY = iconScale.value }, contentAlignment = Alignment.Center) {
                                Surface(Modifier.size(60.dp).shadow(Design.ShadowAura, CircleShape), CircleShape, color = SoftGreen.copy(0.13f)) {}
                                Surface(Modifier.size(Design.IconSizeSmall), CircleShape, color = SoftGreen.copy(0.25f), contentColor = WarmWhiteCard) {
                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Language, stringResource(content.iconDescriptionRes), Modifier.size(Design.IconSizeMini)) }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Column(Modifier.graphicsLayer { alpha = titleAlpha }, horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(content.titleRes), color = WarmWhiteCard, style = MaterialTheme.typography.headlineMedium, fontWeight = Design.FontExtraBold, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(5.dp))
                                Text(stringResource(content.supportRes), color = WarmWhiteCard.copy(0.8f), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                item {
                    Column(Modifier.padding(horizontal = Design.PaddingHorizontal, vertical = Design.PaddingHorizontal), verticalArrangement = Arrangement.spacedBy(majorSpacing)) {
                        FlowRow(Modifier.fillMaxWidth(), Arrangement.spacedBy(Design.SpacingSmall), Arrangement.spacedBy(Design.SpacingSmall)) {
                            PreservationConceptChip(Icons.Default.Language, content.indigenousLanguageRes, chipProgress[0])
                            PreservationConceptChip(Icons.Default.FavoriteBorder, content.preservationRes, chipProgress[1])
                            PreservationConceptChip(Icons.Default.PhoneAndroid, content.digitalAccessRes, chipProgress[2])
                        }
                        Column(Modifier.graphicsLayer { alpha = bodyProgress; translationY = (1f - bodyProgress) * 10.dp.toPx() }, Arrangement.spacedBy(Design.PaddingElement)) {
                            PreservationParagraph(content.firstParagraphRes)
                            PreservationParagraph(content.secondParagraphRes)
                            PreservationParagraph(content.thirdParagraphRes)
                        }
                        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).graphicsLayer { alpha = quoteProgress; translationY = (1f - quoteProgress) * Design.IconPadding.toPx() }) {
                            Box(Modifier.width(3.dp).fillMaxHeight().clip(Design.Circle).background(ForestGreen))
                            Spacer(Modifier.width(Design.PaddingElement))
                            Column(Modifier.weight(1f), Arrangement.spacedBy(Design.PaddingSmall)) {
                                Text(stringResource(content.finalLeadRes), color = ForestGreen, style = MaterialTheme.typography.titleMedium, fontWeight = Design.FontBold)
                                Text(stringResource(content.finalDetailRes), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        Button(onClick = onContinue, modifier = Modifier.align(Alignment.CenterHorizontally).widthIn(max = 480.dp).fillMaxWidth().height(Design.ButtonHeightAction).graphicsLayer { alpha = buttonAlpha }, shape = Design.CardShape, colors = ButtonDefaults.buttonColors(ForestGreen, WarmWhiteCard)) {
                            Text(stringResource(content.continueRes), style = MaterialTheme.typography.labelLarge, fontWeight = Design.FontBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreservationParagraph(res: Int) = Text(stringResource(res), color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)

@Composable
private fun PreservationConceptChip(icon: ImageVector, res: Int, progress: Float) = Surface(
    Modifier.widthIn(max = 220.dp).graphicsLayer { alpha = progress; translationY = (1f - progress) * 6.dp.toPx() },
    Design.Circle, SoftGreen.copy(0.62f), ForestGreen
) {
    Row(Modifier.padding(horizontal = 11.dp, vertical = Design.IconPadding), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(Design.IconSizeMicro))
        Spacer(Modifier.width(7.dp))
        Text(stringResource(res), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}
