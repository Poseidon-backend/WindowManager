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

class WifiReceiverViewModel : ViewModel() {
    var wifiNetworks by mutableStateOf<List<ScanResult>>(emptyList())
    var isScanning by mutableStateOf(false)
    var serverResponse by mutableStateOf<WifiServerResponse?>(null)
    var isWifiEnabled by mutableStateOf(false) // Добавляем состояние Wi-Fi

    private val _wifiFlow = MutableStateFlow<List<ScanResult>>(emptyList())
    val wifiFlow: StateFlow<List<ScanResult>> = _wifiFlow

    // Обновляем состояние Wi-Fi из WifiReceiver
    fun updateWifiState(enabled: Boolean) {
        isWifiEnabled = enabled
        if (!enabled && isScanning) {
            stopWifiScan() // Останавливаем сканирование, если Wi-Fi выключен
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
                while (isScanning && isWifiEnabled) { // Проверяем оба условия
                    wifiManager.startScan()
                    val scanResults = wifiManager.scanResults
                    _wifiFlow.value = scanResults
                    delay(10000) // Задержка 10 секунд
                }
                isScanning = false // Устанавливаем false, когда цикл завершен
            }
        }
    }

    fun stopWifiScan() {
        isScanning = false // Метод для принудительной остановки
    }

    fun sendWifiCredentials(wifiData: WifiData) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.connectToWiFi(wifiData)
                serverResponse = if (response.isSuccessful) response.body()
                else WifiServerResponse("error", "Ошибка: ${response.code()}")
            } catch (e: Exception) {
                serverResponse = WifiServerResponse("error", "Ошибка: ${e.message}")
            }
        }
    }
}