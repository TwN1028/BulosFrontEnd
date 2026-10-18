package com.example.bulosfrontend

import org.junit.Assert.assertEquals
import org.junit.Test

class SystemNavigationModeTest {
    @Test
    fun `gesture-sized navigation and mandatory gesture insets select bottom navigation`() {
        assertEquals(
            SystemNavigationMode.GESTURE,
            systemNavigationMode(navigationBottom = 24, mandatoryGestureBottom = 24),
        )
    }

    @Test
    fun `larger button navigation inset retains floating navigation`() {
        assertEquals(
            SystemNavigationMode.BUTTONS,
            systemNavigationMode(navigationBottom = 48, mandatoryGestureBottom = 0),
        )
    }

    @Test
    fun `ambiguous inset data safely falls back to floating navigation`() {
        assertEquals(
            SystemNavigationMode.BUTTONS,
            systemNavigationMode(navigationBottom = 0, mandatoryGestureBottom = 0),
        )
    }
}
