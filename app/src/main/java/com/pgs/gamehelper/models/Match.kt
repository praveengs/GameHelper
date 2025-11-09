package com.pgs.gamehelper.models

data class Match(
    val teamA: List<String>,
    val teamB: List<String>,
    val resting:List<String> = emptyList()
)

