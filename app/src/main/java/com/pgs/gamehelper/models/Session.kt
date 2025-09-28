package com.pgs.gamehelper.models

data class Session(
    val id: String,
    val players: List<String>,
    val courts: Int,
    val hours: Int,
    val gameDuration: Int,
    val startedAt: String, // formatted datetime string
    val completedGames: Set<Int> = emptySet()
)
