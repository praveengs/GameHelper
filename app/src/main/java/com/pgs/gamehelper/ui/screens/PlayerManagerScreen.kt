package com.pgs.gamehelper.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pgs.gamehelper.models.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerManagerScreen(
    navController: NavController,
    viewModel: PlayerViewModel = viewModel()
) {
    var newPlayerName by remember { mutableStateOf("") }

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Manage Players") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            AddPlayerBottomBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.ime),
                playerName = newPlayerName,
                onPlayerNameChange = { newPlayerName = it },
                onAddPlayer = {
                    if (newPlayerName.isNotBlank()) {
                        viewModel.addPlayer(newPlayerName.trim())
                        newPlayerName = ""
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Players List
            val players by viewModel.players.collectAsState()
            
            if (players.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No players added yet",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add your first player below",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(players.toList().sorted()) { player ->
                        PlayerItem(
                            playerName = player,
                            onRemovePlayer = { viewModel.removePlayer(player) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerItem(
    playerName: String,
    onRemovePlayer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Could add player details or selection logic here */ },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = playerName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = onRemovePlayer,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove player",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddPlayerBottomBar(
    modifier: Modifier = Modifier,
    playerName: String,
    onPlayerNameChange: (String) -> Unit,
    onAddPlayer: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = playerName,
                onValueChange = onPlayerNameChange,
                label = { Text("Player Name") },
                placeholder = { Text("Enter player name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            Button(
                onClick = onAddPlayer,
                enabled = playerName.isNotBlank(),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add player",
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text("Add")
            }
        }
    }
}
