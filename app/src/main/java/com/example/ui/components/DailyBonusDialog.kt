package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun DailyBonusDialog(
    streak: Int,
    isAvailable: Boolean,
    onClaim: () -> Unit,
    onDismiss: () -> Unit
) {
    var claimedMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Daily Bonus",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DAILY REWARDS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Login daily to claim amazing prizes and grow your wealth!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 7 Days Grid
                val rewards = listOf(
                    Triple(1, "100 🪙", "5 💎"),
                    Triple(2, "250 🪙", "10 💎"),
                    Triple(3, "500 🪙", "15 💎"),
                    Triple(4, "1,000 🪙", "20 💎"),
                    Triple(5, "2,000 🪙", "30 💎"),
                    Triple(6, "3,500 🪙", "40 💎"),
                    Triple(7, "5,000 🪙", "50 💎")
                )

                val activeDay = if (streak in 1..7) streak else 1

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rewards.size) { index ->
                        val dayNum = rewards[index].first
                        val coinsText = rewards[index].second
                        val gemsText = rewards[index].third
                        val isCurrent = dayNum == activeDay
                        val isPast = dayNum < activeDay

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isCurrent && isAvailable -> Color(0xFF2E7D32)
                                    isPast -> Color(0xFF2A2A3E)
                                    else -> Color(0xFF252538)
                                }
                            ),
                            border = if (isCurrent) BorderStroke(2.dp, Color(0xFFFFD700)) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Day $dayNum",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) Color(0xFFFFD700) else Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = coinsText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = gemsText, fontSize = 10.sp, color = Color(0xFF00E5FF))
                                if (isPast) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("CLAIMED", fontSize = 9.sp, color = Color.Green, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (claimedMessage != null) {
                    Text(
                        text = claimedMessage!!,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        if (isAvailable) {
                            onClaim()
                            claimedMessage = "🎉 Bonus Claimed Successfully!"
                        }
                    },
                    enabled = isAvailable,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    Text(
                        text = if (isAvailable) "CLAIM DAILY REWARD" else "ALREADY CLAIMED TODAY",
                        color = if (isAvailable) Color.Black else Color.DarkGray,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
