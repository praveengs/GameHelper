package com.pgs.gamehelper.ui.screens

sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object PlayerManager : NavRoutes("player_manager")
    object CourtConfig : NavRoutes("court_config")
    object PlayerSelection : NavRoutes("player_selection")
    object Schedule : NavRoutes("schedule")
}