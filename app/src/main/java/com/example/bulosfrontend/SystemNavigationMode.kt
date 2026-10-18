package com.example.bulosfrontend

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

internal enum class SystemNavigationMode { GESTURE, BUTTONS }

/**
 * Android has no public navigation-mode flag. The relationship between the navigation-bar and
 * mandatory-gesture insets is the supported, layout-focused signal: gesture navigation reserves
 * the same small bottom area for both, while a button bar reserves a larger tappable area.
 * Unavailable or ambiguous insets deliberately fall back to button navigation so controls remain
 * accessible through the existing floating menu.
 */
@Composable
internal fun rememberSystemNavigationMode(): SystemNavigationMode {
    val view = LocalView.current
    var navigationMode by remember(view) {
        mutableStateOf(systemNavigationMode(ViewCompat.getRootWindowInsets(view)))
    }

    DisposableEffect(view) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            navigationMode = systemNavigationMode(insets)
            insets
        }
        ViewCompat.requestApplyInsets(view)
        onDispose { ViewCompat.setOnApplyWindowInsetsListener(view, null) }
    }
    return navigationMode
}

internal fun systemNavigationMode(insets: WindowInsetsCompat?): SystemNavigationMode {
    if (insets == null) return SystemNavigationMode.BUTTONS
    val navigationBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    val mandatoryGestureBottom = insets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures()).bottom
    return systemNavigationMode(navigationBottom, mandatoryGestureBottom)
}

internal fun systemNavigationMode(
    navigationBottom: Int,
    mandatoryGestureBottom: Int,
): SystemNavigationMode {
    return if (navigationBottom > 0 && mandatoryGestureBottom > 0 &&
        navigationBottom <= mandatoryGestureBottom + 8
    ) {
        SystemNavigationMode.GESTURE
    } else {
        SystemNavigationMode.BUTTONS
    }
}
