package com.pgs.gamehelper.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pgs.gamehelper.models.SessionsViewModel
import com.pgs.gamehelper.schedule.Scheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    context: Context,
    navController: NavController,
    sessionId: String,
    sessionsViewModel: SessionsViewModel = viewModel()
) {
    val sessions by sessionsViewModel.sessions.collectAsState()
    val session = sessions.find { it.id == sessionId }

    if (session == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text("Session not found") }
        return
    }

    // If this is a new session or an older one without a seed, generate one and save it.
    LaunchedEffect(session.id, session.reshuffleSeed) {
        if (session.reshuffleSeed == 0L) {
            sessionsViewModel.updateSession(session.copy(reshuffleSeed = System.currentTimeMillis()))
        }
    }

    val players = session.players
    val courts = session.courts
    val totalGames = session.hours * 60 / session.gameDuration

    val schedule = remember(session.reshuffleSeed) {
        if (session.reshuffleSeed == 0L) {
            emptyList()
        } else {
            Scheduler.generateSchedule(players, courts, totalGames, session.reshuffleSeed)
        }
    }

    val playerStats = remember(schedule) {
        Scheduler.calculatePlayerStats(schedule, players)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (!session.isLocked) {
                        IconButton(onClick = {
                            // Reshuffle schedule
                            val newSeed = System.currentTimeMillis()
                            sessionsViewModel.updateSession(
                                session.copy(
                                    reshuffleSeed = newSeed,
                                    completedGames = emptySet()
                                )
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reshuffle"
                            )
                        }
                        IconButton(onClick = {
                            sessionsViewModel.updateSession(
                                session.copy(isLocked = true)
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Lock Schedule"
                            )
                        }
                    }
                })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Progress tracker
            Text(
                "Progress: ${session.completedGames.size} of $totalGames games completed",
                style = MaterialTheme.typography.bodyLarge
            )
            LinearProgressIndicator(
                progress = { (session.completedGames.size.toFloat() / totalGames).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            Spacer(Modifier.height(8.dp))

            // Game schedule
            LazyColumn(
                modifier = Modifier.weight(1f, fill = true)
            ) {
                itemsIndexed(schedule) { gameIndex, courtsSchedule ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Game ${gameIndex + 1}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Checkbox(
                                    checked = session.completedGames.contains(gameIndex),
                                    onCheckedChange = { checked ->
                                        val updatedGames =
                                            session.completedGames.toMutableSet().apply {
                                                if (checked) add(gameIndex) else remove(gameIndex)
                                            }
                                        sessionsViewModel.updateSession(
                                            session.copy(completedGames = updatedGames)
                                        )
                                    }
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            courtsSchedule.forEachIndexed { courtIndex, match ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        "Court ${courtIndex + 1}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        PlayerTag(match.teamA[0])
                                        PlayerTag(match.teamA[1])
                                        Text("vs")
                                        PlayerTag(match.teamB[0])
                                        PlayerTag(match.teamB[1])
                                    }
                                    if (match.resting.isNotEmpty()) {
                                        Text(
                                            "Resting: ${match.resting.joinToString()}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Player summary
            Text("Summary", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            playerStats.forEach { (player, stats) ->
                Text(
                    "$player: ${stats.gamesPlayed} games, ${stats.rests} rests",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun PlayerTag(name: String) {
    val colors = listOf(
        Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFF81C784),
        Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF4DB6AC),
        Color(0xFFA1887F), Color(0xFF7986CB), Color(0xFFF06292)
    )
    val color = colors[name.hashCode().absoluteValue % colors.size]

    Box(
        modifier = Modifier
            .background(color, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(name, color = Color.White, style = MaterialTheme.typography.bodySmall)
    }
}

private val Int.absoluteValue: Int
    get() = if (this < 0) -this else this
