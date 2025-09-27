package com.pgs.gamehelper.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InputScreen(onGenerate: (List<String>, Int, Int, Int) -> Unit) {
    var playerCount by remember { mutableStateOf(6) }
    var courtCount by remember { mutableStateOf(1) }
    var courtHours by remember { mutableStateOf(2) }
    var gameDuration by remember { mutableStateOf(10) }
    var playerNames by remember { mutableStateOf(List(playerCount) { "Player ${it + 1}" }) }


    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Badminton Scheduler", style = MaterialTheme.typography.headlineMedium)


        Spacer(Modifier.height(16.dp))


        OutlinedTextField(
            value = playerCount.toString(),
            onValueChange = {
                val newCount = it.toIntOrNull() ?: playerCount
                playerCount = newCount
                playerNames = List(playerCount) { i ->
                    playerNames.getOrNull(i) ?: "Player ${i + 1}" }
            },
            label = { Text("Number of Players") },
            modifier = Modifier.fillMaxWidth()
        )


        OutlinedTextField(
            value = courtCount.toString(),
            onValueChange = { courtCount = it.toIntOrNull() ?: courtCount },
            label = { Text("Number of Courts") },
            modifier = Modifier.fillMaxWidth()
        )


        OutlinedTextField(
            value = courtHours.toString(),
            onValueChange = { courtHours = it.toIntOrNull() ?: courtHours },
            label = { Text("Court Hours") },
            modifier = Modifier.fillMaxWidth()
        )


        OutlinedTextField(
            value = gameDuration.toString(),
            onValueChange = { gameDuration = it.toIntOrNull() ?: gameDuration },
            label = { Text("Game Duration (minutes)") },
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(Modifier.height(16.dp))


        Text("Enter Player Names:")
        playerNames.forEachIndexed { i, name ->
            OutlinedTextField(
                value = name,
                onValueChange = { newName ->
                    playerNames = playerNames.toMutableList().also { list -> list[i] = newName }
                },
                label = { Text("Player ${i + 1}") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }


        Spacer(Modifier.height(16.dp))


        Button(onClick = {
            onGenerate(playerNames, courtHours, gameDuration, courtCount)
        }) {
            Text("Generate Schedule")
        }
    }
}