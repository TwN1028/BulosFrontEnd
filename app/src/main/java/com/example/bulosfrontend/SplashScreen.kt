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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.SplashSage

@Composable
fun BrandedSplashScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        GeometricGreenBackground(
            modifier = Modifier.fillMaxSize(),
            cellSize = 52.dp,
            patternAlpha = 0.14f,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.navigationBars)),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = Color.Black.copy(alpha = 0.24f),
                            spotColor = Color.Black.copy(alpha = 0.18f),
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(SplashSage.copy(alpha = 0.42f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(28.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // Replace this drawable with the official app icon when available.
                    Image(
                        painter = painterResource(R.drawable.ic_app_logo_placeholder),
                        contentDescription = stringResource(R.string.splash_logo_content_description),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(58.dp),
                    )
                }

                Spacer(Modifier.height(28.dp))
                Text(
                    text = stringResource(R.string.splash_title),
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(11.dp))
                Text(
                    text = stringResource(R.string.splash_subtitle),
                    color = Color.White.copy(alpha = 0.76f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(22.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(170.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50)),
                    color = Color(0xFFF5C47C),
                    trackColor = Color.White.copy(alpha = 0.18f),
                )
            }

            Text(
                text = stringResource(R.string.splash_initiative),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                color = Color.White.copy(alpha = 0.36f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}
