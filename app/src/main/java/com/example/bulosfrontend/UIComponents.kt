package com.example.bulosfrontend

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTopAppBar(title: String) = TopAppBar(
    title = { Text(title) },
    navigationIcon = {
        Icon(painterResource(R.drawable.ic_launcher_foreground), stringResource(R.string.app_logo_content_description), Modifier.size(Design.IconSizeSmall).padding(Design.IconPadding))
    },
    colors = TopAppBarDefaults.topAppBarColors(Design.EarthlyBrown, titleContentColor = Design.OnEarthlyBrown, navigationIconContentColor = Design.OnEarthlyBrown)
)

@Composable
fun BulosButton(onClick: () -> Unit, text: String, modifier: Modifier = Modifier, enabled: Boolean = true, colors: ButtonColors? = null) = 
    Button(onClick, modifier, enabled, shape = Design.ButtonShape, colors = colors ?: Design.secondaryColors()) {
        Text(text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }

@Composable
fun PoppingIconButton(onClick: () -> Unit, icon: ImageVector, contentDescription: String?, modifier: Modifier = Modifier) {
    var clicked by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (clicked) Design.PopScale else 1f, tween(Design.PopDuration), finishedListener = { clicked = false }, label = "Pop")
    Button(onClick = { clicked = true; onClick() }, modifier.size(Design.IconSizeSmall).scale(scale), shape = Design.ButtonShape, contentPadding = PaddingValues(0.dp), colors = Design.secondaryColors()) {
        Icon(icon, contentDescription, Modifier.size(Design.IconSizeMini))
    }
}

@Composable
fun TopToastNotification(visible: Boolean, message: String, p: PaddingValues) = Box(Design.FillMax) {
    AnimatedVisibility(visible, Modifier.align(Alignment.TopCenter).padding(top = p.calculateTopPadding() + Design.PaddingElement), enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()) {
        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = Design.CardShape, tonalElevation = 4.dp) {
            Text(message, Modifier.padding(horizontal = Design.ToastPaddingH, vertical = Design.ToastPaddingV), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun StandardFooter(res: Int) = Text(stringResource(res), Modifier.padding(bottom = Design.PaddingElement).alpha(Design.AlphaFooter), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Light, fontFamily = Aileron))
