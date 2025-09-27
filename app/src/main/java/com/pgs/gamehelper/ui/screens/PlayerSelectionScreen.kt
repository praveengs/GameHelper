package com.pgs.gamehelper.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pgs.gamehelper.models.PlayerViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSelectionScreen(
    navController: NavController,
    viewModel: PlayerViewModel = viewModel()
) {
    val players by viewModel.players.collectAsState()
    var selectedPlayers by remember { mutableStateOf(setOf<String>()) }
    var newPlayerName by remember { mutableStateOf("") }

    val allSelected = players.isNotEmpty() && selectedPlayers.size == players.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Players") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (players.isEmpty()) {
                Text("No saved players yet. Add some below.")
            } else {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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

                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .padding(top = 8.dp)
                ) {
                    items(players.toList()) { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedPlayers.contains(player),
                                onCheckedChange = { checked ->
                                    selectedPlayers =
                                        if (checked) selectedPlayers + player
                                        else selectedPlayers - player
                                }
                            )
                            Text(player, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            // Add new player inline
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newPlayerName,
                    onValueChange = { newPlayerName = it },
                    label = { Text("New player name") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newPlayerName.isNotBlank()) {
                            viewModel.addPlayer(newPlayerName.trim())
                            selectedPlayers = selectedPlayers + newPlayerName.trim()
                            newPlayerName = ""
                        }
                    }
                ) {
                    Text("Add")
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val courts = navController.previousBackStackEntry
                        ?.savedStateHandle?.get<Int>("courts") ?: 1
                    val hours = navController.previousBackStackEntry
                        ?.savedStateHandle?.get<Int>("hours") ?: 2
                    val gameDuration = navController.previousBackStackEntry
                        ?.savedStateHandle?.get<Int>("gameDuration") ?: 10

                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                        set("players", selectedPlayers.toList())
                        set("courts", courts)
                        set("hours", hours)
                        set("gameDuration", gameDuration)
                    }
                    navController.navigate(NavRoutes.Schedule.route)
                },
                enabled = selectedPlayers.size >= 4, // need minimum 4 players for doubles
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next")
            }
        }
    }
}
