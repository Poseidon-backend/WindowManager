package com.example.my_android_application.ui.screens

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.BroadcastReceiver
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.my_android_application.data.model.WifiData
import com.example.my_android_application.ui.components.BottomNavigationBar
import com.example.my_android_application.ui.components.WifiPasswordDialog
import com.example.my_android_application.viewmodel.WifiReceiverViewModel

@Composable
fun WifiScreen(navController: NavController, viewModel: WifiReceiverViewModel = viewModel()) {
    val context = LocalContext.current
    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    var isPermissionGranted by remember { mutableStateOf(true) }
    var scanErrorMessage by remember { mutableStateOf("") }
    var selectedNetwork by remember { mutableStateOf<ScanResult?>(null) }

    LaunchedEffect(Unit) {
        viewModel.updateWifiState(wifiManager.isWifiEnabled)
    }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            isPermissionGranted = isGranted
            scanErrorMessage = if (!isGranted) "Требуется разрешение на местоположение" else ""
        }

    val wifiReceiver = remember {
        WifiReceiver { wifiEnabled ->
            viewModel.updateWifiState(wifiEnabled)
        }
    }

    DisposableEffect(context) {
        val filter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        context.registerReceiver(wifiReceiver, filter)
        onDispose { context.unregisterReceiver(wifiReceiver) }
    }

    val scanReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val scanResults = wifiManager.scanResults
                    viewModel.wifiNetworks = scanResults
                } else {
                    scanErrorMessage = "Требуется разрешение на местоположение"
                }
            }
        }
    }

    DisposableEffect(context) {
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(scanReceiver, filter)
        onDispose { context.unregisterReceiver(scanReceiver) }
    }

    val wifiNetworks by viewModel.wifiFlow.collectAsState()
    val serverResponse by remember { derivedStateOf { viewModel.serverResponse } }
    val isWifiEnabled by remember { derivedStateOf { viewModel.isWifiEnabled } }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController, selectedIndex = 1) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isWifiEnabled) {
                Text(text = "Для работы приложения требуется включить Wi-Fi")
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!isPermissionGranted && scanErrorMessage.isNotEmpty()) {
                Text(
                    text = scanErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        viewModel.startWifiScan(context)
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                enabled = isWifiEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Сканировать сети Wi-Fi")
            }

            if (viewModel.isScanning) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            if (wifiNetworks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Доступные сети:")
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(wifiNetworks) { network ->
                        if (network.SSID.isNotEmpty()) {
                            Text(
                                text = network.SSID,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedNetwork = network }
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }

            selectedNetwork?.let { network ->
                WifiPasswordDialog(
                    ssid = network.SSID,
                    onDismiss = { selectedNetwork = null },
                    onSubmit = { password ->
                        val wifiData = WifiData(ssid = network.SSID, password = password)
                        viewModel.sendWifiCredentials(wifiData)
                        selectedNetwork = null
                    }
                )
            }

            serverResponse?.let { response ->
                Spacer(modifier = Modifier.height(16.dp))
                when {
                    response.status == "success" -> {
                        Text(
                            text = "Подключение успешно!",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    response.status == "error" && response.message.contains("Ошибка сервера") -> {
                        Text(
                            text = "Ошибка сервера: ${response.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    response.status == "error" && response.message.contains("Ошибка сети") -> {
                        Text(
                            text = "Ошибка сети: ${response.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    response.status == "error" -> {
                        Text(
                            text = "Пароль неверный",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}