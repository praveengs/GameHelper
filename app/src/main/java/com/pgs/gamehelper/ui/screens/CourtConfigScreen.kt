package com.pgs.gamehelper.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtConfigScreen(navController: NavController) {
    var courts by remember { mutableStateOf("1") }
    var hours by remember { mutableStateOf("2") }
    var gameDuration by remember { mutableStateOf("10") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Courts") })
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = courts,
                onValueChange = { courts = it },
                label = { Text("Number of Courts") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = hours,
                onValueChange = { hours = it },
                label = { Text("Hours per Day") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(onClick = { // Save the config in navController's backStackEntry
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set(
                        "courts", courts.toIntOrNull() ?: 1
                    )
                    set("hours", hours.toIntOrNull() ?: 2)
                    set("gameDuration", gameDuration.toIntOrNull() ?: 10)
                }
                navController.navigate(NavRoutes.PlayerSelection.route)
            }, modifier = Modifier.fillMaxWidth()) { Text("Next") }
        }
    }
}