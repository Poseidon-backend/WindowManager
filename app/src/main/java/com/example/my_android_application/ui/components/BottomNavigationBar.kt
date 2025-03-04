package com.example.my_android_application.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Build

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun BottomNavigationBar(navController: NavController, selectedIndex: Int) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = {
                if (selectedIndex != 0) {
                    navController.navigate("instructionScreen")
                }
            },
            label = { Text("Инструкция") },
            icon = {
                val iconColor = if (selectedIndex == 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
                Icon(Icons.Filled.Info, contentDescription = null, tint = iconColor)
            },
            enabled = selectedIndex != 0,
            alwaysShowLabel = true
        )

        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = {
                if (selectedIndex != 1) {
                    navController.navigate("wifiScreen")
                }
            },
            label = { Text("Подключение") },
            icon = { val iconColor = if (selectedIndex == 1) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
                Icon(Icons.Filled.Build, contentDescription = null, tint = iconColor)
            },
            enabled = selectedIndex != 1,
            alwaysShowLabel = true
        )
    }
}

