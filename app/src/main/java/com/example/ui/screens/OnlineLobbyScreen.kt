package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.GameMode
import com.example.model.AIDifficulty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineLobbyScreen(
    onStartOnlineGame: (GameMode, Int) -> Unit,
    onBack: () -> Unit
) {
    var roomCodeInput by remember { mutableStateOf("") }
    var isMatching by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Online Global Matchmaking") },
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = "Global",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Global Multiplayer Rooms",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Join global matchmaking queue or create a private room to play with friends anywhere.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = {
                    isMatching = true
                    // Simulate matchmaking queue connection
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isMatching
            ) {
                Text(if (isMatching) "Searching for Players..." else "Quick Match (4 Players)")
            }

            if (isMatching) {
                CircularProgressIndicator()
                Text("Connecting to global servers...")
                Button(onClick = { isMatching = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Cancel Search")
                }
            }

            Divider()

            OutlinedTextField(
                value = roomCodeInput,
                onValueChange = { roomCodeInput = it },
                label = { Text("Enter Room Code") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (roomCodeInput.isNotBlank()) {
                        onStartOnlineGame(GameMode.ONLINE_MULTIPARTY, 4)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = roomCodeInput.isNotBlank()
            ) {
                Text("Join Room by Code")
            }

            Button(
                onClick = {
                    onStartOnlineGame(GameMode.ONLINE_MULTIPARTY, 4)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Create Private Room")
            }
        }
    }
}
