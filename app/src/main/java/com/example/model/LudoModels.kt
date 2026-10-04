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

enum class GameVariant {
    CLASSIC, // Requires all 4 tokens home
    QUICK    // Only 1 token needs to reach home
}

enum class AIDifficulty {
    EASY, MEDIUM, HARD
}

enum class BoardTheme(val displayName: String) {
    CLASSIC_WOOD("Classic Wood"),
    NEON_NIGHT("Neon Night"),
    MINIMALIST("Minimalist")
}

data class Token(
    val id: Int, // 0 to 3
    val playerColor: PlayerColor,
    val position: Int = -1, // -1 = base, 0-51 = main path, 500-505 = home path, 505 = finished
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
    val avatarEmoji: String = "🦁",
    val chatReaction: String? = null
)

data class GameState(
    val gameId: String = "local_game",
    val mode: GameMode = GameMode.PASS_AND_PLAY,
    val variant: GameVariant = GameVariant.CLASSIC,
    val players: List<Player> = emptyList(),
    val currentTurnPlayerIndex: Int = 0,
    val diceValue: Int? = null,
    val isRolling: Boolean = false,
    val hasRolled: Boolean = false,
    val consecutiveSixes: Int = 0,
    val winner: Player? = null,
    val logMessage: String = "Game started! Roll the dice!",
    val isGameOver: Boolean = false,
    val captureEventMessage: String? = null,
    val boardTheme: BoardTheme = BoardTheme.CLASSIC_WOOD
)
