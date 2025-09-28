package com.pgs.gamehelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pgs.gamehelper.models.SessionsViewModel
import com.pgs.gamehelper.ui.screens.CourtConfigScreen
import com.pgs.gamehelper.ui.screens.HomeScreen
import com.pgs.gamehelper.ui.screens.NavRoutes
import com.pgs.gamehelper.ui.screens.PlayerManagerScreen
import com.pgs.gamehelper.ui.screens.PlayerSelectionScreen
import com.pgs.gamehelper.ui.screens.ScheduleScreen
import com.pgs.gamehelper.ui.screens.SessionsScreen
import com.pgs.gamehelper.ui.theme.GameHelperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameHelperTheme {
                GameHelper()
            }
        }
    }
}

@Composable
private fun GameHelper() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        val navController = rememberNavController()
        val context = LocalContext.current
        
        // Create shared ViewModels
        val sessionsViewModel: SessionsViewModel = remember { SessionsViewModel.Factory(context).create(SessionsViewModel::class.java) }

        NavHost(
            navController = navController,
            startDestination = NavRoutes.Home.route,
            // Apply the innerPadding to the NavHost
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.Home.route) { HomeScreen(navController) }
            composable(NavRoutes.Sessions.route) { SessionsScreen(context, navController, sessionsViewModel) }
            composable(NavRoutes.PlayerManager.route) { PlayerManagerScreen(navController) }
            composable(NavRoutes.CourtConfig.route) { CourtConfigScreen(navController, sessionsViewModel) }
            composable(NavRoutes.PlayerSelection.route) { PlayerSelectionScreen(navController, sessionsViewModel) }
            composable(
                NavRoutes.Schedule.routePattern,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sessionId =
                    backStackEntry.arguments?.getString("sessionId")!!
                ScheduleScreen(context, navController, sessionId, sessionsViewModel)
            }
        }
    }
}