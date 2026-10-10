package com.example.bulosfrontend

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.Cream
import com.example.bulosfrontend.ui.theme.PatternGreen
import com.example.bulosfrontend.ui.theme.PrimaryGreen
import com.example.bulosfrontend.ui.theme.SoftGreen
import com.example.bulosfrontend.ui.theme.SplashSage

internal val HomeContentCream = Color(0xFFFFFBF4)
internal val AppHeaderBottomExtension = 30.dp
internal val AppHeaderTailHeight = AppHeaderBottomExtension
internal val TranslationCardMinHeight = 348.dp

@Composable
fun HomeHeaderBackground(
    modifier: Modifier = Modifier,
    includeCreamTail: Boolean = true,
    includePattern: Boolean = true,
    includeAmbientWhirlpool: Boolean = false,
) {
    Box(
        modifier = modifier.background(homeHeaderGradientBrush(includeCreamTail)),
    ) {
        if (includeAmbientWhirlpool) {
            HomeAmbientWhirlpool(Modifier.matchParentSize())
        }
        if (includePattern) {
            GeometricGreenBackground(
                modifier = Modifier.matchParentSize(),
                cellSize = 52.dp,
                patternAlpha = 0.08f,
                backgroundColor = Color.Transparent,
                patternColor = Color(0xFF5E7A4A),
            )
        }
    }
}

/** A low-opacity, non-interactive wash used only behind the Home header content. */
@Composable
private fun HomeAmbientWhirlpool(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "homeAmbientWhirlpool")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 42_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "homeAmbientWhirlpoolRotation",
    )
    val scale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "homeAmbientWhirlpoolScale",
    )
    val intensity by transition.animateFloat(
        initialValue = 0.78f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "homeAmbientWhirlpoolIntensity",
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height * 0.48f)
        val baseDiameter = minOf(size.width, size.height)

        fun drawSoftLayer(
            degrees: Float,
            centerOffset: Offset,
            widthMultiplier: Float,
            heightMultiplier: Float,
            centerColor: Color,
            middleColor: Color,
        ) {
            val ovalWidth = baseDiameter * widthMultiplier * scale
            val ovalHeight = baseDiameter * heightMultiplier * scale
            val layerCenter = center + centerOffset
            val ovalTopLeft = Offset(
                x = layerCenter.x - ovalWidth / 2f,
                y = layerCenter.y - ovalHeight / 2f,
            )
            rotate(degrees = degrees, pivot = center) {
                drawOval(
                    brush = Brush.radialGradient(
                        0.00f to centerColor.copy(alpha = centerColor.alpha * intensity),
                        0.48f to middleColor.copy(alpha = middleColor.alpha * intensity),
                        1.00f to Color.Transparent,
                        center = layerCenter,
                        radius = ovalWidth * 0.52f,
                    ),
                    topLeft = ovalTopLeft,
                    size = Size(ovalWidth, ovalHeight),
                )
            }
        }

        drawSoftLayer(
            degrees = rotation,
            centerOffset = Offset(x = -baseDiameter * 0.15f, y = -baseDiameter * 0.05f),
            widthMultiplier = 1.74f,
            heightMultiplier = 0.94f,
            centerColor = SoftGreen.copy(alpha = 0.16f),
            middleColor = SplashSage.copy(alpha = 0.07f),
        )
        drawSoftLayer(
            degrees = rotation * 0.62f,
            centerOffset = Offset(x = baseDiameter * 0.13f, y = baseDiameter * 0.09f),
            widthMultiplier = 1.48f,
            heightMultiplier = 0.78f,
            centerColor = Cream.copy(alpha = 0.11f),
            middleColor = SoftGreen.copy(alpha = 0.05f),
        )
    }
}

fun homeHeaderGradientBrush(includeCreamTail: Boolean = true): Brush {
    return Brush.verticalGradient(*homeHeaderGradientStops(includeCreamTail))
}

fun appHeaderGradientBrush(): Brush =
    Brush.verticalGradient(*homeHeaderGradientStops(includeCreamTail = true))

fun speechTranslationGradientBrush(): Brush =
    Brush.verticalGradient(
        0.00f to Color(0xFF1E3F20),
        0.32f to Color(0xFF5E7A4A),
        0.70f to Color(0xFFEAEBD9),
        1.00f to Color(0xFFF7F8F2),
    )

fun Modifier.speechTranslationBackground(): Modifier =
    background(speechTranslationGradientBrush())
        .drawBehind {
            val center = Offset(size.width / 2f, size.height * 0.58f)
            val radius = size.width * 0.86f
            drawCircle(
                brush = Brush.radialGradient(
                    0.00f to Color(0xFFEAEBD9).copy(alpha = 0.30f),
                    0.32f to Color(0xFFEAEBD9).copy(alpha = 0.23f),
                    0.58f to Color(0xFFEAEBD9).copy(alpha = 0.13f),
                    0.78f to Color(0xFFEAEBD9).copy(alpha = 0.055f),
                    0.94f to Color(0xFFEAEBD9).copy(alpha = 0.012f),
                    1.00f to Color.Transparent,
                    center = center,
                    radius = radius,
                ),
                center = center,
                radius = radius,
            )
        }

fun homeHeaderGradientStops(includeCreamTail: Boolean = true): Array<Pair<Float, Color>> =
    buildList {
        add(0.00f to Color(0xFF1E3F20))
        add(0.30f to Color(0xFF4F7045))
        if (includeCreamTail) {
            add(0.56f to Color(0xFF879B70))
            add(0.78f to Color(0xFFC8CFB3))
            add(0.92f to Color(0xFFEDEBDD))
            add(1.00f to HomeContentCream)
        } else {
            clear()
            add(0.00f to Color(0xFF1E3F20))
            add(0.28f to Color(0xFF315A32))
            add(0.48f to Color(0xFF5E7A4A))
            add(0.66f to Color(0xFFA6B394))
            add(0.84f to Color(0xFFE6E5D6))
            add(1.00f to HomeContentCream)
        }
    }.toTypedArray()

@Composable
fun GeometricGreenBackground(
    modifier: Modifier = Modifier,
    cellSize: Dp = 52.dp,
    patternAlpha: Float = 0.1f,
    backgroundColor: Color = PrimaryGreen,
    patternColor: Color = PatternGreen,
) {
    val tileSize = with(LocalDensity.current) { cellSize.toPx() }
    Canvas(modifier = modifier) {
        drawRect(backgroundColor)

        val scale = tileSize / FIGMA_TILE_SIZE
        val resolvedPatternColor = patternColor.copy(alpha = patternAlpha.coerceIn(0f, 1f))
        var tileTop = -tileSize
        while (tileTop < size.height + tileSize) {
            var tileLeft = -tileSize
            while (tileLeft < size.width + tileSize) {
                drawFigmaPatternTile(
                    origin = Offset(tileLeft, tileTop),
                    scale = scale,
                    color = resolvedPatternColor,
                )
                tileLeft += tileSize
            }
            tileTop += tileSize
        }
    }
}

private const val FIGMA_TILE_SIZE = 52f

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFigmaPatternTile(
    origin: Offset,
    scale: Float,
    color: Color,
) {
    fun point(x: Float, y: Float) = Offset(origin.x + x * scale, origin.y + y * scale)
    fun diamond(top: Offset, right: Offset, bottom: Offset, left: Offset) = Path().apply {
        moveTo(top.x, top.y)
        lineTo(right.x, right.y)
        lineTo(bottom.x, bottom.y)
        lineTo(left.x, left.y)
        close()
    }

    drawPath(
        path = diamond(point(26f, 4f), point(48f, 26f), point(26f, 48f), point(4f, 26f)),
        color = color,
        style = Stroke(width = 1.75f * scale),
    )
    drawPath(
        path = diamond(point(26f, 14f), point(38f, 26f), point(26f, 38f), point(14f, 26f)),
        color = color,
        style = Stroke(width = 1.25f * scale),
    )
    listOf(
        point(26f, 4f) to point(26f, 14f),
        point(48f, 26f) to point(38f, 26f),
        point(26f, 48f) to point(26f, 38f),
        point(4f, 26f) to point(14f, 26f),
    ).forEach { (start, end) ->
        drawLine(color = color, start = start, end = end, strokeWidth = 1f * scale)
    }
    drawCircle(color = color, radius = 2.5f * scale, center = point(26f, 26f))
}
