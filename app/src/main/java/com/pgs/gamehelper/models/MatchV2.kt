package com.pgs.gamehelper.models

data class MatchV2(
    val teamA: Team,
    val teamB: Team,
    val resting:List<Player> = emptyList()
) {
    override fun toString(): String {
        return "$teamA vs $teamB, resting=$resting)"
    }
}