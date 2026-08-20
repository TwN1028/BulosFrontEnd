package com.example.bulosfrontend

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Design {
    // Colors
    val EarthlyBrown = Color(0xFF5D4037)
    val OnEarthlyBrown = Color(0xFFFFF8E1)
    val ButtonBrown = Color(0xFF795548)
    val OnButtonBrown = Color(0xFFFFF8E1)
    val LightForestGreen = Color(0xFFA5D6A7)
    val DarkForestGreen = Color(0xFF1B5E20)
    val LightButtonRed = Color(0xFFEF9A9A)
    val DarkButtonRed = Color(0xFFB71C1C)

    // Alpha & Transparency
    val AlphaSecondary = 0.5f
    val AlphaFooter = 0.7f

    // Shapes
    val ButtonShape = RoundedCornerShape(12.dp)
    val CardShape = RoundedCornerShape(16.dp)
    val DashboardShape = RoundedCornerShape(24.dp)
    val Circle = RoundedCornerShape(50)

    // Dimensions - Paddings & Spacings
    val PaddingScreen = 32.dp
    val PaddingElement = 16.dp
    val PaddingSmall = 8.dp
    val SpacingLarge = 24.dp
    val SpacingMedium = 16.dp
    val SpacingSmall = 8.dp
    
    // Dimensions - Heights & Sizes
    val ButtonHeightLarge = 80.dp
    val ButtonHeightMedium = 64.dp
    val ButtonHeightSmall = 48.dp
    val ButtonHeightSelector = 56.dp
    
    val IconSizeLarge = 200.dp
    val IconSizeMedium = 120.dp
    val IconSizeSmall = 48.dp
    val IconSizeMini = 24.dp
    val IconSizeHistory = 12.dp
    val IconPadding = 8.dp
    
    val DashboardButtonSize = 160.dp
    val ToastPaddingH = 24.dp
    val ToastPaddingV = 8.dp

    // Typography SP
    val FontDisplayLarge = 64.sp
    val FontDisplayMedium = 52.sp
    val FontDisplaySmall = 42.sp
    val FontHeadlineLarge = 40.sp
    val FontHeadlineMedium = 34.sp
    val FontHeadlineSmall = 30.sp
    val FontTitleLarge = 28.sp
    val FontTitleMedium = 22.sp
    val FontTitleSmall = 18.sp
    val FontBodyLarge = 20.sp
    val FontBodyMedium = 18.sp
    val FontBodySmall = 16.sp
    val FontLabelLarge = 18.sp
    val FontLabelMedium = 16.sp
    val FontLabelSmall = 14.sp
    
    val TimerFontSize = 60.sp
    val FontBold = FontWeight.Bold

    // Animation
    val PopDuration = 100
    val PopScale = 1.2f

    // Layout Helpers
    val FillMax = Modifier.fillMaxSize()
    val FillWidth = Modifier.fillMaxWidth()
    val CenterH = Alignment.CenterHorizontally
    val CenterV = Alignment.CenterVertically
    val CenterArr = Arrangement.Center
    val CenterAlign = Alignment.Center

    @Composable
    fun primaryColors() = ButtonDefaults.buttonColors(containerColor = LightForestGreen, contentColor = DarkForestGreen)
    @Composable
    fun secondaryColors() = ButtonDefaults.buttonColors(containerColor = ButtonBrown, contentColor = OnButtonBrown)
    @Composable
    fun dangerColors() = ButtonDefaults.buttonColors(containerColor = LightButtonRed, contentColor = DarkButtonRed)
}
