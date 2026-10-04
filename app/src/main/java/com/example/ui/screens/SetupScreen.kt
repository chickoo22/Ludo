package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AIDifficulty
import com.example.model.BoardTheme
import com.example.model.GameMode
import com.example.model.GameVariant
import com.example.model.PlayerColor
import com.example.ui.components.BannerAdView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    mode: GameMode,
    onStartGame: (GameMode, GameVariant, List<Triple<PlayerColor, String, Boolean>>, AIDifficulty, BoardTheme) -> Unit,
    onBack: () -> Unit
) {
    var playerCount by remember { mutableStateOf(2) }
    var gameVariant by remember { mutableStateOf(GameVariant.CLASSIC) }
    var aiDifficulty by remember { mutableStateOf(AIDifficulty.MEDIUM) }
    var boardTheme by remember { mutableStateOf(BoardTheme.CLASSIC_WOOD) }

    val defaultColors = listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.BLUE)
    
    var playerConfigs by remember(playerCount, mode) {
        mutableStateOf(
            List(playerCount) { index ->
                Triple(
                    defaultColors[index],
                    if (index == 0) "Player 1" else if (mode == GameMode.VS_AI) "Bot ${defaultColors[index].displayName}" else "Player ${index + 1}",
                    mode == GameMode.VS_AI && index > 0
                )
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF42A5F5))
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(if (mode == GameMode.VS_AI) "VS AI Setup" else "Ludo Match Setup", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                BannerAdView()
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Game Variant (Classic vs Quick)
                item {
                    Text(text = "Select Game Variant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GameVariant.values().forEach { variant ->
                            OutlinedButton(
                                onClick = { gameVariant = variant },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (gameVariant == variant) Color(0xFFFFD700) else Color.Black.copy(alpha = 0.3f),
                                    contentColor = if (gameVariant == variant) Color.Black else Color.White
                                )
                            ) {
                                Text(if (variant == GameVariant.CLASSIC) "Classic (All Home)" else "Quick (1 Token Out)", fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }

                // Board Theme Engine
                item {
                    Text(text = "Select Board Theme Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BoardTheme.values().forEach { theme ->
                            OutlinedButton(
                                onClick = { boardTheme = theme },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (boardTheme == theme) Color(0xFFFFD700) else Color.Black.copy(alpha = 0.3f),
                                    contentColor = if (boardTheme == theme) Color.Black else Color.White
                                )
                            ) {
                                Text(theme.displayName, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }

                item {
                    Text(text = "Select Number of Players", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 3, 4).forEach { count ->
                            OutlinedButton(
                                onClick = {
                                    playerCount = count
                                    playerConfigs = List(count) { index ->
                                        Triple(
                                            defaultColors[index],
                                            if (index == 0) "Player 1" else if (mode == GameMode.VS_AI) "Bot ${defaultColors[index].displayName}" else "Player ${index + 1}",
                                            mode == GameMode.VS_AI && index > 0
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (playerCount == count) Color(0xFFFFD700) else Color.Black.copy(alpha = 0.3f),
                                    contentColor = if (playerCount == count) Color.Black else Color.White
                                )
                            ) {
                                Text("$count Players", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Text(text = "Player Colors & Names", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }

                itemsIndexed(playerConfigs) { index, config ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Player ${index + 1}", fontWeight = FontWeight.Bold, color = config.first.lightColor)
                                
                                if (mode == GameMode.VS_AI && index > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.SmartToy, contentDescription = "AI", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("AI Bot", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                defaultColors.forEach { col ->
                                    val isSelected = (config.first == col)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(col.baseColor)
                                            .border(
                                                width = if (isSelected) 3.dp else 0.dp,
                                                color = if (isSelected) Color.White else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                playerConfigs = playerConfigs.toMutableList().also { list ->
                                                    list[index] = list[index].copy(first = col)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color.White))
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = config.second,
                                onValueChange = { newName ->
                                    playerConfigs = playerConfigs.toMutableList().also { list ->
                                        list[index] = list[index].copy(second = newName)
                                    }
                                },
                                label = { Text("Name", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD700),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                }

                if (mode == GameMode.VS_AI) {
                    item {
                        Text(text = "AI Difficulty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AIDifficulty.values().forEach { diff ->
                                OutlinedButton(
                                    onClick = { aiDifficulty = diff },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (aiDifficulty == diff) Color(0xFFFFD700) else Color.Black.copy(alpha = 0.3f),
                                        contentColor = if (aiDifficulty == diff) Color.Black else Color.White
                                    )
                                ) {
                                    Text(diff.name, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onStartGame(mode, gameVariant, playerConfigs, aiDifficulty, boardTheme) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text("START BATTLE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
