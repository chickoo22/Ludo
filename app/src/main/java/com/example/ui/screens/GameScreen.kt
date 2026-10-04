package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoardTheme
import com.example.model.GameState
import com.example.model.Token
import com.example.ui.components.LudoBoardView
import com.example.ui.components.LudoDiceComponent
import com.example.ui.components.ConfettiOverlayView
import com.example.viewmodel.LudoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: LudoViewModel,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val validMoves = remember(gameState) {
        val currentPlayer = gameState.players.getOrNull(gameState.currentTurnPlayerIndex)
        if (currentPlayer != null && gameState.diceValue != null && gameState.hasRolled) {
            viewModel.getValidMovesForPlayer(currentPlayer, gameState.diceValue!!)
        } else {
            emptyList()
        }
    }

    var showReactionPicker by remember { mutableStateOf(false) }
    var showThemeSelector by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF102027), Color(0xFF263238), Color(0xFF37474F))
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Ludo Battle", fontWeight = FontWeight.Black, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = gameState.variant.name,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showThemeSelector = true }) {
                            Icon(Icons.Default.Palette, contentDescription = "Change Theme", tint = Color(0xFFFFD700))
                        }
                        IconButton(onClick = { showReactionPicker = !showReactionPicker }) {
                            Icon(Icons.Default.EmojiEmotions, contentDescription = "Reactions", tint = Color(0xFFFFD700))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Player HUD cards
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(gameState.players) { index, player ->
                        val isCurrent = (index == gameState.currentTurnPlayerIndex)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) player.color.baseColor else Color.Black.copy(alpha = 0.5f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 10.dp else 2.dp),
                            modifier = if (isCurrent) Modifier.border(3.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp)) else Modifier
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isCurrent) Color(0xFFFFD700) else Color.White)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = player.avatarEmoji, fontSize = 22.sp)
                                }
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = player.name,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (isCurrent) {
                                            Text("👑", fontSize = 12.sp)
                                        }
                                    }
                                    Text(
                                        text = "Home: ${player.tokens.count { it.isFinished }}/4",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                                if (player.chatReaction != null) {
                                    Text(text = player.chatReaction!!, fontSize = 24.sp)
                                }
                            }
                        }
                    }
                }

                // Quick Reactions Popup
                if (showReactionPicker) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("🔥", "😂", "👑", "👍", "😎", "🎉", "😱").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 28.sp,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.sendReaction(emoji)
                                        showReactionPicker = false
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Arcade Ludo Board View with glowing valid moves
                    LudoBoardView(
                        players = gameState.players,
                        currentTurnPlayerIndex = gameState.currentTurnPlayerIndex,
                        validMoves = validMoves,
                        boardTheme = gameState.boardTheme,
                        onTokenClick = { token ->
                            viewModel.moveToken(token)
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Capture / Opponent sent to base animation banner
                    if (gameState.captureEventMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFFD32F2F).copy(alpha = 0.95f),
                            shadowElevation = 12.dp
                        ) {
                            Text(
                                text = gameState.captureEventMessage!!,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Game Log & Status Message Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = gameState.logMessage,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dice Roll Control Bar (Arcade style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentPlayer = gameState.players.getOrNull(gameState.currentTurnPlayerIndex)

                    Column {
                        Text(
                            text = "Turn: ${currentPlayer?.name ?: ""}",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (gameState.diceValue != null) "Rolled: ${gameState.diceValue}" else "Tap Dice to Roll!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFFD700)
                        )
                    }

                    LudoDiceComponent(
                        diceValue = gameState.diceValue,
                        isRolling = gameState.isRolling,
                        onRollRequested = {
                            if (!gameState.hasRolled && !gameState.isRolling && !(currentPlayer?.isAI ?: false)) {
                                viewModel.rollDice()
                            }
                        }
                    )
                }
            }
        }

        // Victory Celebration Dialog and Confetti
        if (gameState.isGameOver && gameState.winner != null) {
            ConfettiOverlayView()
            AlertDialog(
                onDismissRequest = {},
                containerColor = Color(0xFF1E272C),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("👑", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("VICTORY!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color(0xFFFFD700))
                    }
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${gameState.winner?.name} has won the Ludo Championship!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF37474F)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = Color(0xFFFFD700))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("+500 Coins Won!", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to Lobby", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            )
        }

        // Theme Selector Dialog
        if (showThemeSelector) {
            AlertDialog(
                onDismissRequest = { showThemeSelector = false },
                containerColor = Color(0xFF1E272C),
                title = {
                    Text("Select Board Theme", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BoardTheme.values().forEach { theme ->
                            Button(
                                onClick = {
                                    viewModel.setBoardTheme(theme)
                                    showThemeSelector = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (gameState.boardTheme == theme) Color(0xFFFFD700) else Color(0xFF37474F),
                                    contentColor = if (gameState.boardTheme == theme) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(theme.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeSelector = false }) {
                        Text("Close", color = Color.White)
                    }
                }
            )
        }
    }
}
