package com.example.bulosfrontend

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.PatternGreen
import com.example.bulosfrontend.ui.theme.PrimaryGreen

internal val HomeContentCream = Color(0xFFFFFBF4)
internal val AppHeaderBottomExtension = 69.dp
internal val AppHeaderTitleTopPadding = 28.dp
internal val AppHeaderTailHeight = 51.dp
internal val TranslationCardMinHeight = 348.dp

@Composable
fun HomeHeaderBackground(
    modifier: Modifier = Modifier,
    includeCreamTail: Boolean = true,
    includePattern: Boolean = true,
) {
    Box(
        modifier = modifier.background(homeHeaderGradientBrush(includeCreamTail)),
    ) {
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

fun homeHeaderGradientBrush(includeCreamTail: Boolean = true): Brush {
    return Brush.verticalGradient(*homeHeaderGradientStops(includeCreamTail))
}

fun appHeaderGradientBrush(): Brush =
    Brush.verticalGradient(*homeHeaderGradientStops(includeCreamTail = true))

fun speechTranslationGradientBrush(): Brush =
    Brush.verticalGradient(*homeHeaderGradientStops(includeCreamTail = true))

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
