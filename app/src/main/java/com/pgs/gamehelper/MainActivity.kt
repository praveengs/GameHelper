package com.pgs.gamehelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pgs.gamehelper.models.CourtGame
import com.pgs.gamehelper.models.GameBlock
import com.pgs.gamehelper.ui.screens.InputScreen
import com.pgs.gamehelper.ui.screens.ScheduleScreen
import com.pgs.gamehelper.ui.theme.GameHelperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameHelperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    var schedule by remember { mutableStateOf<List<GameBlock>>(emptyList()) }
                    var playerNames by remember { mutableStateOf(listOf<String>()) }


                    NavHost(
                        navController = navController,
                        startDestination = "input",
                        // Apply the innerPadding to the NavHost
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("input") {
                            InputScreen(onGenerate = { players, courtHours, gameDuration, courtCount ->
                                schedule = generateSchedule(
                                    players,
                                    courtHours * 60,
                                    gameDuration,
                                    courtCount
                                )
                                playerNames = players
                                navController.navigate("schedule")
                            })
                        }
                        composable("schedule") {
                            ScheduleScreen(schedule, playerNames, navController)
                        }
                    }
                }
            }
        }
    }
}

fun generateSchedule(
    players: List<String>,
    courtMinutes: Int,
    gameDuration: Int = 10,
    courtCount: Int = 1
): List<GameBlock> {
    val totalGames = courtMinutes / gameDuration
    val schedule = mutableListOf<GameBlock>()


    val playerRestCounter = mutableMapOf<String, Int>().apply {
        players.forEach { this[it] = 0 }
    }


    val rotations = players.toMutableList()
    var currentTime = 0


    for (i in 1..totalGames) {
        val courts = mutableListOf<CourtGame>()
        val chosenPlayers = mutableSetOf<String>()


        for (c in 1..courtCount) {
            if (rotations.size >= 4) {
// Sort by rest count so players with more rest get priority to play
                rotations.sortByDescending { playerRestCounter[it] ?: 0 }


                val teamA = Pair(rotations[0], rotations[1])
                val teamB = Pair(rotations[2], rotations[3])
                val sitting = rotations.drop(4)


                courts.add(CourtGame(c, teamA, teamB, sitting))
                chosenPlayers.addAll(listOf(rotations[0], rotations[1], rotations[2], rotations[3]))


// Update rest counters
                players.forEach { p ->
                    if (chosenPlayers.contains(p)) {
                        playerRestCounter[p] = 0
                    } else {
                        playerRestCounter[p] = (playerRestCounter[p] ?: 0) + 1
                    }
                }


// Rotate players to vary teams
                rotations.add(rotations.removeAt(1))
            }
        }


        val block = GameBlock(
            gameNumber = i,
            startTime = "%02d:%02d".format(currentTime / 60, currentTime % 60),
            courts = courts
        )
        schedule.add(block)


        currentTime += gameDuration
    }


    return schedule
}