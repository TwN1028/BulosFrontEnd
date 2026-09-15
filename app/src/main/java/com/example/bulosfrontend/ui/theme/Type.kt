package com.example.bulosfrontend.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import com.example.bulosfrontend.R
import com.example.bulosfrontend.Design

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
    bodyLarge = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.Normal, fontSize = Design.FontBodyLarge),
    titleLarge = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.Normal, fontSize = Design.FontTitleLarge),
    labelSmall = TextStyle(fontFamily = Aileron, fontWeight = FontWeight.Medium, fontSize = Design.FontLabelSmall),
).run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = Aileron, fontSize = Design.FontDisplayLarge),
        displayMedium = displayMedium.copy(fontFamily = Aileron, fontSize = Design.FontDisplayMedium),
        displaySmall = displaySmall.copy(fontFamily = Aileron, fontSize = Design.FontDisplaySmall),
        headlineLarge = headlineLarge.copy(fontFamily = Aileron, fontSize = Design.FontHeadlineLarge),
        headlineMedium = headlineMedium.copy(fontFamily = Aileron, fontSize = Design.FontHeadlineMedium),
        headlineSmall = headlineSmall.copy(fontFamily = Aileron, fontSize = Design.FontHeadlineSmall),
        titleMedium = titleMedium.copy(fontFamily = Aileron, fontSize = Design.FontTitleMedium),
        titleSmall = titleSmall.copy(fontFamily = Aileron, fontSize = Design.FontTitleSmall),
        bodyMedium = bodyMedium.copy(fontFamily = Aileron, fontSize = Design.FontBodyMedium),
        bodySmall = bodySmall.copy(fontFamily = Aileron, fontSize = Design.FontBodySmall),
        labelLarge = labelLarge.copy(fontFamily = Aileron, fontSize = Design.FontLabelLarge),
        labelMedium = labelMedium.copy(fontFamily = Aileron, fontSize = Design.FontLabelMedium)
    )
}
