package com.pgs.gamehelper.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pgs.gamehelper.models.GameBlock

@Composable
fun ScheduleScreen(schedule: List<GameBlock>, playerNames: List<String>, navController: NavController) {
    val playerColors = remember(playerNames) { generatePlayerColors(playerNames) }


    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Schedule Grid", fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
        }


        schedule.forEachIndexed { index, block ->
            var completed by remember { mutableStateOf(block.completed) }
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Game ${block.gameNumber} - Start: ${block.startTime}", style = MaterialTheme.typography.titleMedium)
                    Checkbox(
                        checked = completed,
                        onCheckedChange = { isChecked ->
                            completed = isChecked
                            schedule[index].completed = isChecked
                        }
                    )
                }
                Row(Modifier.fillMaxWidth()) {
                    block.courts.forEach { cg ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                                .border(1.dp, Color.Gray)
                                .padding(8.dp)
                        ) {
                            Text("Court ${cg.courtNumber}", style = MaterialTheme.typography.titleSmall)
                            PlayerTag(cg.teamA.first, playerColors)
                            PlayerTag(cg.teamA.second, playerColors)
                            Text("vs")
                            PlayerTag(cg.teamB.first, playerColors)
                            PlayerTag(cg.teamB.second, playerColors)
                            if (cg.sittingOut.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text("Sitting:", fontSize = 12.sp, color = Color.DarkGray)
                                cg.sittingOut.forEach { p -> PlayerTag(p, playerColors, small = true) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerTag(name: String, playerColors: Map<String, Color>, small: Boolean = false) {
    val color = playerColors[name] ?: Color.LightGray
    Text(
        text = name,
        fontSize = if (small) 12.sp else 14.sp,
        color = Color.Black,
        modifier = Modifier
            .padding(2.dp)
            .background(color.copy(alpha = 0.6f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

// Generate distinct colors for players
fun generatePlayerColors(players: List<String>): Map<String, Color> {
    val colors = listOf(
        Color(0xFFE57373), // Red
        Color(0xFF64B5F6), // Blue
        Color(0xFF81C784), // Green
        Color(0xFFFFB74D), // Orange
        Color(0xFFBA68C8), // Purple
        Color(0xFF4DB6AC), // Teal
        Color(0xFFA1887F), // Brown
        Color(0xFFFF8A65), // Deep Orange
        Color(0xFF90A4AE), // Gray Blue
        Color(0xFFDCE775) // Lime
    )
    return players.mapIndexed { index, player ->
        player to colors[index % colors.size]
    }.toMap()
}