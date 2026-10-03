package com.example.bulosfrontend

import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionStatusTest {
    @Test
    fun `offline takes precedence without validated connectivity`() {
        assertEquals(
            ConnectionStatus.OFFLINE,
            resolveConnectionStatus(connected = false, backendCheckInProgress = true, backendReady = true),
        )
    }

    @Test
    fun `validated connection reports waking only during health check`() {
        assertEquals(
            ConnectionStatus.WAKING_UP,
            resolveConnectionStatus(connected = true, backendCheckInProgress = true, backendReady = false),
        )
    }

    @Test
    fun `backend health success reports online`() {
        assertEquals(
            ConnectionStatus.ONLINE,
            resolveConnectionStatus(connected = true, backendCheckInProgress = false, backendReady = true),
        )
    }

    @Test
    fun `bounded health check failure reports server unavailable`() {
        assertEquals(
            ConnectionStatus.SERVER_UNAVAILABLE,
            resolveConnectionStatus(connected = true, backendCheckInProgress = false, backendReady = false),
        )
    }
}
