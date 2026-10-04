package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.viewmodel.LudoViewModel
import com.example.ui.components.DailyBonusDialog
import com.example.ui.components.RateUsDialog
import com.example.ui.components.BannerAdView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LudoViewModel,
    onStartGame: (GameMode, Int) -> Unit,
    onNavigateOnline: () -> Unit,
    onNavigateStats: () -> Unit,
    onNavigateRules: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val playerProfile by viewModel.playerProfile.collectAsState()
    var showDailyBonusDialog by remember { mutableStateOf(false) }
    var showRateUsDialog by remember { mutableStateOf(false) }
    val isBonusAvailable = viewModel.isDailyBonusAvailable
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color(0xFF0D47A1),
                drawerContentColor = Color.White
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                // Drawer Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(playerProfile.avatarEmoji.ifEmpty { "👑" }, fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = playerProfile.playerName.ifEmpty { "Ludo Player" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Coins: %,d | Gems: %d".format(playerProfile.coins, playerProfile.gems),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFEB3B)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White) },
                    label = { Text("Profile & Avatar", color = Color.White, fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateProfile()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.HelpOutline, contentDescription = "Rules", tint = Color.White) },
                    label = { Text("Game Rules & Tutorial", color = Color.White, fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateRules()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Rate Us", tint = Color(0xFFFFD700)) },
                    label = { Text("Rate ⭐ on Play Store", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showRateUsDialog = true
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Leaderboard", tint = Color.White) },
                    label = { Text("Leaderboard & Stats", color = Color.White, fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateStats()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF42A5F5))
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Arcade Header (Menu Button, Daily Bonus, Coins & Gems)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu Drawer", tint = Color.White, modifier = Modifier.size(28.dp))
                        }

                        // Daily Bonus Button with Badge
                        Box {
                            Button(
                                onClick = { showDailyBonusDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = "Daily Bonus", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bonus", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            if (isBonusAvailable) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                        .border(2.dp, Color.White, CircleShape)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Coins Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.4f)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("%,d".format(playerProfile.coins), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        // Gems Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.4f)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Diamond, contentDescription = "Gems", tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${playerProfile.gems}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Center Title & Logo Banner
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFFFFD700), Color(0xFFF57C00))))
                            .border(4.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Ludo Logo",
                            modifier = Modifier.size(60.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "LUDO KINGDOM",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "⭐ Ultimate Multiplayer Board Game ⭐",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFEB3B),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Game Mode Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ArcadeMenuButton(
                        title = "PASS & PLAY (2-4 PLAYERS)",
                        subtitle = "Play with friends on same device",
                        color = Color(0xFF2E7D32),
                        icon = Icons.Default.People,
                        onClick = { onStartGame(GameMode.PASS_AND_PLAY, 4) }
                    )

                    ArcadeMenuButton(
                        title = "VS COMPUTER / AI",
                        subtitle = "Challenge smart bot opponents",
                        color = Color(0xFFC62828),
                        icon = Icons.Default.SmartToy,
                        onClick = { onStartGame(GameMode.VS_AI, 2) }
                    )

                    ArcadeMenuButton(
                        title = "ONLINE MULTIPLAYER",
                        subtitle = "Global matchmaking & private rooms",
                        color = Color(0xFF1565C0),
                        icon = Icons.Default.Public,
                        onClick = onNavigateOnline
                    )
                }

                // Bottom Banner Ad & Leaderboard Action
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = onNavigateStats) {
                            Icon(Icons.Default.BarChart, contentDescription = "Stats", tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Leaderboard", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    BannerAdView()
                }
            }

            if (showDailyBonusDialog) {
                DailyBonusDialog(
                    streak = playerProfile.dailyBonusStreak,
                    isAvailable = isBonusAvailable,
                    onClaim = {
                        viewModel.claimDailyBonus()
                    },
                    onDismiss = { showDailyBonusDialog = false }
                )
            }

            if (showRateUsDialog) {
                RateUsDialog(
                    onRewardEarned = { coins, gems ->
                        viewModel.addReward(coins, gems)
                    },
                    onDismiss = { showRateUsDialog = false }
                )
            }
        }
    }
}

@Composable
fun ArcadeMenuButton(
    title: String,
    subtitle: String,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color.White
            )
        }
    }
}
