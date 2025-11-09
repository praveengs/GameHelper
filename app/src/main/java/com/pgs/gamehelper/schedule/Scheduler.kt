package com.pgs.gamehelper.schedule

import com.pgs.gamehelper.models.Match
import com.pgs.gamehelper.models.PlayerStats
import java.util.Random

object Scheduler {

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

    fun generateSchedule(players: List<String>, courts: Int, totalGames: Int, seed: Long): List<List<Match>> {
        if (players.size < 4 || (players.size < courts * 4)) {
            return emptyList()
        }

        val schedule = mutableListOf<List<Match>>()
        val random = Random(seed) // For shuffling players within a game
        val playedGamePairs = emptySet<Set<String>>()
        var lastGamePairs = emptySet<Set<String>>()

        val numToRest = players.size - (courts * 4)
        // Create a mutable list to act as our rotating queue for round-robin
        val rotatingPlayers = players.toMutableList()

        repeat(totalGames) {
            // Round-robin selection for resting players
            val restingPlayers = rotatingPlayers.take(numToRest)
            val playingPlayers = rotatingPlayers.drop(numToRest)

            // The logic to form matches can be similar to V1, trying to avoid consecutive pairs
            var gameMatches = emptyList<Match>()
            var currentPairs = emptySet<Set<String>>()
            var foundPermutation = false

            // Attempt to find a permutation of players that doesn't result in consecutive pairs.
            for (attempt in 1..100) {
                val shuffledPlayingPlayers = playingPlayers.shuffled(random).toMutableList()
                val tempMatches = mutableListOf<Match>()
                val tempPairs = mutableSetOf<Set<String>>()
                var hasConsecutive = false

                for (c in 1..courts) {
                    if (shuffledPlayingPlayers.size >= 4) {
                        val teamA = listOf(shuffledPlayingPlayers.removeAt(0), shuffledPlayingPlayers.removeAt(0))
                        val teamB = listOf(shuffledPlayingPlayers.removeAt(0), shuffledPlayingPlayers.removeAt(0))
                        val pairA = teamA.toSet()
                        val pairB = teamB.toSet()

                        if (playedGamePairs.contains(pairA) || playedGamePairs.contains(pairB) || lastGamePairs.contains(pairA) || lastGamePairs.contains(pairB)) {
                            hasConsecutive = true
                            break // Exit court generation for this attempt
                        }
                        tempMatches.add(Match(teamA, teamB, resting = restingPlayers))
                        tempPairs.add(pairA)
                        tempPairs.add(pairB)
                    }
                }

                if (!hasConsecutive) {
                    gameMatches = tempMatches
                    currentPairs = tempPairs
                    foundPermutation = true
                    break // Exit attempt loop
                }
            }

            // Fallback if a non-consecutive permutation isn't found
            if (!foundPermutation) {
                val shuffledPlayingPlayers = playingPlayers.shuffled(random).toMutableList()
                val tempMatches = mutableListOf<Match>()
                val tempPairs = mutableSetOf<Set<String>>()

                repeat(courts) {
                    if (shuffledPlayingPlayers.size >= 4) {
                        val teamA = listOf(shuffledPlayingPlayers.removeAt(0), shuffledPlayingPlayers.removeAt(0))
                        val teamB = listOf(shuffledPlayingPlayers.removeAt(0), shuffledPlayingPlayers.removeAt(0))
                        tempMatches.add(Match(teamA, teamB, resting = restingPlayers))
                        tempPairs.add(teamA.toSet())
                        tempPairs.add(teamB.toSet())
                    }
                }
                gameMatches = tempMatches
                currentPairs = tempPairs
            }

            schedule.add(gameMatches)
            lastGamePairs = currentPairs
            playedGamePairs.plus(currentPairs)

            // Rotate the list for the next round-robin selection
            if (numToRest > 0) {
                java.util.Collections.rotate(rotatingPlayers, -numToRest)
            }
        }

        return schedule
    }
}
