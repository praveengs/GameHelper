package com.pgs.gamehelper.models

data class GameBlock(
val gameNumber: Int,
val startTime: String,
val courts: List<CourtGame>,
var completed: Boolean = false
)