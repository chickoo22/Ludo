package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MatchEntity
import com.example.data.MatchRepository
import com.example.data.PlayerProfileEntity
import com.example.model.*
import com.example.util.SoundManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class LudoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MatchRepository
    private val profileDao = AppDatabase.getDatabase(application).playerProfileDao()
    private val soundManager = SoundManager(application)

    val matchHistory: StateFlow<List<MatchEntity>>

    private val _playerProfile = MutableStateFlow(PlayerProfileEntity())
    val playerProfile: StateFlow<PlayerProfileEntity> = _playerProfile.asStateFlow()

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _onlineRooms = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val onlineRooms: StateFlow<List<Map<String, Any>>> = _onlineRooms.asStateFlow()

    private var matchStartTime: Long = System.currentTimeMillis()

    init {
        val matchDao = AppDatabase.getDatabase(application).matchDao()
        repository = MatchRepository(matchDao)
        matchHistory = repository.allMatches.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            profileDao.getProfile("default_user").collect { profile ->
                if (profile != null) {
                    _playerProfile.value = profile
                } else {
                    profileDao.saveProfile(PlayerProfileEntity())
                }
            }
        }
    }

    fun updateProfile(name: String, avatarEmoji: String) {
        viewModelScope.launch {
            val current = _playerProfile.value
            val updated = current.copy(playerName = name, avatarEmoji = avatarEmoji)
            profileDao.saveProfile(updated)
            _playerProfile.value = updated
        }
    }

    val isDailyBonusAvailable: Boolean
        get() {
            val profile = _playerProfile.value
            if (profile.lastDailyBonusTime == 0L) return true
            val oneDayMs = 24 * 60 * 60 * 1000L
            return (System.currentTimeMillis() - profile.lastDailyBonusTime) >= oneDayMs
        }

    fun claimDailyBonus(): Pair<Long, Int> {
        val profile = _playerProfile.value
        val currentTime = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val timeDiff = currentTime - profile.lastDailyBonusTime

        if (profile.lastDailyBonusTime > 0 && timeDiff < oneDayMs) {
            return Pair(0L, 0)
        }

        val nextStreak = if (profile.lastDailyBonusTime == 0L || timeDiff < 48 * 60 * 60 * 1000L) {
            if (profile.dailyBonusStreak >= 7) 1 else profile.dailyBonusStreak + 1
        } else {
            1
        }

        val (rewardCoins, rewardGems) = when (nextStreak) {
            1 -> Pair(100L, 5)
            2 -> Pair(250L, 10)
            3 -> Pair(500L, 15)
            4 -> Pair(1000L, 20)
            5 -> Pair(2000L, 30)
            6 -> Pair(3500L, 40)
            7 -> Pair(5000L, 50)
            else -> Pair(100L, 5)
        }

        val updated = profile.copy(
            coins = profile.coins + rewardCoins,
            gems = profile.gems + rewardGems,
            lastDailyBonusTime = currentTime,
            dailyBonusStreak = nextStreak
        )

        viewModelScope.launch {
            profileDao.saveProfile(updated)
            _playerProfile.value = updated
        }

        soundManager.playWinSound()
        return Pair(rewardCoins, rewardGems)
    }

    fun addReward(coins: Long, gems: Int) {
        val profile = _playerProfile.value
        val updated = profile.copy(
            coins = profile.coins + coins,
            gems = profile.gems + gems
        )
        viewModelScope.launch {
            profileDao.saveProfile(updated)
            _playerProfile.value = updated
        }
        soundManager.playWinSound()
    }

    fun startNewGame(mode: GameMode, playerCount: Int, variant: GameVariant = GameVariant.CLASSIC, aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM, boardTheme: BoardTheme = BoardTheme.CLASSIC_WOOD) {
        val colors = listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.BLUE).take(playerCount)
        val profile = _playerProfile.value
        val configs = colors.mapIndexed { index, color ->
            Triple(color, if (index == 0) profile.playerName else "Bot ${color.displayName}", mode == GameMode.VS_AI && index > 0)
        }
        startCustomGame(mode, variant, configs, aiDifficulty, boardTheme)
    }

    fun startCustomGame(mode: GameMode, variant: GameVariant, playerConfigs: List<Triple<PlayerColor, String, Boolean>>, aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM, boardTheme: BoardTheme = BoardTheme.CLASSIC_WOOD) {
        matchStartTime = System.currentTimeMillis()
        val profile = _playerProfile.value
        val players = playerConfigs.mapIndexed { index, config ->
            val name = if (index == 0 && !config.third) profile.playerName else config.second
            val emoji = if (index == 0 && !config.third) profile.avatarEmoji else listOf("🦁", "🐯", "🤖", "🦊", "🐼").random()
            Player(
                id = "player_$index",
                name = name,
                color = config.first,
                isAI = config.third,
                aiDifficulty = aiDifficulty,
                avatarEmoji = emoji
            )
        }
        _gameState.value = GameState(
            gameId = "game_${System.currentTimeMillis()}",
            mode = mode,
            variant = variant,
            players = players,
            currentTurnPlayerIndex = 0,
            diceValue = null,
            hasRolled = false,
            logMessage = "${players[0].name}'s turn. Tap Roll Dice!",
            isGameOver = false,
            winner = null,
            boardTheme = boardTheme
        )
    }

    fun setBoardTheme(theme: BoardTheme) {
        _gameState.value = _gameState.value.copy(boardTheme = theme)
    }

    fun sendReaction(emoji: String) {
        val state = _gameState.value
        val current = state.players.getOrNull(state.currentTurnPlayerIndex) ?: return
        val updatedPlayers = state.players.map { p ->
            if (p.id == current.id) p.copy(chatReaction = emoji) else p
        }
        _gameState.value = state.copy(players = updatedPlayers)
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            clearReaction(current.id)
        }
    }

    private fun clearReaction(playerId: String) {
        val state = _gameState.value
        val updatedPlayers = state.players.map { p ->
            if (p.id == playerId) p.copy(chatReaction = null) else p
        }
        _gameState.value = state.copy(players = updatedPlayers)
    }

    fun rollDice() {
        val state = _gameState.value
        if (state.isRolling || state.hasRolled || state.isGameOver) return

        val currentPlayer = state.players[state.currentTurnPlayerIndex]
        if (currentPlayer.isAI) return

        soundManager.playDiceRollSound()
        _gameState.value = state.copy(isRolling = true)

        viewModelScope.launch {
            kotlinx.coroutines.delay(700) // exciting dice roll animation delay
            val roll = Random.nextInt(1, 7)
            processDiceRoll(roll)
        }
    }

    private fun processDiceRoll(roll: Int) {
        val state = _gameState.value
        val currentPlayer = state.players[state.currentTurnPlayerIndex]
        val consecutiveSixes = if (roll == 6) state.consecutiveSixes + 1 else 0

        if (consecutiveSixes == 3) {
            _gameState.value = state.copy(
                isRolling = false,
                diceValue = roll,
                hasRolled = false,
                consecutiveSixes = 0,
                logMessage = "${currentPlayer.name} rolled three 6s in a row! Turn skipped."
            )
            viewModelScope.launch {
                kotlinx.coroutines.delay(1200)
                nextTurn()
            }
            return
        }

        val validMoves = getValidMovesForPlayer(currentPlayer, roll)
        if (validMoves.isEmpty()) {
            _gameState.value = state.copy(
                isRolling = false,
                diceValue = roll,
                hasRolled = true,
                consecutiveSixes = consecutiveSixes,
                logMessage = "${currentPlayer.name} rolled $roll. No valid moves!"
            )
            viewModelScope.launch {
                kotlinx.coroutines.delay(1200)
                nextTurn()
            }
        } else if (validMoves.size == 1) {
            _gameState.value = state.copy(
                isRolling = false,
                diceValue = roll,
                hasRolled = true,
                consecutiveSixes = consecutiveSixes,
                logMessage = "${currentPlayer.name} rolled $roll. Auto-moving single pawn!"
            )
            viewModelScope.launch {
                kotlinx.coroutines.delay(500)
                moveToken(validMoves.first())
            }
        } else {
            _gameState.value = state.copy(
                isRolling = false,
                diceValue = roll,
                hasRolled = true,
                consecutiveSixes = consecutiveSixes,
                logMessage = "${currentPlayer.name} rolled $roll. Tap a glowing token to move!"
            )
            if (currentPlayer.isAI) {
                viewModelScope.launch {
                    kotlinx.coroutines.delay(900)
                    makeAIMove(validMoves)
                }
            }
        }
    }

    fun getValidMovesForPlayer(player: Player, roll: Int): List<Token> {
        return player.tokens.filter { token ->
            if (token.isFinished) false
            else if (token.position == -1) roll == 6
            else {
                val targetPos = getTargetPosition(player.color, token.position, roll)
                targetPos <= 505
            }
        }
    }

    private fun getTargetPosition(color: PlayerColor, currentPos: Int, roll: Int): Int {
        if (currentPos >= 500) {
            return currentPos + roll
        }
        val startSquare = color.pathIndex
        val distFromStart = if (currentPos >= startSquare) currentPos - startSquare else (52 - startSquare + currentPos)
        val newDist = distFromStart + roll
        if (newDist >= 51) {
            val excess = newDist - 51
            return 500 + excess
        } else {
            return (startSquare + newDist) % 52
        }
    }

    fun moveToken(token: Token) {
        val state = _gameState.value
        if (!state.hasRolled || state.diceValue == null || state.isGameOver || state.isRolling) return

        val currentPlayer = state.players[state.currentTurnPlayerIndex]
        val roll = state.diceValue

        val startPos = token.position
        val targetPos = if (startPos == -1) {
            currentPlayer.color.pathIndex
        } else {
            getTargetPosition(currentPlayer.color, startPos, roll)
        }

        if (targetPos > 505) return

        viewModelScope.launch {
            _gameState.value = state.copy(hasRolled = false, diceValue = null, logMessage = "${currentPlayer.name} is moving...")

            // Generate intermediate step positions for smooth animation
            val positions = mutableListOf<Int>()
            if (startPos == -1) {
                positions.add(currentPlayer.color.pathIndex)
            } else {
                var current = startPos
                for (step in 1..roll) {
                    current = getTargetPosition(currentPlayer.color, current, 1)
                    positions.add(current)
                }
            }

            // Animate step by step along path / spawn
            var currentTokens = currentPlayer.tokens
            for (pos in positions) {
                val isFinished = (pos == 505)
                currentTokens = currentTokens.map { t ->
                    if (t.id == token.id) t.copy(position = pos, isHome = isFinished, isFinished = isFinished)
                    else t
                }
                val animPlayers = _gameState.value.players.map { p ->
                    if (p.id == currentPlayer.id) p.copy(tokens = currentTokens) else p
                }
                soundManager.playMoveSound()
                _gameState.value = _gameState.value.copy(players = animPlayers)
                kotlinx.coroutines.delay(130)
            }

            // Final state evaluation after animation completes
            val finalState = _gameState.value
            val finalPlayer = finalState.players[finalState.currentTurnPlayerIndex]
            val finalToken = finalPlayer.tokens.first { it.id == token.id }
            val finalPos = finalToken.position
            val isFinished = finalToken.isFinished

            // Check captures
            var capturedAny = false
            val updatedPlayers = finalState.players.map { p ->
                if (p.id == finalPlayer.id) {
                    p.copy(tokens = finalPlayer.tokens)
                } else {
                    val isSafeSquare = isSafePosition(finalPos)
                    val opponentTokens = p.tokens.map { ot ->
                        if (!isSafeSquare && ot.position == finalPos && finalPos < 500) {
                            capturedAny = true
                            ot.copy(position = -1)
                        } else ot
                    }
                    p.copy(tokens = opponentTokens)
                }
            }

            // Check win condition
            val hasWon = when (finalState.variant) {
                GameVariant.QUICK -> finalPlayer.tokens.any { it.isFinished }
                GameVariant.CLASSIC -> finalPlayer.tokens.all { it.isFinished }
            }

            var winner = finalState.winner
            if (hasWon && winner == null) {
                winner = finalPlayer
                saveMatchRecord(finalState.mode, winner.name, finalState.players.size)
            }

            if (capturedAny) {
                soundManager.playCaptureSound()
            }
            if (winner != null) {
                soundManager.playWinSound()
            }

            val bonusTurn = (roll == 6 || capturedAny || isFinished)
            val msg = buildString {
                append("${finalPlayer.name} moved.")
                if (capturedAny) append(" Captured piece! 🔥")
                if (bonusTurn && winner == null) append(" Bonus turn! 🎲")
            }

            val captureMsg = if (capturedAny) "Piece Captured! Returned to Base! 💥" else null

            _gameState.value = finalState.copy(
                players = updatedPlayers,
                winner = winner,
                isGameOver = (winner != null),
                logMessage = msg,
                captureEventMessage = captureMsg
            )

            if (captureMsg != null) {
                viewModelScope.launch {
                    kotlinx.coroutines.delay(2000)
                    val current = _gameState.value
                    if (current.captureEventMessage == captureMsg) {
                        _gameState.value = current.copy(captureEventMessage = null)
                    }
                }
            }

            if (winner == null) {
                if (bonusTurn) {
                    val nextPlayer = updatedPlayers[finalState.currentTurnPlayerIndex]
                    if (nextPlayer.isAI && !finalState.isGameOver) {
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(1000)
                            rollDiceForAI()
                        }
                    }
                } else {
                    nextTurn()
                }
            }
        }
    }

    private fun isSafePosition(pos: Int): Boolean {
        if (pos >= 500) return true
        val safeSquares = listOf(0, 8, 13, 21, 26, 34, 39, 47)
        return pos in safeSquares
    }

    private fun nextTurn() {
        val state = _gameState.value
        if (state.isGameOver) return

        val nextIndex = (state.currentTurnPlayerIndex + 1) % state.players.size
        val nextPlayer = state.players[nextIndex]

        _gameState.value = state.copy(
            currentTurnPlayerIndex = nextIndex,
            hasRolled = false,
            diceValue = null,
            consecutiveSixes = 0,
            logMessage = "${nextPlayer.name}'s turn. Roll the dice!"
        )

        if (nextPlayer.isAI && !state.isGameOver) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                rollDiceForAI()
            }
        }
    }

    private fun rollDiceForAI() {
        val state = _gameState.value
        if (state.isGameOver || !state.players[state.currentTurnPlayerIndex].isAI) return

        _gameState.value = state.copy(isRolling = true)
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            val roll = Random.nextInt(1, 7)
            processDiceRoll(roll)
        }
    }

    private fun makeAIMove(validMoves: List<Token>) {
        val currentPlayer = _gameState.value.players[_gameState.value.currentTurnPlayerIndex]
        val chosenToken = when (currentPlayer.aiDifficulty) {
            AIDifficulty.HARD -> validMoves.maxByOrNull { it.position } ?: validMoves.first()
            AIDifficulty.MEDIUM -> validMoves.random()
            AIDifficulty.EASY -> validMoves.first()
        }
        moveToken(chosenToken)
    }

    private fun saveMatchRecord(mode: GameMode, winnerName: String, playersCount: Int) {
        val duration = (System.currentTimeMillis() - matchStartTime) / 1000
        viewModelScope.launch {
            repository.insert(
                MatchEntity(
                    mode = mode.name,
                    winnerName = winnerName,
                    playersCount = playersCount,
                    durationSeconds = duration
                )
            )
        }
    }

    fun clearStats() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
