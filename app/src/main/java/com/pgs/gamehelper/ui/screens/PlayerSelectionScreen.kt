package com.pgs.gamehelper.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
// Import necessary window insets
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pgs.gamehelper.models.PlayerViewModel
import com.pgs.gamehelper.models.SessionsViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSelectionScreen(
    navController: NavController,
    sessionViewModel: SessionsViewModel,
    playerViewModel: PlayerViewModel = viewModel(),
) {
    val players by playerViewModel.players.collectAsState()
    var selectedPlayers by remember { mutableStateOf(setOf<String>()) }
    var newPlayerName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val allSelected = players.isNotEmpty() && selectedPlayers.size == players.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Players") },
                navigationIcon = {
                    // Added a back button for better UX
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        // NEW: Apply window insets to the Scaffold to avoid system bars
        modifier = Modifier.windowInsetsPadding(WindowInsets.ime)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player list now uses weight to take up available space
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (players.isEmpty()) {
                    item {
                        Text("No saved players yet. Add some below.")
                    }
                } else {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = allSelected,
                                onCheckedChange = { checked ->
                                    selectedPlayers =
                                        if (checked) players.toSet()
                                        else emptySet()
                                }
                            )
                            Text("Select All")
                        }
                    }

                    items(players.toList().sorted()) { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedPlayers.contains(player),
                                onCheckedChange = { checked ->
                                    selectedPlayers =
                                        if (checked) selectedPlayers + player
                                        else selectedPlayers - player
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(player, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            // UI for adding a new player
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newPlayerName,
                    onValueChange = { newPlayerName = it },
                    label = { Text("New player name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newPlayerName.isNotBlank()) {
                            val trimmedName = newPlayerName.trim()
                            playerViewModel.addPlayer(trimmedName)
                            // Automatically select the newly added player
                            selectedPlayers = selectedPlayers + trimmedName
                            newPlayerName = ""
                        }
                    },
                    enabled = newPlayerName.isNotBlank()
                ) {
                    Text("Add")
                }
            }

            //Spacer(Modifier.height(16.dp)) // No longer needed with arrangement

            // "Next" button at the bottom
            Button(
                onClick = {
                    scope.launch {
                        val tempSession = sessionViewModel.getTempSession()
                        if (tempSession != null) {
                            sessionViewModel.addSession(
                                players = selectedPlayers.toList(),
                                courts = tempSession.courts,
                                hours = tempSession.hours,
                                gameDuration = tempSession.gameDuration
                            ) { session ->
                                navController.navigate(NavRoutes.Schedule(session.id).route) {
                                    popUpTo(NavRoutes.Sessions.route) { inclusive = false }
                                }
                            }
                        } else {
                            // Fallback navigation if temp session is null
                            navController.navigate(NavRoutes.Sessions.route)
                        }
                    }
                },
                enabled = selectedPlayers.size >= 4, // need minimum 4 players for doubles
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next")
            }
        }
    }
}
