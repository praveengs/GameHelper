package com.pgs.gamehelper.schedule

import com.pgs.gamehelper.models.Match
import com.pgs.gamehelper.models.PlayerStats
import java.util.Random

object Scheduler {

    fun generateSchedule(players: List<String>, courts: Int, totalGames: Int, seed: Long): List<List<Match>> {
        if (players.size < 4 || (players.size < courts * 4)) {
            return emptyList()
        }

        val schedule = mutableListOf<List<Match>>()
        val random = Random(seed)

        val playerRestCounts = players.associateWith { 0 }.toMutableMap()

        val numToRest = players.size - (courts * 4)

        repeat(totalGames) {
            val restingPlayers = if (numToRest > 0) {
                // To ensure fair rests, we prioritize players who have rested less.
                // We shuffle players before sorting to randomly break ties among players with equal rest counts.
                players.shuffled(random).sortedBy { playerRestCounts[it] }.take(numToRest)
            } else {
                emptyList()
            }

            restingPlayers.forEach {
                playerRestCounts[it] = playerRestCounts.getValue(it) + 1
            }

            val playingPlayers = (players - restingPlayers).shuffled(random).toMutableList()

            val gameMatches = mutableListOf<Match>()
            repeat(courts) {
                if (playingPlayers.size >= 4) {
                    val teamA = listOf(playingPlayers.removeAt(0), playingPlayers.removeAt(0))
                    val teamB = listOf(playingPlayers.removeAt(0), playingPlayers.removeAt(0))
                    gameMatches.add(Match(teamA, teamB, resting = restingPlayers))
                }
            }
            schedule.add(gameMatches)
        }

        return schedule
    }

    fun calculatePlayerStats(
        schedule: List<List<Match>>,
        players: List<String>
    ): Map<String, PlayerStats> {
        val playerStats = players.associateWith { PlayerStats(0, 0) }.toMutableMap()
        schedule.forEach { game ->
            val playersInGame = game.flatMap { it.teamA + it.teamB }.toSet()

            players.forEach { player ->
                val currentStats = playerStats.getValue(player)
                if (player in playersInGame) {
                    playerStats[player] = currentStats.copy(gamesPlayed = currentStats.gamesPlayed + 1)
                } else {
                    playerStats[player] = currentStats.copy(rests = currentStats.rests + 1)
                }
            }
        }
        return playerStats
    }
}
