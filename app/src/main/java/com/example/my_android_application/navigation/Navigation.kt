package com.example.my_android_application.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.my_android_application.ui.screens.InstructionScreen
import com.example.my_android_application.ui.screens.WifiScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "instructionScreen") {
        composable("instructionScreen") {
            InstructionScreen(navController)
        }
        composable("wifiscreen") {

            WifiScreen(navController)
        }

    }
}
