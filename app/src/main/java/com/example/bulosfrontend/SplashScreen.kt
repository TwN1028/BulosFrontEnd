package com.example.bulosfrontend

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.BulosFrontEndTheme

private val SplashCream = Color(0xFFFFFBF4)
private val SplashForest = Color(0xFF173819)
private val SplashSage = Color(0xFF819976)
private val SplashGold = Color(0xFFE7B861)

@Composable
fun BrandedSplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.00f to SplashForest,
                    0.25f to Color(0xFF355B35),
                    0.55f to SplashSage,
                    0.78f to Color(0xFFC5CEB6),
                    1.00f to SplashCream,
                ),
            )
            .drawBehind {
                val glowCenter = Offset(size.width / 2f, size.height * 0.34f)
                drawCircle(
                    brush = Brush.radialGradient(
                        0.00f to Color(0xFFFFF4B8).copy(alpha = 0.72f),
                        0.28f to Color(0xFFF2EDB9).copy(alpha = 0.46f),
                        0.58f to Color(0xFFD9DEB4).copy(alpha = 0.18f),
                        1.00f to Color.Transparent,
                        center = glowCenter,
                        radius = size.width * 0.64f,
                    ),
                    center = glowCenter,
                    radius = size.width * 0.64f,
                )
            },
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
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
            val progressWidth = (maxWidth * 0.34f).coerceIn(132.dp, 170.dp)
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
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(progressWidth)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50)),
                    color = SplashGold,
                    trackColor = Color.White.copy(alpha = 0.62f),
                )
                Text(
                    text = stringResource(R.string.splash_preparing),
                    modifier = Modifier.padding(top = 18.dp),
                    color = Color(0xFF294E2C),
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
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
