package com.example.my_android_application.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager

// BroadcastReceiver отслеживает состояние вайфай на устройстве
class WifiReceiver(private val onWifiStateChanged: (Boolean) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val wifiState = intent?.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
        onWifiStateChanged(wifiState == WifiManager.WIFI_STATE_ENABLED)
    }
}
