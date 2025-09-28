package com.pgs.gamehelper.ui.screens

sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object Sessions : NavRoutes("sessions")
    object PlayerManager : NavRoutes("player_manager")
    object CourtConfig : NavRoutes("court_config")
    object PlayerSelection : NavRoutes("player_selection")
    data class Schedule(val sessionId: String) : NavRoutes("schedule/$sessionId") {
        companion object {
            const val routePattern = "schedule/{sessionId}"
        }
    }
}