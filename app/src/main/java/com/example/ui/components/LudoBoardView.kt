package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.model.BoardTheme
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.Token

@Composable
fun LudoBoardView(
    players: List<Player>,
    currentTurnPlayerIndex: Int,
    validMoves: List<Token>,
    boardTheme: BoardTheme = BoardTheme.CLASSIC_WOOD,
    onTokenClick: (Token) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val activeGlow by infiniteTransition.animateFloat(
        initialValue = 3f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "active_glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(6.dp)
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(players, currentTurnPlayerIndex, validMoves) {
                    detectTapGestures { offset ->
                        val clickedToken = findClickedToken(offset, size.width.toFloat(), players, currentTurnPlayerIndex, validMoves)
                        if (clickedToken != null) {
                            onTokenClick(clickedToken)
                        }
                    }
                }
        ) {
            drawArcadeLudoBoard(players, currentTurnPlayerIndex, validMoves, boardTheme, pulseScale, activeGlow)
        }
    }
}

private fun DrawScope.drawArcadeLudoBoard(
    players: List<Player>,
    currentTurnIndex: Int,
    validMoves: List<Token>,
    boardTheme: BoardTheme,
    pulseScale: Float,
    activeGlow: Float
) {
    val boardSize = size.width
    val cellSize = boardSize / 15f

    val bgColor = when (boardTheme) {
        BoardTheme.CLASSIC_WOOD -> Color(0xFF3E2723)
        BoardTheme.NEON_NIGHT -> Color(0xFF0F172A)
        BoardTheme.MINIMALIST -> Color(0xFFF8FAFC)
    }
    val frameColor = when (boardTheme) {
        BoardTheme.CLASSIC_WOOD -> Color(0xFF8D6E63)
        BoardTheme.NEON_NIGHT -> Color(0xFFFFD700)
        BoardTheme.MINIMALIST -> Color(0xFF64748B)
    }

    // Board background
    drawRect(color = bgColor, size = size)

    val activePlayer = players.getOrNull(currentTurnIndex)

    // 4 Base Yards with active player turn highlight glow
    val baseRects = mapOf(
        PlayerColor.RED to Offset(0f, 0f),
        PlayerColor.GREEN to Offset(cellSize * 9, 0f),
        PlayerColor.YELLOW to Offset(cellSize * 9, cellSize * 9),
        PlayerColor.BLUE to Offset(0f, cellSize * 9)
    )

    baseRects.forEach { (color, topLeft) ->
        val isPlayerActive = (activePlayer?.color == color)
        val yardSize = cellSize * 6

        drawRect(color = color.baseColor, topLeft = topLeft, size = Size(yardSize, yardSize))

        if (isPlayerActive) {
            drawRect(
                color = Color(0xFFFFD700),
                topLeft = topLeft,
                size = Size(yardSize, yardSize),
                style = Stroke(width = activeGlow + 3f)
            )
        }
    }

    drawBaseWhiteBoxes(cellSize)
    drawHomeStretchPaths(cellSize)
    drawEnhancedHomeCenter(cellSize)
    drawTrackGrid(cellSize)
    drawStarSafeSpots(cellSize)

    // Outer Border Frame
    drawRect(
        color = frameColor,
        topLeft = Offset(0f, 0f),
        size = Size(boardSize, boardSize),
        style = Stroke(width = 6f)
    )

    // Draw True 3D Sculpted Pawns
    players.forEach { player ->
        player.tokens.forEach { token ->
            val offset = getTokenCanvasOffset(token, player.color, cellSize)
            if (offset != null) {
                val isValidMove = validMoves.any { it.playerColor == token.playerColor && it.id == token.id }

                // Outer golden pulse ring for valid movable tokens
                if (isValidMove) {
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = cellSize * 0.5f * pulseScale,
                        center = offset,
                        style = Stroke(width = 6f)
                    )
                }

                drawTrue3DPawn(offset, player.color, cellSize)
            }
        }
    }
}

private fun DrawScope.drawTrue3DPawn(center: Offset, color: PlayerColor, cellSize: Float) {
    val bx = center.x
    val by = center.y

    // 1. Soft Ground Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.55f),
        topLeft = Offset(bx - cellSize * 0.35f, by + cellSize * 0.15f),
        size = Size(cellSize * 0.70f, cellSize * 0.28f)
    )

    // 2. Pawn Bottom Base Rim
    drawCircle(
        color = Color.Black.copy(alpha = 0.7f),
        radius = cellSize * 0.34f,
        center = Offset(bx, by + cellSize * 0.08f)
    )
    drawCircle(
        color = color.baseColor,
        radius = cellSize * 0.32f,
        center = Offset(bx, by + cellSize * 0.05f)
    )
    drawCircle(
        color = color.lightColor,
        radius = cellSize * 0.24f,
        center = Offset(bx, by + cellSize * 0.05f)
    )

    // 3. Tapered Stem / Body
    val stemPath = Path().apply {
        moveTo(bx - cellSize * 0.19f, by + cellSize * 0.05f)
        lineTo(bx + cellSize * 0.19f, by + cellSize * 0.05f)
        lineTo(bx + cellSize * 0.11f, by - cellSize * 0.15f)
        lineTo(bx - cellSize * 0.11f, by - cellSize * 0.15f)
        close()
    }
    drawPath(stemPath, color.baseColor)
    drawRect(
        color = color.lightColor,
        topLeft = Offset(bx - cellSize * 0.04f, by - cellSize * 0.12f),
        size = Size(cellSize * 0.08f, cellSize * 0.16f)
    )

    // 4. Collar Ring
    drawCircle(
        color = Color.White,
        radius = cellSize * 0.13f,
        center = Offset(bx, by - cellSize * 0.18f)
    )
    drawCircle(
        color = color.baseColor,
        radius = cellSize * 0.10f,
        center = Offset(bx, by - cellSize * 0.18f)
    )

    // 5. Spherical Head
    val headCenter = Offset(bx, by - cellSize * 0.36f)
    drawCircle(
        color = Color.Black.copy(alpha = 0.4f),
        radius = cellSize * 0.19f,
        center = Offset(headCenter.x + 1f, headCenter.y + 2f)
    )
    drawCircle(
        color = color.baseColor,
        radius = cellSize * 0.19f,
        center = headCenter
    )
    // 3D Specular Highlight on Head Sphere
    drawCircle(
        color = Color.White,
        radius = cellSize * 0.075f,
        center = Offset(headCenter.x - 0.05f * cellSize, headCenter.y - 0.05f * cellSize)
    )
}

private fun DrawScope.drawBaseWhiteBoxes(cellSize: Float) {
    val boxSize = cellSize * 3.8f
    val offsets = listOf(
        Offset(cellSize * 1.1f, cellSize * 1.1f),
        Offset(cellSize * 10.1f, cellSize * 1.1f),
        Offset(cellSize * 10.1f, cellSize * 10.1f),
        Offset(cellSize * 1.1f, cellSize * 10.1f)
    )
    offsets.forEach { topLeft ->
        drawRect(color = Color(0xFFFAFAFA), topLeft = topLeft, size = Size(boxSize, boxSize))
    }
}

private fun DrawScope.drawHomeStretchPaths(cellSize: Float) {
    // Red home stretch (Row 7, cols 1..5)
    for (i in 1..5) {
        drawRect(color = PlayerColor.RED.baseColor, topLeft = Offset(i * cellSize, 7f * cellSize), size = Size(cellSize, cellSize))
    }
    // Green home stretch (Col 7, rows 1..5)
    for (i in 1..5) {
        drawRect(color = PlayerColor.GREEN.baseColor, topLeft = Offset(7f * cellSize, i * cellSize), size = Size(cellSize, cellSize))
    }
    // Yellow home stretch (Row 7, cols 9..13)
    for (i in 9..13) {
        drawRect(color = PlayerColor.YELLOW.baseColor, topLeft = Offset(i * cellSize, 7f * cellSize), size = Size(cellSize, cellSize))
    }
    // Blue home stretch (Col 7, rows 9..13)
    for (i in 9..13) {
        drawRect(color = PlayerColor.BLUE.baseColor, topLeft = Offset(7f * cellSize, i * cellSize), size = Size(cellSize, cellSize))
    }
}

private fun DrawScope.drawEnhancedHomeCenter(cellSize: Float) {
    val cx = cellSize * 7.5f
    val cy = cellSize * 7.5f
    val centerLeft = cellSize * 6f
    val centerTop = cellSize * 6f
    val centerSize = cellSize * 3f

    drawRect(color = Color(0xFF0D1114), topLeft = Offset(centerLeft, centerTop), size = Size(centerSize, centerSize))

    val redPath = Path().apply {
        moveTo(cx, cy)
        lineTo(centerLeft, centerTop)
        lineTo(centerLeft, centerTop + centerSize)
        close()
    }
    drawPath(redPath, PlayerColor.RED.baseColor)

    val greenPath = Path().apply {
        moveTo(cx, cy)
        lineTo(centerLeft, centerTop)
        lineTo(centerLeft + centerSize, centerTop)
        close()
    }
    drawPath(greenPath, PlayerColor.GREEN.baseColor)

    val yellowPath = Path().apply {
        moveTo(cx, cy)
        lineTo(centerLeft + centerSize, centerTop)
        lineTo(centerLeft + centerSize, centerTop + centerSize)
        close()
    }
    drawPath(yellowPath, PlayerColor.YELLOW.baseColor)

    val bluePath = Path().apply {
        moveTo(cx, cy)
        lineTo(centerLeft, centerTop + centerSize)
        lineTo(centerLeft + centerSize, centerTop + centerSize)
        close()
    }
    drawPath(bluePath, PlayerColor.BLUE.baseColor)

    // Center Gold Trophy Crown Badge with multi-layer glow
    drawCircle(color = Color(0xFFFFD700), radius = cellSize * 0.8f, center = Offset(cx, cy))
    drawCircle(color = Color(0xFFFFA000), radius = cellSize * 0.62f, center = Offset(cx, cy))
    drawCircle(color = Color(0xFFFFD700), radius = cellSize * 0.35f, center = Offset(cx, cy))
}

private fun DrawScope.drawTrackGrid(cellSize: Float) {
    val strokeWidth = 1f
    val gridColor = Color(0xFF263238)
    for (i in 0..15) {
        drawLine(gridColor, Offset(i * cellSize, 0f), Offset(i * cellSize, size.height), strokeWidth)
        drawLine(gridColor, Offset(0f, i * cellSize), Offset(size.width, i * cellSize), strokeWidth)
    }
}

private fun DrawScope.drawStarSafeSpots(cellSize: Float) {
    val safeCoords = listOf(
        Offset(1.5f * cellSize, 6.5f * cellSize),
        Offset(8.5f * cellSize, 1.5f * cellSize),
        Offset(13.5f * cellSize, 8.5f * cellSize),
        Offset(6.5f * cellSize, 13.5f * cellSize)
    )
    safeCoords.forEach { center ->
        drawCircle(color = Color(0xFFFFD700), radius = cellSize * 0.28f, center = center)
        drawCircle(color = Color(0xFFFFF176), radius = cellSize * 0.14f, center = center)
    }
}

private fun getTokenCanvasOffset(token: Token, color: PlayerColor, cellSize: Float): Offset? {
    if (token.position == -1) {
        val baseCoords = when (color) {
            PlayerColor.RED -> listOf(Offset(2f, 2f), Offset(4f, 2f), Offset(2f, 4f), Offset(4f, 4f))
            PlayerColor.GREEN -> listOf(Offset(11f, 2f), Offset(13f, 2f), Offset(11f, 4f), Offset(13f, 4f))
            PlayerColor.YELLOW -> listOf(Offset(11f, 11f), Offset(13f, 11f), Offset(11f, 13f), Offset(13f, 13f))
            PlayerColor.BLUE -> listOf(Offset(2f, 11f), Offset(4f, 11f), Offset(2f, 13f), Offset(4f, 13f))
        }
        val coord = baseCoords.getOrElse(token.id) { Offset(2f, 2f) }
        return Offset((coord.x + 0.5f) * cellSize, (coord.y + 0.5f) * cellSize)
    }

    if (token.position in 500..505) {
        val stepsInHome = token.position - 500
        val coord = when (color) {
            PlayerColor.RED -> Offset(1f + stepsInHome, 7f)
            PlayerColor.GREEN -> Offset(7f, 1f + stepsInHome)
            PlayerColor.YELLOW -> Offset(13f - stepsInHome, 7f)
            PlayerColor.BLUE -> Offset(7f, 13f - stepsInHome)
        }
        return Offset((coord.x + 0.5f) * cellSize, (coord.y + 0.5f) * cellSize)
    }

    val mainPathCoords = getMainPathCoordinates()
    val pos = token.position % 52
    val coord = mainPathCoords.getOrElse(pos) { Offset(7f, 7f) }
    return Offset((coord.x + 0.5f) * cellSize, (coord.y + 0.5f) * cellSize)
}

private fun getMainPathCoordinates(): List<Offset> {
    val list = mutableListOf<Offset>()
    for (i in 1..6) list.add(Offset(i.toFloat(), 6f))
    list.add(Offset(6f, 5f))
    list.add(Offset(6f, 4f))
    list.add(Offset(6f, 3f))
    list.add(Offset(6f, 2f))
    list.add(Offset(6f, 1f))
    list.add(Offset(6f, 0f))
    list.add(Offset(7f, 0f))
    list.add(Offset(8f, 0f))
    list.add(Offset(8f, 1f))
    list.add(Offset(8f, 2f))
    list.add(Offset(8f, 3f))
    list.add(Offset(8f, 4f))
    list.add(Offset(8f, 5f))
    for (i in 9..14) list.add(Offset(i.toFloat(), 6f))
    list.add(Offset(14f, 7f))
    list.add(Offset(14f, 8f))
    for (i in 13 downTo 9) list.add(Offset(i.toFloat(), 8f))
    list.add(Offset(8f, 9f))
    list.add(Offset(8f, 10f))
    list.add(Offset(8f, 11f))
    list.add(Offset(8f, 12f))
    list.add(Offset(8f, 13f))
    list.add(Offset(8f, 14f))
    list.add(Offset(7f, 14f))
    list.add(Offset(6f, 14f))
    list.add(Offset(6f, 13f))
    list.add(Offset(6f, 12f))
    list.add(Offset(6f, 11f))
    list.add(Offset(6f, 10f))
    list.add(Offset(6f, 9f))
    for (i in 5 downTo 0) list.add(Offset(i.toFloat(), 8f))
    list.add(Offset(0f, 7f))
    list.add(Offset(0f, 6f))
    list.add(Offset(1f, 6f))
    return list
}

private fun findClickedToken(tapOffset: Offset, boardWidth: Float, players: List<Player>, currentTurnIndex: Int, validMoves: List<Token>): Token? {
    val cellSize = boardWidth / 15f
    val currentPlayer = players.getOrNull(currentTurnIndex) ?: return null

    for (token in currentPlayer.tokens) {
        val tokenOffset = getTokenCanvasOffset(token, currentPlayer.color, cellSize) ?: continue
        val distance = (tapOffset - tokenOffset).getDistance()
        if (distance <= cellSize * 0.6f) {
            if (validMoves.any { it.playerColor == token.playerColor && it.id == token.id }) {
                return token
            }
        }
    }
    return null
}
