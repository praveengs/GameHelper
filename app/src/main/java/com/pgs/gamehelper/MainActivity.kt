package com.pgs.gamehelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pgs.gamehelper.ui.screens.CourtConfigScreen
import com.pgs.gamehelper.ui.screens.HomeScreen
import com.pgs.gamehelper.ui.screens.NavRoutes
import com.pgs.gamehelper.ui.screens.PlayerManagerScreen
import com.pgs.gamehelper.ui.screens.PlayerSelectionScreen
import com.pgs.gamehelper.ui.screens.ScheduleScreen
import com.pgs.gamehelper.ui.theme.GameHelperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameHelperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.Home.route,
                        // Apply the innerPadding to the NavHost
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(NavRoutes.Home.route) { HomeScreen(navController) }
                        composable(NavRoutes.PlayerManager.route) { PlayerManagerScreen(navController) }
                        composable(NavRoutes.CourtConfig.route) { CourtConfigScreen(navController) }
                        composable(NavRoutes.PlayerSelection.route) { PlayerSelectionScreen(navController) }
                        composable(NavRoutes.Schedule.route) { ScheduleScreen(navController) }
                    }
                }
            }
        }
    }
}