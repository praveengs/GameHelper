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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pgs.gamehelper.data.CompletedGamesRepository
import com.pgs.gamehelper.models.SessionsViewModel
import com.pgs.gamehelper.schedule.Scheduler
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    context: Context,
    navController: NavController,
    sessionId: String,
    sessionsViewModel: SessionsViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val sessions by sessionsViewModel.sessions.collectAsState()
    val session = sessions.find { it.id == sessionId }

    if (session == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text("Session not found") }
        return
    }
    val players = session.players
    val courts = session.courts
    val totalGames = session.hours * 60 / session.gameDuration


    // Create a unique session ID (but add reshuffle seed later)
    var reshuffleSeed by remember { mutableStateOf(System.currentTimeMillis()) }
    var schedule by remember {
        mutableStateOf(
            Scheduler.generateSchedule(
                players,
                courts,
                totalGames
            )
        )
    }

    // Create a unique session ID
    val sessionId = remember(sessionId, reshuffleSeed) {
        sessionId
    }
    // Track completed games
    var completedGames by remember { mutableStateOf(setOf<Int>()) }
    // Load saved progress
    LaunchedEffect(sessionId) {
        CompletedGamesRepository.getCompletedGames(context, sessionId)
            .collect { saved ->
                completedGames = saved
            }
    }

    val playerStats = Scheduler.calculatePlayerStats(schedule, players)

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
                    IconButton(onClick = {
                        // Reshuffle schedule
                        reshuffleSeed = System.currentTimeMillis()
                        schedule = Scheduler.generateSchedule(players, courts, totalGames)
                        completedGames = emptySet()
                        scope.launch {
                            CompletedGamesRepository.saveCompletedGames(
                                context,
                                sessionId,
                                completedGames
                            )
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reshuffle"
                        )
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
                "Progress: ${completedGames.size} of $totalGames games completed",
                style = MaterialTheme.typography.bodyLarge
            )
            LinearProgressIndicator(
                progress = { (completedGames.size.toFloat() / totalGames).coerceIn(0f, 1f) },
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
                                    checked = completedGames.contains(gameIndex),
                                    onCheckedChange = { checked ->
                                        completedGames = completedGames.toMutableSet().apply {
                                            if (checked) add(gameIndex) else remove(gameIndex)
                                        }
                                        // Save progress
                                        scope.launch {
                                            CompletedGamesRepository.saveCompletedGames(
                                                context,
                                                sessionId,
                                                completedGames
                                            )
                                        }
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
