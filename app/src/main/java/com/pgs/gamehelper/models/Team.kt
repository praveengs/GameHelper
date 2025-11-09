package com.pgs.gamehelper.models

data class Team(
    val playerOne: Player,
    val playerTwo: Player,
) {
    override fun toString(): String {
        return "[$playerOne, $playerTwo]"
    }
}
