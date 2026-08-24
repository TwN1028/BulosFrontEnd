package com.example.bulosfrontend.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.R

val Aileron = FontFamily(
    Font(R.font.aileron_regular, FontWeight.Normal), Font(R.font.aileron_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.aileron_bold, FontWeight.Bold), Font(R.font.aileron_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.aileron_semibold, FontWeight.SemiBold), Font(R.font.aileron_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.aileron_light, FontWeight.Light), Font(R.font.aileron_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.aileron_thin, FontWeight.Thin), Font(R.font.aileron_thinitalic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.aileron_black, FontWeight.Black), Font(R.font.aileron_blackitalic, FontWeight.Black, FontStyle.Italic),
    Font(R.font.aileron_heavy, FontWeight.ExtraBold), Font(R.font.aileron_heavyitalic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.aileron_ultralight, FontWeight.ExtraLight), Font(R.font.aileron_ultralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
)

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-0.3).sp),
    displayMedium = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.Bold, fontSize = 33.sp, lineHeight = 39.sp, letterSpacing = (-0.2).sp),
    displaySmall = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.1).sp),
    headlineLarge = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.Bold, fontSize = 25.sp, lineHeight = 31.sp, letterSpacing = (-0.1).sp),
    headlineMedium = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp, letterSpacing = 0.sp),
    headlineSmall = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 25.sp, letterSpacing = 0.sp),
    titleLarge = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp, letterSpacing = 0.sp),
    titleSmall = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.sp),
    bodyLarge = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
    bodyMedium = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp, letterSpacing = 0.sp),
    bodySmall = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.Normal, fontSize = 10.sp, lineHeight = 15.sp, letterSpacing = 0.sp),
    labelLarge = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.sp),
    labelMedium = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.sp),
    labelSmall = TextStyle(
        fontFamily = Aileron,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.sp,
    ),
)
