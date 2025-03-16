package com.example.my_android_application.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_android_application.data.model.WifiData
import com.example.my_android_application.data.response.WifiServerResponse
import com.example.my_android_application.network.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log

class WifiReceiverViewModel : ViewModel() {
    var wifiNetworks by mutableStateOf<List<ScanResult>>(emptyList())
    var isScanning by mutableStateOf(false)
    var serverResponse by mutableStateOf<WifiServerResponse?>(null)
    var isWifiEnabled by mutableStateOf(false)

    private val _wifiFlow = MutableStateFlow<List<ScanResult>>(emptyList())
    val wifiFlow: StateFlow<List<ScanResult>> = _wifiFlow

    fun updateWifiState(enabled: Boolean) {
        isWifiEnabled = enabled
        if (!enabled && isScanning) {
            stopWifiScan()
        }
    }

    fun startWifiScan(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModelScope.launch {
                val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                isScanning = true
                while (isScanning && isWifiEnabled) {
                    wifiManager.startScan()
                    val scanResults = wifiManager.scanResults
                    _wifiFlow.value = scanResults
                    delay(10000)
                }
                isScanning = false
            }
        }
    }

    private fun stopWifiScan() {
        isScanning = false
    }

    fun sendWifiCredentials(wifiData: WifiData) {
        Log.d("WifiData", "Sending: SSID=${wifiData.ssid}, Password=${wifiData.password}")
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.connectToWiFi(wifiData)
                if (response.isSuccessful) {
                    val serverBody = response.body()
                    Log.d("ServerResponse", "Received: ${serverBody?.toString()}")
                    serverResponse =
                        serverBody ?: WifiServerResponse("error", "Нет ответа от сервера")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ServerError", "HTTP Error: ${response.code()} - $errorBody")
                    serverResponse =
                        WifiServerResponse("error", "Ошибка сервера: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("NetworkError", "Exception: ${e.message}")
                serverResponse = WifiServerResponse("error", "Ошибка сети: ${e.message}")
            }
        }
    }
}