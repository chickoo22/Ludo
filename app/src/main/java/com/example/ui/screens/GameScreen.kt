package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.GameState
import com.example.model.Token
import com.example.ui.components.LudoBoardView
import com.example.viewmodel.LudoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: LudoViewModel,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ludo Match (${gameState.mode.name})") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Player info horizontal scroll / cards
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(gameState.players) { index, player ->
                    val isCurrent = (index == gameState.currentTurnPlayerIndex)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) player.color.lightColor else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 6.dp else 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = player.name, fontWeight = FontWeight.Bold, color = player.color.baseColor)
                            Text(text = "Tokens home: ${player.tokens.count { it.isFinished }}/4", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Game Board View
            LudoBoardView(
                players = gameState.players,
                currentTurnPlayerIndex = gameState.currentTurnPlayerIndex,
                onTokenClick = { token ->
                    viewModel.moveToken(token)
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Game Log & Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = gameState.logMessage,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dice Roll Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentPlayer = gameState.players.getOrNull(gameState.currentTurnPlayerIndex)

                Column {
                    Text(text = "Turn: ${currentPlayer?.name ?: ""}", fontWeight = FontWeight.Bold)
                    Text(text = "Dice: ${gameState.diceValue ?: "-"}", style = MaterialTheme.typography.titleLarge)
                }

                Button(
                    onClick = { viewModel.rollDice() },
                    enabled = !gameState.hasRolled && !gameState.isRolling && !(currentPlayer?.isAI ?: false),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Casino, contentDescription = "Roll")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (gameState.isRolling) "Rolling..." else "Roll Dice")
                }
            }
        }

        // Win Dialog
        if (gameState.isGameOver && gameState.winner != null) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("🎉 Victory!") },
                text = { Text("${gameState.winner?.name} has won the Ludo match!") },
                confirmButton = {
                    Button(onClick = onBack) {
                        Text("Back to Home")
                    }
                }
            )
        }
    }
}
