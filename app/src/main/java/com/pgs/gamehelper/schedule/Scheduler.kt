package com.pgs.gamehelper.schedule

import com.pgs.gamehelper.models.Game
import com.pgs.gamehelper.models.Match
import com.pgs.gamehelper.models.MatchV2
import com.pgs.gamehelper.models.Player
import com.pgs.gamehelper.models.PlayerStats
import com.pgs.gamehelper.models.Team
import kotlin.random.Random

object Scheduler {

    fun generateSchedule(players: List<String>, courts: Int, totalGames: Int, seed: Long): List<List<Match>> {
        if (players.size < 4 || (players.size < courts * 4)) {
            return emptyList()
        }

        val schedule = mutableListOf<List<Match>>()
        val random = Random(seed)

        val playerRestCounts = players.associateWith { 0 }.toMutableMap()
        var lastGamePairs = emptySet<Set<String>>()

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

            val playingPlayers = (players - restingPlayers).toMutableList()
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

                        if (lastGamePairs.contains(pairA) || lastGamePairs.contains(pairB)) {
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

            // If no non-consecutive permutation is found after 100 attempts, use the original logic for this game.
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

    fun generateScheduleV2(players: List<Player>, courts: Int, totalGames: Int, seed: Long): List<MatchV2> {
        if (players.size < 4 || (players.size < courts * 4)) {
            return emptyList()
        }
        val random = Random(seed)
        val partnerHistory = mutableMapOf<Pair<Player, Player>, Int>()
        val opponentHistory = mutableMapOf<Pair<Player, Player>, Int>()
        val gameCounts = players.associateWith { 0 }.toMutableMap()

        var lastGamePlayers = emptySet<Player>()

        fun addPair(map: MutableMap<Pair<Player, Player>, Int>, a: Player, b: Player) {
            val key = if (a.name < b.name) a to b else b to a
            map[key] = map.getOrDefault(key, 0) + 1
        }

        val allGames = mutableListOf<MatchV2>()

        repeat(totalGames) { gameIndex ->
            // Sort players by play count + random jitter + penalty if they just played
            val sortedPlayers = players.sortedBy {
                val restPenalty = if (it in lastGamePlayers) 1.5 else 0.0
                gameCounts[it]!! + restPenalty + Random.nextDouble(0.0, 0.3)
            }

            val chosen = sortedPlayers.take(4).shuffled(random)

            // Try all possible team combinations
            val possibleTeams = listOf(
                Team(chosen[0], chosen[1]) to Team(chosen[2], chosen[3]),
                Team(chosen[0], chosen[2]) to Team(chosen[1], chosen[3]),
                Team(chosen[0], chosen[3]) to Team(chosen[1], chosen[2])
            )

            val bestPairing = possibleTeams.minByOrNull { (t1, t2) ->
                val partnerScore =
                    partnerHistory.getOrDefault(t1.playerOne to t1.playerTwo, 0) +
                            partnerHistory.getOrDefault(t2.playerOne to t2.playerTwo, 0)
                val opponentScore =
                    listOf(
                        opponentHistory.getOrDefault(t1.playerOne to t2.playerOne, 0),
                        opponentHistory.getOrDefault(t1.playerOne to t2.playerTwo, 0),
                        opponentHistory.getOrDefault(t1.playerTwo to t2.playerOne, 0),
                        opponentHistory.getOrDefault(t1.playerTwo to t2.playerTwo, 0)
                    ).sum()
                // Add mild penalty if any player just played last game
                val restPenalty = (listOf(t1.playerOne, t1.playerTwo, t2.playerOne, t2.playerTwo).count { it in lastGamePlayers }) * 2
                partnerScore + opponentScore + restPenalty
            }!!

            val (teamA, teamB) = bestPairing
            val playingNow = setOf(teamA.playerOne, teamA.playerTwo, teamB.playerOne, teamB.playerTwo)
            val resting = players.filterNot { it in playingNow }
            allGames += MatchV2(teamA, teamB, resting)

            // Update histories
            addPair(partnerHistory, teamA.playerOne, teamA.playerTwo)
            addPair(partnerHistory, teamB.playerOne, teamB.playerTwo)
            listOf(
                teamA.playerOne to teamB.playerOne, teamA.playerOne to teamB.playerTwo,
                teamA.playerTwo to teamB.playerOne, teamA.playerTwo to teamB.playerTwo
            ).forEach { addPair(opponentHistory, it.first, it.second) }

            // Update game counts
            playingNow.forEach { gameCounts[it] = gameCounts[it]!! + 1 }

            // Save current players to penalize in next iteration
            lastGamePlayers = playingNow
        }

        return allGames
    }

    fun generateScheduleV3(players: List<Player>, courts: Int, totalGames: Int, seed: Long): List<MatchV2> {
        if (players.size < 4 || (players.size < courts * 4)) {
            return emptyList()
        }
        val random = Random(seed)
        val partnerHistory = mutableMapOf<Team, Int>()
        val opponentHistory = mutableMapOf<Team, Int>()
        val gameCounts = players.associateWith { 0 }.toMutableMap()

        var lastGamePlayers = emptySet<Player>()

        val allGames = mutableListOf<MatchV2>()

        repeat(totalGames) { gameIndex ->
            // Sort players by play count + random jitter + penalty if they just played
            val sortedPlayers = players.sortedBy {
                val restPenalty = if (it in lastGamePlayers) 1.5 else 0.0
                gameCounts[it]!! + restPenalty + Random.nextDouble(0.0, 0.3)
            }

            val chosen = sortedPlayers.take(4).shuffled(random)

            // Try all possible team combinations
            val possibleTeams = listOf(
                Team(chosen[0], chosen[1]) to Team(chosen[2], chosen[3]),
                Team(chosen[0], chosen[2]) to Team(chosen[1], chosen[3]),
                Team(chosen[0], chosen[3]) to Team(chosen[1], chosen[2])
            )

            val bestPairing = possibleTeams.minByOrNull { (t1, t2) ->
                val partnerScore =
                    partnerHistory.getOrDefault(t1.playerOne to t1.playerTwo, 0) +
                            partnerHistory.getOrDefault(t2.playerOne to t2.playerTwo, 0)
                val opponentScore =
                    listOf(
                        opponentHistory.getOrDefault(t1.playerOne to t2.playerOne, 0),
                        opponentHistory.getOrDefault(t1.playerOne to t2.playerTwo, 0),
                        opponentHistory.getOrDefault(t1.playerTwo to t2.playerOne, 0),
                        opponentHistory.getOrDefault(t1.playerTwo to t2.playerTwo, 0)
                    ).sum()
                // Add mild penalty if any player just played last game
                val restPenalty = (listOf(t1.playerOne, t1.playerTwo, t2.playerOne, t2.playerTwo).count { it in lastGamePlayers }) * 2
                partnerScore + opponentScore + restPenalty
            }!!

            val (teamA, teamB) = bestPairing
            val playingNow = setOf(teamA.playerOne, teamA.playerTwo, teamB.playerOne, teamB.playerTwo)
            val resting = players.filterNot { it in playingNow }
            allGames += MatchV2(teamA, teamB, resting)

            // Update histories
            addPair(partnerHistory, teamA.playerOne, teamA.playerTwo)
            addPair(partnerHistory, teamB.playerOne, teamB.playerTwo)
            listOf(
                teamA.playerOne to teamB.playerOne, teamA.playerOne to teamB.playerTwo,
                teamA.playerTwo to teamB.playerOne, teamA.playerTwo to teamB.playerTwo
            ).forEach { addPair(opponentHistory, it.first, it.second) }

            // Update game counts
            playingNow.forEach { gameCounts[it] = gameCounts[it]!! + 1 }

            // Save current players to penalize in next iteration
            lastGamePlayers = playingNow
        }

        return allGames
    }
}
