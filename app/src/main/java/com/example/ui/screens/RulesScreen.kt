package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ludo Rules & Guide") },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RuleSection(title = "1. Starting the Game", content = "Each player has 4 tokens in their base yard. Players take turns rolling a 6-sided die.")
            RuleSection(title = "2. Opening a Token", content = "You need to roll a '6' to move a token out of your base and onto your starting square on the main track.")
            RuleSection(title = "3. Capturing Opponents", content = "If your token lands on the exact square occupied by an opponent's token (and it is not a safe star square), the opponent's token is captured and sent back to its base.")
            RuleSection(title = "4. Safe Squares", content = "Squares marked with stars are safe zones. Tokens on safe squares cannot be captured.")
            RuleSection(title = "5. Winning the Match", content = "Navigate all 4 of your tokens around the board and into the home center. The first player to get all 4 tokens home wins!")
        }
    }
}

@Composable
fun RuleSection(title: String, content: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
