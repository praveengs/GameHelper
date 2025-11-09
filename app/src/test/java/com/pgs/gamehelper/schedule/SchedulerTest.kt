package com.pgs.gamehelper.schedule

import com.pgs.gamehelper.models.MatchV2
import com.pgs.gamehelper.models.Player
import com.pgs.gamehelper.models.Team
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SchedulerTest {

    @Test
    fun `generateSchedule for 6 players, 1 court, 12 games ensures variety and correct rests`() {
        // Given
        val players = listOf("A", "B", "C", "D", "E", "F")
        val courts = 1
        val totalGames = 12
        val seed = 12345L

        // When
        val schedule = Scheduler.generateSchedule(players, courts, totalGames, seed)

        // Then
        // 1. Check if the schedule has the correct number of games
        assertEquals(totalGames, schedule.size)

        // 2. Iterate through the schedule to check rests and pair uniqueness
        for (i in 0 until schedule.size - 1) {
            val currentGame = schedule[i]
            val nextGame = schedule[i + 1]

            // --- Check Resting Players ---
            val playingInCurrent = currentGame.flatMap { it.teamA + it.teamB }.toSet()
            val restingInCurrent = players.toSet() - playingInCurrent
            val expectedRestingInCurrent = currentGame.first().resting.toSet()
            assertEquals("Resting players should match", expectedRestingInCurrent, restingInCurrent)

            // --- Check Consecutive Pairs ---
            val pairsInCurrent = currentGame.flatMap { listOf(it.teamA.toSet(), it.teamB.toSet()) }.toSet()
            val pairsInNext = nextGame.flatMap { listOf(it.teamA.toSet(), it.teamB.toSet()) }.toSet()

            // Check that there is no overlap in pairs between consecutive games
            val intersection = pairsInCurrent.intersect(pairsInNext)
            assertEquals("No pairs should be repeated in consecutive games. Overlap: $intersection", 0, intersection.size)
        }
    }

    @Test
    fun `generateScheduleV2 for 6 players, 1 court, 12 games ensures variety and correct rests`() {
        // Given
        val players = listOf(Player("A"), Player("B"), Player("C"), Player("D"), Player("E"), Player("F"))
        val courts = 1
        val totalGames = 12
        val seed = 12345L

        // When
        val schedule = Scheduler.generateScheduleV2(players, courts, totalGames, seed)

        // Then
        // 1. Check if the schedule has the correct number of games
        assertEquals(totalGames, schedule.size)

        // 2. Iterate through the schedule to check rests and pair uniqueness
        for (i in 0 until schedule.size - 1) {
            val currentGame = schedule[i]
            val nextGame = schedule[i + 1]


            // --- Check Resting Players ---
            val playingInCurrent = getPlayersInMatch(currentGame)
            val restingInCurrent = players.toSet() - playingInCurrent
            val expectedRestingInCurrent = currentGame.resting.toSet()
            assertEquals("Resting players should match", expectedRestingInCurrent, restingInCurrent)

            // --- Check Consecutive Pairs ---
            val pairsInCurrent = setOf(getPlayersInTeam(currentGame.teamA), getPlayersInTeam(currentGame.teamB))
            val pairsInNext = setOf(getPlayersInTeam(nextGame.teamA), getPlayersInTeam(nextGame.teamB))
            assertNotEquals("No pairs should be repeated in consecutive games. Overlap: $pairsInCurrent", pairsInCurrent, pairsInNext)

            // Check that there is no overlap in pairs between consecutive games
            val intersection = pairsInCurrent.intersect(pairsInNext)
            if (intersection.isNotEmpty()) {
                println("Intersection: $intersection")
            }
            assertEquals("No pairs should be repeated in consecutive games. Overlap: $intersection", 0, intersection.size)
        }
    }

    private fun getPlayersInMatch(match: MatchV2): Set<Player> = setOf(
        match.teamA.playerOne,
        match.teamA.playerTwo,
        match.teamB.playerOne,
        match.teamB.playerTwo
    )
    private fun getPlayersInTeam(team: Team): Set<Player> = setOf(
        team.playerOne,
        team.playerTwo,
    )
}

