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
    bodyLarge = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    titleLarge = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.Normal, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp),
    labelSmall = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
).run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = Aileron), displayMedium = displayMedium.copy(fontFamily = Aileron), displaySmall = displaySmall.copy(fontFamily = Aileron),
        headlineLarge = headlineLarge.copy(fontFamily = Aileron), headlineMedium = headlineMedium.copy(fontFamily = Aileron), headlineSmall = headlineSmall.copy(fontFamily = Aileron),
        titleMedium = titleMedium.copy(fontFamily = Aileron), titleSmall = titleSmall.copy(fontFamily = Aileron),
        bodyMedium = bodyMedium.copy(fontFamily = Aileron), bodySmall = bodySmall.copy(fontFamily = Aileron),
        labelLarge = labelLarge.copy(fontFamily = Aileron), labelMedium = labelMedium.copy(fontFamily = Aileron)
    )
}
