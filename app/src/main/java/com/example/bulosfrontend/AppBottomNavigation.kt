package com.example.bulosfrontend

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulosfrontend.ui.theme.ForestGreen
import com.example.bulosfrontend.ui.theme.HomeCardBorder
import com.example.bulosfrontend.ui.theme.NavigationCream
import com.example.bulosfrontend.ui.theme.WarmBrown

private data class BottomItem(
    @StringRes val labelRes: Int,
    val icon: Painter,
    val route: String,
)

@Composable
fun AppBottomNavigation(
    currentRoute: String?,
    content: HomeDialogueContent,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        BottomItem(content.navSpeechRes, painterResource(R.drawable.ic_lucide_mic), AppDestinations.VOICE),
        BottomItem(content.navTranslateRes, painterResource(R.drawable.ic_lucide_languages), AppDestinations.TEXT),
        BottomItem(content.historyTitleRes, painterResource(R.drawable.ic_lucide_history), AppDestinations.HISTORY),
        BottomItem(content.navDictionaryRes, painterResource(R.drawable.ic_dictionary_book), AppDestinations.DICTIONARY),
        BottomItem(content.settingsTitleRes, painterResource(R.drawable.ic_lucide_settings), AppDestinations.MORE),
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
        NavigationBar(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            items.filterNot { it.route == currentRoute }.forEach { item ->
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate(item.route) },
                    icon = {
                        Icon(
                            painter = item.icon,
                            contentDescription = stringResource(item.labelRes),
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    label = {
                        Text(
                            stringResource(item.labelRes),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                lineHeight = 10.sp,
                                letterSpacing = 0.sp,
                            ),
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}
