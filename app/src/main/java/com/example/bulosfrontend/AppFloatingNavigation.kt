package com.example.bulosfrontend

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.NavigationCream

private data class FloatingNavigationItem(
    @StringRes val labelRes: Int,
    val icon: Painter,
    val route: String,
)

/** An inset-aware overlay that does not reserve or resize screen content. */
@Composable
fun AppFloatingNavigation(
    currentRoute: String?,
    content: HomeDialogueContent,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    activeFunctionRoute: String? = currentRoute,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val dismissInteraction = remember { MutableInteractionSource() }
    val items = listOf(
        FloatingNavigationItem(content.navSpeechRes, painterResource(R.drawable.ic_lucide_mic), AppDestinations.HOME_RECORDING),
        FloatingNavigationItem(content.navTranslateRes, painterResource(R.drawable.ic_lucide_languages), AppDestinations.TEXT),
        FloatingNavigationItem(content.historyTitleRes, painterResource(R.drawable.ic_saved_history_reference), AppDestinations.HISTORY),
        FloatingNavigationItem(content.navDictionaryRes, painterResource(R.drawable.ic_dictionary_book), AppDestinations.DICTIONARY),
        FloatingNavigationItem(content.settingsTitleRes, painterResource(R.drawable.ic_lucide_settings), AppDestinations.MORE),
    )

    BackHandler(enabled = expanded) { expanded = false }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.union(WindowInsets.ime)),
    ) {
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = dismissInteraction,
                        indication = null,
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.close_navigation_menu),
                    ) { expanded = false },
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            val availableOptionsHeight = (this.maxHeight - 62.dp).coerceAtLeast(0.dp)
            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom) +
                        scaleIn(transformOrigin = TransformOrigin(1f, 1f)),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom) +
                        scaleOut(transformOrigin = TransformOrigin(1f, 1f)),
                ) {
                    Column(
                        modifier = Modifier
                            .heightIn(max = availableOptionsHeight)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items.forEach { item ->
                            val selected = item.route == activeFunctionRoute
                            val label = stringResource(item.labelRes)
                            Surface(
                                modifier = Modifier
                                    .widthIn(min = 164.dp, max = 232.dp)
                                    .semantics { this.selected = selected }
                                    .clickable(role = Role.Tab, onClickLabel = label) {
                                        expanded = false
                                        if (!selected) onNavigate(item.route)
                                    },
                                shape = RoundedCornerShape(18.dp),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                } else {
                                    NavigationCream
                                },
                                contentColor = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                    else TranslationInputBorderColor.copy(alpha = 0.55f),
                                ),
                                shadowElevation = 6.dp,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Icon(
                                        painter = item.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = stringResource(
                                if (expanded) R.string.close_navigation_menu
                                else R.string.open_navigation_menu,
                            ),
                        ) { expanded = !expanded },
                    shape = RoundedCornerShape(18.dp),
                    color = NavigationCream,
                    contentColor = MaterialTheme.colorScheme.primary,
                    border = BorderStroke(1.dp, TranslationInputBorderColor.copy(alpha = 0.55f)),
                    shadowElevation = 8.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_more_horizontal),
                            contentDescription = stringResource(
                                if (expanded) R.string.close_navigation_menu
                                else R.string.open_navigation_menu,
                            ),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}
