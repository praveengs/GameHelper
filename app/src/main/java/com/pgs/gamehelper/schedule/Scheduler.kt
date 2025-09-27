package com.pgs.gamehelper.schedule

import com.pgs.gamehelper.models.Match
import com.pgs.gamehelper.models.PlayerStats

import kotlin.random.Random

object Scheduler {

    /**
     * Generate schedule of matches.
     * @param players list of player names
     * @param courts number of courts available
     * @param totalGames total number of games to schedule
     */
    fun generateSchedule(players: List<String>, courts: Int, totalGames: Int): List<List<Match>> {
        if (players.size < 4) return emptyList()

        val schedule = mutableListOf<List<Match>>()
        val shuffledPlayers = players.shuffled(Random(System.currentTimeMillis()))

        repeat(totalGames) { gameIndex ->
            val matchesForThisGame = mutableListOf<Match>()

            val rotation = shuffledPlayers.drop(gameIndex % players.size) +
                    shuffledPlayers.take(gameIndex % players.size)

            val availablePlayers = rotation.toMutableList()

            repeat(courts) {
                if (availablePlayers.size >= 4) {
                    val teamA = availablePlayers.take(2)
                    val teamB = availablePlayers.drop(2).take(2)
                    val match = Match(teamA, teamB, resting = players - (teamA + teamB))
                    matchesForThisGame.add(match)
                    availablePlayers.removeAll(teamA + teamB)
                }
            }
            schedule.add(matchesForThisGame)
        }

        return schedule
    }

    /**
     * Calculate basic stats for each player:
     * - games played
     * - rests
     */
    fun calculatePlayerStats(
        schedule: List<List<Match>>,
        players: List<String>
    ): Map<String, PlayerStats> {
        val stats = players.associateWith { PlayerStats(0, 0) }.toMutableMap()

        schedule.forEach { matches ->
            val playing = matches.flatMap { it.teamA + it.teamB }.toSet()

            players.forEach { player ->
                val current = stats[player] ?: PlayerStats(0, 0)
                stats[player] = if (player in playing) {
                    current.copy(gamesPlayed = current.gamesPlayed + 1)
                } else {
                    current.copy(rests = current.rests + 1)
                }
            }
        }

        return stats
    }
}