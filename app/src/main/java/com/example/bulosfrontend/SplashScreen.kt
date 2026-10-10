package com.example.bulosfrontend

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.BulosFrontEndTheme
import kotlin.math.sin

private val SplashCream = Color(0xFFFFFBF4)
private val SplashForest = Color(0xFF173819)
private val SplashSage = Color(0xFF819976)
private val SplashGlowCore = Color(0xFFFFF4B8)
private val SplashGlowWarm = Color(0xFFF2EDB9)
private val SplashGlowMiddle = Color(0xFFD9DEB4)
private val SplashProgressForest = Color(0xFF385F2B)
private val SplashProgressGreen = Color(0xFF83AE46)
private val SplashProgressHighlight = Color(0xFFDCEF69)
internal const val StartupProgressAnimationDurationMillis = 900

fun splashGradientBrush(): Brush =
    Brush.verticalGradient(
        0.00f to SplashForest,
        0.25f to Color(0xFF355B35),
        0.55f to SplashSage,
        0.78f to Color(0xFFC5CEB6),
        1.00f to SplashCream,
    )

fun Modifier.splashGradientBackground(): Modifier =
    background(splashGradientBrush()).drawBehind {
        val glowCenter = Offset(size.width / 2f, size.height * 0.34f)
        drawCircle(
            brush = Brush.radialGradient(
                0.00f to SplashGlowCore.copy(alpha = 0.72f),
                0.28f to SplashGlowWarm.copy(alpha = 0.46f),
                0.58f to SplashGlowMiddle.copy(alpha = 0.18f),
                1.00f to Color.Transparent,
                center = glowCenter,
                radius = size.width * 0.64f,
            ),
            center = glowCenter,
            radius = size.width * 0.64f,
        )
        // Preserve the Splash palette while giving the title/subtitle area the same
        // soft green contrast field shown in the reference.
        val textEmphasisTop = size.height * 0.43f
        val textEmphasisHeight = size.height * 0.27f
        drawRect(
            brush = Brush.verticalGradient(
                0.00f to Color.Transparent,
                0.25f to SplashForest.copy(alpha = 0.16f),
                0.58f to SplashSage.copy(alpha = 0.20f),
                0.82f to SplashSage.copy(alpha = 0.08f),
                1.00f to Color.Transparent,
                startY = textEmphasisTop,
                endY = textEmphasisTop + textEmphasisHeight,
            ),
            topLeft = Offset(0f, textEmphasisTop),
            size = Size(size.width, textEmphasisHeight),
        )
        // Keep the text block in the darker green field. The reference's cream
        // transition begins below it and leads into the loading-indicator area.
        val lowerCreamTop = size.height * 0.66f
        drawRect(
            brush = Brush.verticalGradient(
                0.00f to Color.Transparent,
                0.26f to HomeMicGlowCream.copy(alpha = 0.12f),
                0.54f to HomeMicGlowCream.copy(alpha = 0.34f),
                0.78f to HomeMicGlowCream.copy(alpha = 0.54f),
                1.00f to HomeMicGlowCream.copy(alpha = 0.68f),
                startY = lowerCreamTop,
                endY = size.height,
            ),
            topLeft = Offset(0f, lowerCreamTop),
            size = Size(size.width, size.height - lowerCreamTop),
        )
    }

@Composable
fun BrandedSplashScreen(
    progress: Float = 0f,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onProgressCompleted: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(
                durationMillis = StartupProgressAnimationDurationMillis,
                easing = LinearOutSlowInEasing,
            ),
        )
    }
    LaunchedEffect(progress, animatedProgress.value) {
        if (progress >= 1f && animatedProgress.value >= 1f) {
            onProgressCompleted()
        }
    }
    Box(
        modifier
            .fillMaxSize()
            .splashGradientBackground(),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            SplashLayeredGlow(Modifier.fillMaxSize())
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height * 0.32f)
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    radius = size.width * 0.29f,
                    center = center,
                    style = Stroke(width = 0.75.dp.toPx()),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = size.width * 0.41f,
                    center = center,
                    style = Stroke(width = 0.65.dp.toPx()),
                )
            }

            val iconMountSize = (maxWidth * 0.29f).coerceIn(104.dp, 128.dp)
            val progressWidth = (maxWidth * 0.58f).coerceIn(210.dp, 260.dp)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = maxHeight * 0.32f - iconMountSize / 2f)
                    .size(iconMountSize)
                    .background(SplashCream, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center,
            ) {
                // This remains the dedicated placement for the final official app icon.
                Image(
                    painter = painterResource(R.drawable.ic_app_logo_placeholder),
                    contentDescription = stringResource(R.string.splash_logo_content_description),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(iconMountSize * 0.58f),
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = maxHeight * 0.50f)
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.splash_title),
                    color = SplashCream,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 37.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.splash_subtitle),
                    modifier = Modifier.padding(top = 18.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = maxHeight * 0.84f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .semantics {
                            progressBarRangeInfo = ProgressBarRangeInfo(
                                current = animatedProgress.value,
                                range = 0f..1f,
                            )
                        }
                        .width(progressWidth)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SplashCream.copy(alpha = 0.88f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress.value)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        SplashProgressForest,
                                        SplashProgressGreen,
                                        SplashProgressHighlight,
                                    ),
                                ),
                            ),
                    )
                }
                Text(
                    text = if (errorMessage == null) {
                        stringResource(R.string.splash_preparing)
                    } else {
                        errorMessage
                    },
                    modifier = Modifier.padding(top = 18.dp),
                    color = Color(0xFF294E2C),
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                )
                if (errorMessage != null) {
                    TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.splash_retry), color = SplashForest)
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashLayeredGlow(modifier: Modifier = Modifier) {
    val motion = rememberInfiniteTransition(label = "splashLayeredGlowMotion")
    val pulse by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "splashGlowPulse",
    )
    val sweepRotation by motion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4_800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "splashGlowSweepRotation",
    )
    val outerDriftRotation by motion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6_400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "splashGlowOuterDriftRotation",
    )
    val innerDriftRotation by motion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4_100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "splashGlowInnerDriftRotation",
    )
    val waveBreath by motion.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "splashGlowWaveBreath",
    )
    val depthWaveOne by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4_600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "splashGlowDepthWaveOne",
    )
    val depthWaveTwo by motion.animateFloat(
        initialValue = 0.46f,
        targetValue = 1.46f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6_200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "splashGlowDepthWaveTwo",
    )

    Canvas(modifier) {
        val center = Offset(size.width / 2f, size.height * 0.32f)
        val auraRadius = size.width * 0.64f
        val expansionEnd = 0.84f
        val expansionProgress = (pulse / expansionEnd).coerceAtMost(1f)
        val sweepScale = 0.90f + expansionProgress * 0.22f
        val rotatingRadius = auraRadius * 0.92f

        fun drawSoftLayer(
            rotation: Float,
            widthMultiplier: Float,
            heightMultiplier: Float,
            core: Color,
            middle: Color,
            outer: Color,
            coreAlpha: Float,
            middleAlpha: Float,
            outerAlpha: Float,
            breath: Float = 1f,
        ) {
            val width = rotatingRadius * widthMultiplier * sweepScale * breath
            val height = rotatingRadius * heightMultiplier * sweepScale * breath
            rotate(degrees = rotation, pivot = center) {
                drawOval(
                    brush = Brush.radialGradient(
                        0.00f to core.copy(alpha = coreAlpha),
                        0.42f to middle.copy(alpha = middleAlpha),
                        0.68f to outer.copy(alpha = outerAlpha),
                        0.84f to Color.Transparent,
                        1.00f to Color.Transparent,
                        center = center,
                        radius = width * 0.50f,
                    ),
                    topLeft = Offset(center.x - width / 2f, center.y - height / 2f),
                    size = Size(width, height),
                )
            }
        }

        drawSoftLayer(
            rotation = sweepRotation + 36f,
            widthMultiplier = 1.68f,
            heightMultiplier = 1.18f,
            core = SplashSage,
            middle = SplashSage,
            outer = SplashGlowMiddle,
            coreAlpha = 0.10f + expansionProgress * 0.04f,
            middleAlpha = 0.06f + expansionProgress * 0.03f,
            outerAlpha = 0.02f + expansionProgress * 0.012f,
        )
        drawSoftLayer(
            rotation = sweepRotation + 92f,
            widthMultiplier = 1.46f,
            heightMultiplier = 1.06f,
            core = SplashGlowWarm,
            middle = SplashGlowWarm,
            outer = SplashSage,
            coreAlpha = 0.14f + expansionProgress * 0.055f,
            middleAlpha = 0.085f + expansionProgress * 0.035f,
            outerAlpha = 0.03f + expansionProgress * 0.016f,
        )
        drawSoftLayer(
            rotation = sweepRotation + 148f,
            widthMultiplier = 1.18f,
            heightMultiplier = 0.90f,
            core = SplashGlowCore,
            middle = SplashGlowWarm,
            outer = SplashSage,
            coreAlpha = 0.12f + expansionProgress * 0.045f,
            middleAlpha = 0.065f + expansionProgress * 0.025f,
            outerAlpha = 0.022f + expansionProgress * 0.01f,
        )
        drawSoftLayer(
            rotation = sweepRotation + 212f,
            widthMultiplier = 0.90f,
            heightMultiplier = 0.66f,
            core = SplashGlowCore,
            middle = SplashGlowWarm,
            outer = SplashSage,
            coreAlpha = 0.05f + expansionProgress * 0.022f,
            middleAlpha = 0.025f + expansionProgress * 0.012f,
            outerAlpha = 0.008f,
        )
        drawSoftLayer(
            rotation = outerDriftRotation + 292f,
            widthMultiplier = 1.58f,
            heightMultiplier = 1.36f,
            core = SplashSage,
            middle = SplashSage,
            outer = SplashGlowMiddle,
            coreAlpha = 0.06f + expansionProgress * 0.02f,
            middleAlpha = 0.032f + expansionProgress * 0.012f,
            outerAlpha = 0.010f,
            breath = waveBreath,
        )
        drawSoftLayer(
            rotation = innerDriftRotation + 28f,
            widthMultiplier = 1.06f,
            heightMultiplier = 0.82f,
            core = SplashGlowWarm,
            middle = SplashGlowWarm,
            outer = SplashSage,
            coreAlpha = 0.045f + expansionProgress * 0.018f,
            middleAlpha = 0.024f + expansionProgress * 0.010f,
            outerAlpha = 0.008f,
            breath = waveBreath,
        )

        fun drawDepthWave(progress: Float, color: Color, strength: Float) {
            val intensity = sin(progress * Math.PI.toFloat()).coerceIn(0f, 1f)
            val radius = auraRadius * (0.50f + progress * 0.46f)
            drawCircle(
                brush = Brush.radialGradient(
                    0.00f to Color.Transparent,
                    0.48f to Color.Transparent,
                    0.66f to color.copy(alpha = strength * intensity * 0.22f),
                    0.80f to color.copy(alpha = strength * intensity),
                    0.94f to color.copy(alpha = strength * intensity * 0.16f),
                    1.00f to Color.Transparent,
                    center = center,
                    radius = radius,
                ),
                center = center,
                radius = radius,
            )
        }

        drawDepthWave(depthWaveOne, SplashSage, 0.10f)
        drawDepthWave(depthWaveTwo % 1f, SplashGlowWarm, 0.085f)
    }
}

@Preview(name = "Splash - Compact", device = "spec:width=360dp,height=640dp,dpi=420")
@Composable
private fun CompactSplashPreview() {
    BulosFrontEndTheme { BrandedSplashScreen() }
}

@Preview(name = "Splash - Tall", device = "spec:width=412dp,height=915dp,dpi=420")
@Composable
private fun TallSplashPreview() {
    BulosFrontEndTheme { BrandedSplashScreen() }
}
