package com.pgs.gamehelper.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pgs.gamehelper.models.MatchResult
import com.pgs.gamehelper.models.SessionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreEntryScreen(
    navController: NavController,
    sessionId: String,
    matchId: String,
    teamA: List<String>,
    teamB: List<String>,
    sessionsViewModel: SessionsViewModel = viewModel()
) {
    val sessions by sessionsViewModel.sessions.collectAsState()
    val session = sessions.find { it.id == sessionId }

    if (session == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Session not found")
        }
        return
    }

    val existingResult = session.matchResults[matchId] ?: MatchResult()

    var teamAScore by remember { mutableStateOf(existingResult.teamAScore.toString()) }
    var teamBScore by remember { mutableStateOf(existingResult.teamBScore.toString()) }
    var shuttlesUsed by remember { mutableStateOf(existingResult.shuttlesUsed) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter Score") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Team display
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                TeamScoreInput(teamName = "Team A", players = teamA, score = teamAScore, onScoreChange = { teamAScore = it })
                TeamScoreInput(teamName = "Team B", players = teamB, score = teamBScore, onScoreChange = { teamBScore = it })
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // Shuttle counter
            ShuttleCounter(shuttlesUsed = shuttlesUsed, onShuttlesChanged = { shuttlesUsed = it })

            Spacer(modifier = Modifier.weight(1f))

            // Done button
            Button(
                onClick = {
                    val newResult = MatchResult(
                        teamAScore = teamAScore.toIntOrNull() ?: 0,
                        teamBScore = teamBScore.toIntOrNull() ?: 0,
                        shuttlesUsed = shuttlesUsed
                    )
                    val updatedResults = session.matchResults.toMutableMap().apply {
                        this[matchId] = newResult
                    }
                    sessionsViewModel.updateSession(session.copy(matchResults = updatedResults))
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
fun TeamScoreInput(
    teamName: String,
    players: List<String>,
    score: String,
    onScoreChange: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(teamName, style = MaterialTheme.typography.titleMedium)
        players.forEach { playerName ->
            Text(playerName, style = MaterialTheme.typography.bodyMedium)
        }
        OutlinedTextField(
            value = score,
            onValueChange = onScoreChange,
            label = { Text("Score") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(100.dp)
        )
    }
}

@Composable
fun ShuttleCounter(shuttlesUsed: Int, onShuttlesChanged: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Shuttles Used", style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = { if (shuttlesUsed > 0) onShuttlesChanged(shuttlesUsed - 1) }) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease shuttles")
        }
        Text(shuttlesUsed.toString(), style = MaterialTheme.typography.headlineSmall)
        IconButton(onClick = { onShuttlesChanged(shuttlesUsed + 1) }) {
            Icon(Icons.Default.Add, contentDescription = "Increase shuttles")
        }
    }
}
