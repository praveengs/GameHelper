package com.pgs.gamehelper.schedule

import com.pgs.gamehelper.models.Match
import com.pgs.gamehelper.models.PlayerStats
import kotlin.random.Random

object Scheduler {

    fun generateSchedule(players: List<String>, courts: Int, totalGames: Int, seed: Long): List<List<Match>> {
        if (players.size < 4) return emptyList()

        val schedule = mutableListOf<List<Match>>()
        val random = Random(seed)
        val shuffledPlayers = players.shuffled(random)

        repeat(totalGames) { gameIndex ->
            val gameMatches = mutableListOf<Match>()
            val rotation = shuffledPlayers.drop(gameIndex % players.size) +
                    shuffledPlayers.take(gameIndex % players.size)

            val availablePlayers = rotation.toMutableList()
            repeat(courts) {
                if (availablePlayers.size >= 4) {
                    val teamA = availablePlayers.take(2)
                    val teamB = availablePlayers.drop(2).take(2)
                    gameMatches.add(Match(teamA, teamB, resting = players - (teamA + teamB)))
                    availablePlayers.removeAll(teamA + teamB)
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
            game.forEach { match ->
                (match.teamA + match.teamB).forEach {
                    playerStats[it] = playerStats[it]!!.copy(gamesPlayed = playerStats[it]!!.gamesPlayed + 1)
                }
                match.resting.forEach {
                    playerStats[it] = playerStats[it]!!.copy(rests = playerStats[it]!!.rests + 1)
                }
            }
        }
        return playerStats
    }
}
