package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MatchEntity
import com.example.data.MatchRepository
import com.example.data.PlayerProfileEntity
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class LudoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MatchRepository
    private val profileDao = AppDatabase.getDatabase(application).playerProfileDao()

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
            val updated = PlayerProfileEntity(id = "default_user", playerName = name, avatarEmoji = avatarEmoji)
            profileDao.saveProfile(updated)
            _playerProfile.value = updated
        }
    }

    fun startNewGame(mode: GameMode, playerCount: Int, aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM) {
        matchStartTime = System.currentTimeMillis()
        val colors = listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.BLUE).take(playerCount)
        val profile = _playerProfile.value

        val players = colors.mapIndexed { index, color ->
            val isAI = (mode == GameMode.VS_AI && index > 0)
            val name = if (index == 0) profile.playerName else (if (isAI) "Bot ${color.displayName}" else "Player ${index + 1}")
            Player(
                id = "player_$index",
                name = name,
                color = color,
                isAI = isAI,
                aiDifficulty = aiDifficulty
            )
        }
        _gameState.value = GameState(
            gameId = "game_${System.currentTimeMillis()}",
            mode = mode,
            players = players,
            currentTurnPlayerIndex = 0,
            diceValue = null,
            hasRolled = false,
            logMessage = "${players[0].name}'s turn. Roll the dice!",
            isGameOver = false,
            winner = null
        )
    }

    fun rollDice() {
        val state = _gameState.value
        if (state.isRolling || state.hasRolled || state.isGameOver) return

        val currentPlayer = state.players[state.currentTurnPlayerIndex]
        if (currentPlayer.isAI) return // AI rolls automatically

        _gameState.value = state.copy(isRolling = true)

        viewModelScope.launch {
            kotlinx.coroutines.delay(600) // rolling animation delay
            val roll = Random.nextInt(1, 7)
            val consecutiveSixes = if (roll == 6) state.consecutiveSixes + 1 else 0

            // If 3 consecutive sixes, forfeit turn
            if (consecutiveSixes == 3) {
                _gameState.value = state.copy(
                    isRolling = false,
                    diceValue = roll,
                    hasRolled = false,
                    consecutiveSixes = 0,
                    logMessage = "${currentPlayer.name} rolled three 6s in a row! Turn skipped."
                )
                kotlinx.coroutines.delay(1200)
                nextTurn()
                return@launch
            }

            // Check if player has any valid moves
            val validMoves = getValidMovesForPlayer(currentPlayer, roll)
            if (validMoves.isEmpty()) {
                _gameState.value = state.copy(
                    isRolling = false,
                    diceValue = roll,
                    hasRolled = true,
                    consecutiveSixes = consecutiveSixes,
                    logMessage = "${currentPlayer.name} rolled $roll. No valid moves!"
                )
                kotlinx.coroutines.delay(1200)
                nextTurn()
            } else {
                _gameState.value = state.copy(
                    isRolling = false,
                    diceValue = roll,
                    hasRolled = true,
                    consecutiveSixes = consecutiveSixes,
                    logMessage = "${currentPlayer.name} rolled $roll. Tap a token to move."
                )
                // If AI turn, pick move automatically
                if (currentPlayer.isAI) {
                    kotlinx.coroutines.delay(800)
                    makeAIMove(validMoves)
                }
            }
        }
    }

    private fun getValidMovesForPlayer(player: Player, roll: Int): List<Token> {
        return player.tokens.filter { token ->
            if (token.isFinished) false
            else if (token.position == -1) roll == 6 // Need 6 to get out of base
            else {
                // Check if move exceeds home center (505)
                val targetPos = getTargetPosition(player.color, token.position, roll)
                targetPos <= 505
            }
        }
    }

    private fun getTargetPosition(color: PlayerColor, currentPos: Int, roll: Int): Int {
        if (currentPos >= 500) {
            return currentPos + roll
        }
        // Main track movement (0 to 51)
        val startSquare = color.pathIndex
        // Distance from start
        val distFromStart = if (currentPos >= startSquare) currentPos - startSquare else (52 - startSquare + currentPos)
        val newDist = distFromStart + roll
        if (newDist >= 51) {
            // Entering home stretch (500 + excess steps)
            val excess = newDist - 51
            return 500 + excess
        } else {
            return (startSquare + newDist) % 52
        }
    }

    fun moveToken(token: Token) {
        val state = _gameState.value
        if (!state.hasRolled || state.diceValue == null || state.isGameOver) return

        val currentPlayer = state.players[state.currentTurnPlayerIndex]
        val roll = state.diceValue

        val newPosition = if (token.position == -1) {
            currentPlayer.color.pathIndex
        } else {
            getTargetPosition(currentPlayer.color, token.position, roll)
        }

        if (newPosition > 505) return // Invalid move

        val isFinished = (newPosition == 505)

        // Update tokens for current player
        val updatedTokens = currentPlayer.tokens.map { t ->
            if (t.id == token.id) t.copy(position = newPosition, isHome = isFinished, isFinished = isFinished)
            else t
        }

        // Check captures on main path
        var capturedAny = false
        val updatedPlayers = state.players.map { p ->
            if (p.id == currentPlayer.id) {
                p.copy(tokens = updatedTokens)
            } else {
                // Check if any opponent token is at newPosition (and not on safe square)
                val isSafeSquare = isSafePosition(newPosition)
                val opponentTokens = p.tokens.map { ot ->
                    if (!isSafeSquare && ot.position == newPosition && newPosition < 500) {
                        capturedAny = true
                        ot.copy(position = -1) // Send back to base
                    } else ot
                }
                p.copy(tokens = opponentTokens)
            }
        }

        // Check if player won
        val allFinished = updatedTokens.all { it.isFinished }
        var winner = state.winner
        if (allFinished && winner == null) {
            winner = currentPlayer
            saveMatchRecord(state.mode, winner.name, state.players.size)
        }

        val bonusTurn = (roll == 6 || capturedAny || isFinished)
        val msg = buildString {
            append("${currentPlayer.name} moved token.")
            if (capturedAny) append(" Captured opponent token!")
            if (bonusTurn) append(" Bonus turn awarded!")
        }

        _gameState.value = state.copy(
            players = updatedPlayers,
            hasRolled = false,
            diceValue = null,
            winner = winner,
            isGameOver = (winner != null),
            logMessage = msg
        )

        if (winner == null) {
            if (bonusTurn) {
                // Keep same player turn
                val nextPlayer = updatedPlayers[state.currentTurnPlayerIndex]
                if (nextPlayer.isAI && !state.isGameOver) {
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

    private fun isSafePosition(pos: Int): Boolean {
        if (pos >= 500) return true
        // Standard Ludo safe squares (star positions: 0, 8, 13, 21, 26, 34, 39, 47)
        val safeSquares = listOf(0, 8, 13, 21, 26, 34, 39, 47)
        return pos in safeSquares
    }

    private fun nextTurn() {
        val state = _gameState.value
        if (state.isGameOver) return

        var nextIndex = (state.currentTurnPlayerIndex + 1) % state.players.size
        // Skip finished players if any
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
            val currentPlayer = state.players[state.currentTurnPlayerIndex]
            val validMoves = getValidMovesForPlayer(currentPlayer, roll)

            if (validMoves.isEmpty()) {
                _gameState.value = state.copy(
                    isRolling = false,
                    diceValue = roll,
                    hasRolled = true,
                    logMessage = "${currentPlayer.name} rolled $roll. No valid moves."
                )
                kotlinx.coroutines.delay(1000)
                nextTurn()
            } else {
                _gameState.value = state.copy(
                    isRolling = false,
                    diceValue = roll,
                    hasRolled = true,
                    logMessage = "${currentPlayer.name} rolled $roll."
                )
                kotlinx.coroutines.delay(800)
                makeAIMove(validMoves)
            }
        }
    }

    private fun makeAIMove(validMoves: List<Token>) {
        // AI Strategy based on difficulty
        val currentPlayer = _gameState.value.players[_gameState.value.currentTurnPlayerIndex]
        val chosenToken = when (currentPlayer.aiDifficulty) {
            AIDifficulty.HARD -> {
                // Prefer capturing or finishing or moving out of base
                validMoves.maxByOrNull { it.position } ?: validMoves.first()
            }
            AIDifficulty.MEDIUM -> {
                validMoves.random()
            }
            AIDifficulty.EASY -> {
                validMoves.first()
            }
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
