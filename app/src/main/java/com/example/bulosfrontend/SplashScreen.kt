package com.example.bulosfrontend

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.BulosFrontEndTheme

private val DarkForestGreen = Color(0xFF1E3F20)
private val MutedGreen = Color(0xFF5E7A4A)
private val PaleCreamGreen = Color(0xFFEAEBD9)
private val SplashCream = Color(0xFFFFFBF4)

@Composable
fun BrandedSplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to DarkForestGreen,
                        0.45f to DarkForestGreen,
                        0.78f to MutedGreen,
                        1.00f to PaleCreamGreen,
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.navigationBars)),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = Color.Black.copy(alpha = 0.16f),
                            spotColor = Color.Black.copy(alpha = 0.12f),
                        )
                        .clip(RoundedCornerShape(32.dp))
                        .background(SplashCream)
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.72f),
                            shape = RoundedCornerShape(32.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // Replace this drawable with the official app icon when available.
                    Image(
                        painter = painterResource(R.drawable.ic_app_logo_placeholder),
                        contentDescription = stringResource(R.string.splash_logo_content_description),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(76.dp),
                    )
                }

                Spacer(Modifier.height(30.dp))
                Text(
                    text = stringResource(R.string.splash_title),
                    color = SplashCream,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 38.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(11.dp))
                Text(
                    text = stringResource(R.string.splash_subtitle),
                    color = Color.White.copy(alpha = 0.76f),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(22.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(170.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50)),
                    color = Color(0xFFE7B861),
                    trackColor = Color.White.copy(alpha = 0.38f),
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
