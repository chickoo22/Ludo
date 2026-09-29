package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AIDifficulty
import com.example.model.GameMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    mode: GameMode,
    onStartGame: (GameMode, Int, AIDifficulty) -> Unit,
    onBack: () -> Unit
) {
    var playerCount by remember { mutableStateOf(4) }
    var aiDifficulty by remember { mutableStateOf(AIDifficulty.MEDIUM) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (mode == GameMode.VS_AI) "VS AI Match Setup" else "Pass & Play Setup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(text = "Select Number of Players", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(2, 3, 4).forEach { count ->
                    OutlinedButton(
                        onClick = { playerCount = count },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (playerCount == count) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text("$count Players")
                    }
                }
            }

            if (mode == GameMode.VS_AI) {
                Text(text = "AI Difficulty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AIDifficulty.values().forEach { diff ->
                        OutlinedButton(
                            onClick = { aiDifficulty = diff },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (aiDifficulty == diff) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(diff.name)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onStartGame(mode, playerCount, aiDifficulty) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Start Game", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
