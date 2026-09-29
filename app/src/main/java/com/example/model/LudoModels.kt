package com.example.model

import androidx.compose.ui.graphics.Color

enum class PlayerColor(val displayName: String, val baseColor: Color, val lightColor: Color, val pathIndex: Int) {
    RED("Red", Color(0xFFE53935), Color(0xFFFFCDD2), 0),
    GREEN("Green", Color(0xFF43A047), Color(0xFFC8E6C9), 13),
    YELLOW("Yellow", Color(0xFFFDD835), Color(0xFFFFF9C4), 26),
    BLUE("Blue", Color(0xFF1E88E5), Color(0xFFBBDEFB), 39)
}

enum class GameMode {
    PASS_AND_PLAY,
    VS_AI,
    ONLINE_MULTIPARTY
}

enum class AIDifficulty {
    EASY, MEDIUM, HARD
}

data class Token(
    val id: Int, // 0 to 3 for each player
    val playerColor: PlayerColor,
    val position: Int = -1, // -1 = base, 0-51 = main path, 500-505 = home path, 999 = finished
    val isHome: Boolean = false,
    val isFinished: Boolean = false
)

data class Player(
    val id: String,
    val name: String,
    val color: PlayerColor,
    val isAI: Boolean = false,
    val aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM,
    val tokens: List<Token> = listOf(0, 1, 2, 3).map { Token(it, color) },
    val rank: Int? = null,
    val isConnected: Boolean = true
)

data class GameState(
    val gameId: String = "local_game",
    val mode: GameMode = GameMode.PASS_AND_PLAY,
    val players: List<Player> = emptyList(),
    val currentTurnPlayerIndex: Int = 0,
    val diceValue: Int? = null,
    val isRolling: Boolean = false,
    val hasRolled: Boolean = false,
    val consecutiveSixes: Int = 0,
    val winner: Player? = null,
    val logMessage: String = "Game started! Red's turn to roll.",
    val isGameOver: Boolean = false
)
