package com.pgs.gamehelper.models

data class CourtGame(
val courtNumber: Int,
val teamA: Pair<String, String>,
val teamB: Pair<String, String>,
val sittingOut: List<String>
)