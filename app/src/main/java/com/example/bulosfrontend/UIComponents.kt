package com.example.bulosfrontend

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.bulosfrontend.ui.theme.Aileron

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTopAppBar(title: String) = TopAppBar(
    title = { Text(title) },
    navigationIcon = {
        Icon(
            painterResource(R.drawable.ic_launcher_foreground),
            stringResource(R.string.app_logo_content_description),
            Modifier.size(48.dp).padding(8.dp),
        )
    },
)

@Composable
fun PoppingIconButton(onClick: () -> Unit, icon: ImageVector, contentDescription: String?, modifier: Modifier = Modifier) {
    var isClicked by remember { mutableStateOf(value = false) }
    val scale by animateFloatAsState(
        targetValue = if (isClicked) 1.2f else 1f,
        animationSpec = tween(durationMillis = 100),
        finishedListener = { isClicked = false },
        label = "Pop",
    )
    Button(
        onClick = {
            isClicked = true
            onClick()
        },
        modifier = modifier.size(48.dp).scale(scale),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
    ) {
        Icon(icon, contentDescription, Modifier.size(24.dp))
    }
}

@Composable
fun TopToastNotification(visible: Boolean, message: String, innerPadding: PaddingValues) {
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = innerPadding.calculateTopPadding() + 16.dp),
        ) {
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(16.dp), tonalElevation = 4.dp) {
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
fun StandardFooter(textRes: Int) = Text(
    text = stringResource(textRes),
    modifier = Modifier.padding(bottom = 16.dp).alpha(0.7f),
    textAlign = TextAlign.Center,
    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Light, fontFamily = Aileron),
)
