package com.example.bulosfrontend

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.*

@Composable
fun BrandedSplashScreen(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        GeometricGreenBackground(Modifier.fillMaxSize(), cellSize = 52.dp, patternAlpha = 0.14f)
        val iconSize = minOf(maxWidth * 0.32f, 144.dp).coerceAtLeast(108.dp)
        val cornerRadius = iconSize * 0.24f
        val description = stringResource(R.string.splash_logo_content_description)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(iconSize)
                .shadow(18.dp, RoundedCornerShape(cornerRadius), ambientColor = Color.Black.copy(alpha = 0.28f))
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.linearGradient(
                        colors = listOf(DeepForestGreen, Color(0xFF426E49), SplashSage),
                    ),
                )
                .border(1.dp, SoftGreen.copy(alpha = 0.3f), RoundedCornerShape(cornerRadius))
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(iconSize * 0.08f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(iconSize * 0.3f))
                Icon(Icons.Default.Language, contentDescription = null, tint = SplashGold, modifier = Modifier.size(iconSize * 0.28f))
            }
        }
    }
}
