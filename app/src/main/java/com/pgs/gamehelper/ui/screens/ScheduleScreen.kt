package com.pgs.gamehelper.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pgs.gamehelper.R
import com.pgs.gamehelper.models.Match
import com.pgs.gamehelper.models.MatchResult
import com.pgs.gamehelper.models.PlayerStats
import com.pgs.gamehelper.models.Session
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
            ScheduleScreenTopAppBar(
                session = session,
                onBackClick = { navController.popBackStack() },
                onReshuffleClick = {
                    val newSeed = System.currentTimeMillis()
                    sessionsViewModel.updateSession(
                        session.copy(
                            reshuffleSeed = newSeed,
                            completedGames = emptySet(),
                            matchResults = emptyMap()
                        )
                    )
                },
                onLockClick = {
                    sessionsViewModel.updateSession(session.copy(isLocked = true))
                }
            )
        }
    ) { innerPadding ->
        ScheduleScreenContent(
            modifier = Modifier.padding(innerPadding),
            session = session,
            totalGames = totalGames,
            schedule = schedule,
            playerStats = playerStats,
            onGameCompletedChange = { gameIndex, isCompleted ->
                val updatedGames = session.completedGames.toMutableSet().apply {
                    if (isCompleted) add(gameIndex) else remove(gameIndex)
                }
                sessionsViewModel.updateSession(session.copy(completedGames = updatedGames))
            },
            onScoreClick = { matchId, teamA, teamB ->
                val teamAString = teamA.joinToString(",")
                val teamBString = teamB.joinToString(",")
                navController.navigate(
                    "score_entry/${session.id}/$matchId/$teamAString/$teamBString"
                )
            }
        )
    }
}

@Composable
private fun ScheduleScreenContent(
    modifier: Modifier = Modifier,
    session: Session,
    totalGames: Int,
    schedule: List<List<Match>>,
    playerStats: Map<String, PlayerStats>,
    onGameCompletedChange: (Int, Boolean) -> Unit,
    onScoreClick: (String, List<String>, List<String>) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Progress tracker
        ProgressTracker(session, totalGames)

        Spacer(Modifier.height(16.dp))

        // Game Schedule List
        GameScheduleList(
            modifier = Modifier.weight(1f),
            schedule = schedule,
            session = session,
            onGameCompletedChange = onGameCompletedChange,
            onScoreClick = onScoreClick
        )

        Spacer(Modifier.height(16.dp))

        // Game stats summary
        GameStats(session = session)

        Spacer(Modifier.height(16.dp))

        // Player summary
        PlayerStats(playerStats)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleScreenTopAppBar(
    session: Session,
    onBackClick: () -> Unit,
    onReshuffleClick: () -> Unit,
    onLockClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Schedule") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            if (session.isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Schedule Locked",
                    modifier = Modifier.padding(end = 16.dp)
                )
            } else {
                IconButton(onClick = onReshuffleClick) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reshuffle"
                    )
                }
                IconButton(onClick = onLockClick) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Lock Schedule"
                    )
                }
            }
        }
    )
}

@Composable
private fun PlayerStats(playerStats: Map<String, PlayerStats>) {
    Text("Summary", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    // Make the summary scrollable in case of many players
    LazyColumn {
        items(playerStats.toList().sortedBy { it.first }) { (player, stats) ->
            Text(
                "$player: ${stats.gamesPlayed} games, ${stats.rests} rests",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun GameStats(session: Session) {
    val totalShuttles = session.matchResults.values.sumOf { it.shuttlesUsed }
    if (totalShuttles > 0) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Game Stats", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Total shuttles used: $totalShuttles")
            }
        }
    }
}

@Composable
private fun GameScheduleList(
    modifier: Modifier = Modifier,
    schedule: List<List<Match>>,
    session: Session,
    onGameCompletedChange: (Int, Boolean) -> Unit,
    onScoreClick: (String, List<String>, List<String>) -> Unit
) {
    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(schedule) { gameIndex, courtsSchedule ->
            val matchIdPrefix = "${session.id}-G$gameIndex"
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
                            onCheckedChange = { isChecked ->
                                onGameCompletedChange(gameIndex, isChecked)
                            },
                            enabled = session.isLocked
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    courtsSchedule.forEachIndexed { courtIndex, match ->
                        val matchId = "$matchIdPrefix-C$courtIndex"
                        GameMatchRow(
                            courtIndex = courtIndex,
                            match = match,
                            isSessionLocked = session.isLocked,
                            matchResult = session.matchResults[matchId],
                            onScoreClick = {
                                onScoreClick(matchId, match.teamA, match.teamB)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameMatchRow(
    courtIndex: Int,
    match: Match,
    isSessionLocked: Boolean,
    matchResult: MatchResult?,
    onScoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // Increased vertical padding for more separation
    ) {
        // --- Top Row: Court Info and Score Button ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Court ${courtIndex + 1}",
                style = MaterialTheme.typography.titleSmall, // Made it slightly smaller but still prominent
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = onScoreClick,
                enabled = isSessionLocked,
                // Reduced padding to make the button more compact
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Text("Score")
            }
        }

        Spacer(Modifier.height(8.dp)) // Add space before the match details

        // --- Middle Row: Teams and Scores ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Team A
            MatchTeam(
                players = match.teamA,
                score = matchResult?.teamAScore,
                alignment = Arrangement.Start
            )

            Text("vs", style = MaterialTheme.typography.bodySmall)

            // Team B
            MatchTeam(
                players = match.teamB,
                score = matchResult?.teamBScore,
                alignment = Arrangement.End
            )
        }

        // --- Bottom Row: Resting players and Shuttle count ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Resting players (only shows if there are any)
            if (match.resting.isNotEmpty()) {
                Text(
                    text = "Rest: ${match.resting.joinToString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.weight(1f)) // Fills space if no one is resting
            }

            // Shuttle count (only shows if score has been entered)
            if (matchResult != null && matchResult.shuttlesUsed > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shuttle),
                        contentDescription = "Shuttle icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp) // Made icon smaller to be less intrusive
                    )
                    Text(
                        text = "${matchResult.shuttlesUsed}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


/**
 * A new composable to display a team's players and score.
 * This cleans up the GameMatchRow logic.
 */
@Composable
private fun RowScope.MatchTeam(
    players: List<String>,
    score: Int?,
    alignment: Arrangement.Horizontal
) {
    Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = alignment
    ) {
        // Player Tags
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Logic to handle layout based on alignment
            if (alignment == Arrangement.End) {
                PlayerTag(name = players[0])
                PlayerTag(name = players[1])
            } else {
                PlayerTag(name = players[0])
                PlayerTag(name = players[1])
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Score
        Text(
            text = score?.toString() ?: "", // Show empty string if score is null
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(32.dp) // Fixed width to ensure alignment
        )
    }
}


@Composable
private fun ProgressTracker(session: Session, totalGames: Int) {
    Column {
        Text(
            "Progress: ${session.completedGames.size} of $totalGames games completed",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(8.dp))
        val progress = if (totalGames > 0) {
            (session.completedGames.size.toFloat() / totalGames).coerceIn(0f, 1f)
        } else {
            0f
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
    }
}

@Composable
fun PlayerTag(name: String) {
    val colors = listOf(
        Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFF81C784),
        Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF4DB6AC),
        Color(0xFFA1887F), Color(0xFF7986CB), Color(0xFFF06292)
    )
    val color = remember(name) { colors[name.hashCode().absoluteValue % colors.size] }

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
