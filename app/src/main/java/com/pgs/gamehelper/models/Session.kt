package com.pgs.gamehelper.models

import com.pgs.gamehelper.models.MatchResult

data class Session(
    val id: String,
    val players: List<String>,
    val courts: Int,
    val hours: Int,
    val gameDuration: Int,
    val startedAt: String, // formatted datetime string
    val completedGames: Set<Int> = emptySet(),
    val matchResults: Map<String, MatchResult> = emptyMap(),
    val isLocked: Boolean = false,
    val reshuffleSeed: Long = 0L
)
