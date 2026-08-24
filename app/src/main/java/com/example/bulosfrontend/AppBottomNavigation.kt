package com.example.bulosfrontend

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import com.example.bulosfrontend.ui.theme.WarmWhite
import com.example.bulosfrontend.ui.theme.VoiceReviewOrange

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
        BottomItem(content.navHomeRes, painterResource(R.drawable.ic_lucide_home), AppDestinations.HOME),
        BottomItem(content.navTranslateRes, painterResource(R.drawable.ic_lucide_languages), AppDestinations.TEXT),
        BottomItem(content.navSpeechRes, painterResource(R.drawable.ic_lucide_mic), AppDestinations.VOICE),
        BottomItem(content.navDictionaryRes, painterResource(R.drawable.ic_lucide_book_open), AppDestinations.DICTIONARY),
        BottomItem(content.navMoreRes, painterResource(R.drawable.ic_lucide_more_horizontal), AppDestinations.MORE),
    )
    Box(modifier.fillMaxWidth().navigationBarsPadding().height(92.dp)) {
        Column(Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
            HorizontalDivider(color = HomeCardBorder.copy(alpha = 0.8f), thickness = 1.dp)
            NavigationBar(
                modifier = Modifier.fillMaxWidth().height(70.dp),
                containerColor = NavigationCream,
                tonalElevation = 0.dp,
            ) {
                items.forEachIndexed { index, item ->
                    if (index == 2) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = { onNavigate(item.route) },
                            icon = {
                                Icon(
                                    painter = item.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(21.dp),
                                )
                            },
                            label = {
                                Text(
                                    stringResource(item.labelRes),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        lineHeight = 11.sp,
                                        letterSpacing = 0.sp,
                                    ),
                                    fontWeight = if (currentRoute == item.route) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ForestGreen,
                                selectedTextColor = ForestGreen,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = WarmBrown.copy(alpha = 0.78f),
                                unselectedTextColor = WarmBrown.copy(alpha = 0.88f),
                            ),
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FloatingActionButton(
                onClick = { onNavigate(AppDestinations.VOICE) },
                modifier = Modifier.size(56.dp).shadow(7.dp, CircleShape),
                shape = CircleShape,
                containerColor = if (currentRoute == AppDestinations.VOICE) VoiceReviewOrange else ForestGreen,
                contentColor = WarmWhite,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_mic),
                    contentDescription = stringResource(content.navSpeechRes),
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                stringResource(content.navSpeechRes),
                color = if (currentRoute == AppDestinations.VOICE) VoiceReviewOrange else WarmBrown,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    letterSpacing = 0.sp,
                ),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
