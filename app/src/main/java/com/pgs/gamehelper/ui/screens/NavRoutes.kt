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
    data class ScoreEntry(
        val sessionId: String,
        val matchId: String,
        val teamA: List<String>,
        val teamB: List<String>
    ) : NavRoutes("score_entry/$sessionId/$matchId/${teamA.joinToString(",")}/${teamB.joinToString(",")}") {
        companion object {
            const val routePattern = "score_entry/{sessionId}/{matchId}/{teamA}/{teamB}"
        }
    }
}
