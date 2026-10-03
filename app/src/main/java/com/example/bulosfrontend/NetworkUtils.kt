package com.example.bulosfrontend

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

enum class ConnectionStatus {
    ONLINE,
    OFFLINE,
    WAKING_UP,
    SERVER_UNAVAILABLE,
}

internal fun resolveConnectionStatus(
    connected: Boolean,
    backendCheckInProgress: Boolean,
    backendReady: Boolean,
): ConnectionStatus = when {
    !connected -> ConnectionStatus.OFFLINE
    backendCheckInProgress -> ConnectionStatus.WAKING_UP
    backendReady -> ConnectionStatus.ONLINE
    else -> ConnectionStatus.SERVER_UNAVAILABLE
}

object NetworkUtils {
    fun isOnline(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}

class NetworkMonitor(
    context: Context,
    private val onConnectivityChanged: (Boolean) -> Unit,
) {
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var registered = false
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = dispatchCurrentState()
        override fun onLost(network: Network) = dispatchCurrentState()
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) =
            dispatchCurrentState()
    }

    fun start() {
        if (registered) return
        registered = runCatching {
            connectivityManager.registerDefaultNetworkCallback(callback)
            true
        }.getOrDefault(false)
        dispatchCurrentState()
    }

    fun stop() {
        if (!registered) return
        runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        registered = false
    }

    private fun dispatchCurrentState() {
        val network = connectivityManager.activeNetwork
        val capabilities = network?.let(connectivityManager::getNetworkCapabilities)
        onConnectivityChanged(
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
        )
    }
}
