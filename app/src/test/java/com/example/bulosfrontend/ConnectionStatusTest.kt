package com.example.bulosfrontend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class ConnectionStatusTest {
    @Test
    fun `health checks use the required three-attempt backoff`() {
        assertArrayEquals(longArrayOf(0L, 2_000L, 4_000L), BACKEND_HEALTH_RETRY_DELAYS_MS)
    }

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

    @Test
    fun `user selected offline mode takes precedence over connectivity and backend state`() {
        assertEquals(
            ConnectionStatus.OFFLINE_MODE,
            resolveConnectionStatus(
                connected = true,
                backendCheckInProgress = false,
                backendReady = true,
                offlineModeEnabled = true,
            ),
        )
    }

    @Test
    fun `online services require connectivity and offline mode disabled`() {
        assertEquals(true, canUseOnlineServices(hasInternetConnection = true, offlineModeEnabled = false))
        assertEquals(false, canUseOnlineServices(hasInternetConnection = false, offlineModeEnabled = false))
        assertEquals(false, canUseOnlineServices(hasInternetConnection = true, offlineModeEnabled = true))
        assertEquals(false, canUseOnlineServices(hasInternetConnection = false, offlineModeEnabled = true))
    }
}
