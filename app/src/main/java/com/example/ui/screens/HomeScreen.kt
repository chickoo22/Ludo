package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode

@Composable
fun HomeScreen(
    onStartGame: (GameMode, Int) -> Unit,
    onNavigateOnline: () -> Unit,
    onNavigateStats: () -> Unit,
    onNavigateRules: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A237E), Color(0xFF3949AB), Color(0xFF5C6BC0))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Casino,
                contentDescription = "Ludo Icon",
                modifier = Modifier.size(72.dp),
                tint = Color(0xFFFDD835)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "LUDO MASTER",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Text(
                text = "Play Real-Time Multiplayer & Classic Board",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE8EAF6)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Customization Menu Button
            MenuButton(
                title = "Profile & Avatar Customizer",
                subtitle = "Choose your avatar emoji and display name",
                icon = Icons.Default.Person,
                onClick = onNavigateProfile
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pass & Play (Local 2-4 players)
            MenuButton(
                title = "Pass & Play (Local)",
                subtitle = "Play with friends on same device (2-4 players)",
                icon = Icons.Default.People,
                onClick = { onStartGame(GameMode.PASS_AND_PLAY, 4) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // VS AI Bots
            MenuButton(
                title = "VS Computer / AI",
                subtitle = "Challenge smart bot opponents",
                icon = Icons.Default.SmartToy,
                onClick = { onStartGame(GameMode.VS_AI, 2) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Online Multiplayer & Global Matchmaking
            MenuButton(
                title = "Online Multiplayer",
                subtitle = "Global matchmaking & private rooms",
                icon = Icons.Default.Public,
                onClick = onNavigateOnline
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onNavigateStats,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.BarChart, contentDescription = "Stats")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Leaderboard / Stats")
                }

                OutlinedButton(
                    onClick = onNavigateRules,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = "Rules")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rules")
                }
            }
        }
    }
}

@Composable
fun MenuButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = Color(0xFFFDD835))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFFC5CAE9))
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Go", tint = Color.White)
        }
    }
}
fun Arrangement.spacedSpaceIfSpaceOrEven() = Arrangement.SpaceEvenly
