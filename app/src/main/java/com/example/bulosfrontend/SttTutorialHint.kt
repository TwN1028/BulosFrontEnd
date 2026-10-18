package com.example.bulosfrontend

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.ForestGreen

@Composable
internal fun SttTutorialHint(
    step: Int,
    title: String,
    body: String,
    bodyHighlight: String? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = HomeCardShape,
        color = Color(0xFFFFFBF4),
        border = HomeSupportingCardBorder,
        shadowElevation = 8.dp,
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
            Text(
                text = "STEP $step",
                color = Color(0xFF9B8C69),
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = title,
                color = ForestGreen,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(7.dp))
            val styledBody = buildAnnotatedString {
                val highlight = bodyHighlight?.takeIf { body.contains(it) }
                if (highlight == null) {
                    append(body)
                } else {
                    val highlightStart = body.indexOf(highlight)
                    append(body.substring(0, highlightStart))
                    withStyle(SpanStyle(color = ForestGreen, fontWeight = FontWeight.Bold)) {
                        append(highlight)
                    }
                    append(body.substring(highlightStart + highlight.length))
                }
            }
            Text(
                text = styledBody,
                color = TranslationInputTextColor,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                ),
            )
        }
    }
}

/** A non-intercepting visual cue anchored beside a real tutorial target. */
@Composable
internal fun TutorialTapCue(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "tutorialTapCue")
    val lift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(720, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tutorialTapCueLift",
    )
    Surface(
        modifier = modifier.graphicsLayer { translationY = lift },
        shape = HomeCardShape,
        color = Color.White.copy(alpha = 0.68f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.82f)),
        shadowElevation = 5.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Default.TouchApp, null, modifier = Modifier.size(18.dp), tint = ForestGreen)
            Text("Tap a language", color = ForestGreen, fontWeight = FontWeight.SemiBold)
        }
    }
}
