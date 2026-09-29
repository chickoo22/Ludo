package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.Token

@Composable
fun LudoBoardView(
    players: List<Player>,
    currentTurnPlayerIndex: Int,
    onTokenClick: (Token) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(players, currentTurnPlayerIndex) {
                    detectTapGestures { offset ->
                        // Detect token click based on touch offset on canvas
                        val clickedToken = findClickedToken(offset, size.width.toFloat(), players, currentTurnPlayerIndex)
                        if (clickedToken != null) {
                            onTokenClick(clickedToken)
                        }
                    }
                }
        ) {
            drawLudoBoard(players)
        }
    }
}

private fun DrawScope.drawLudoBoard(players: List<Player>) {
    val boardSize = size.width
    val cellSize = boardSize / 15f

    // Background
    drawRect(color = Color(0xFFFFF8E1), size = size)

    // 4 Base Yards (Red: top-left, Green: top-right, Yellow: bottom-right, Blue: bottom-left)
    // Red Base (0,0 to 6,6)
    drawRect(color = PlayerColor.RED.baseColor, topLeft = Offset(0f, 0f), size = Size(cellSize * 6, cellSize * 6))
    // Green Base (9,0 to 15,6)
    drawRect(color = PlayerColor.GREEN.baseColor, topLeft = Offset(cellSize * 9, 0f), size = Size(cellSize * 6, cellSize * 6))
    // Yellow Base (9,9 to 15,15)
    drawRect(color = PlayerColor.YELLOW.baseColor, topLeft = Offset(cellSize * 9, cellSize * 9), size = Size(cellSize * 6, cellSize * 6))
    // Blue Base (0,9 to 6,15)
    drawRect(color = PlayerColor.BLUE.baseColor, topLeft = Offset(0f, cellSize * 9), size = Size(cellSize * 6, cellSize * 6))

    // Inner white boxes for bases
    drawBaseWhiteBoxes(cellSize)

    // Home Center (Triangle home)
    drawHomeCenter(cellSize)

    // Tracks / paths grid lines
    drawTrackGrid(cellSize)

    // Draw Tokens for all players
    players.forEach { player ->
        player.tokens.forEach { token ->
            val offset = getTokenCanvasOffset(token, player.color, cellSize)
            if (offset != null) {
                drawCircle(color = player.color.baseColor, radius = cellSize * 0.35f, center = offset)
                drawCircle(color = Color.White, radius = cellSize * 0.25f, center = offset)
                // Draw token ID / initial
                // (Omitted text for simplicity in canvas, or draw small inner dot)
                drawCircle(color = player.color.baseColor, radius = cellSize * 0.15f, center = offset)
            }
        }
    }
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
        drawRect(color = Color.White, topLeft = topLeft, size = Size(boxSize, boxSize))
    }
}

private fun DrawScope.drawHomeCenter(cellSize: Float) {
    val centerLeft = cellSize * 6f
    val centerTop = cellSize * 6f
    val centerSize = cellSize * 3f
    drawRect(color = Color(0xFFCFD8DC), topLeft = Offset(centerLeft, centerTop), size = Size(centerSize, centerSize))

    // 4 triangle home zones inside center
    // Red triangle from left
    drawRect(color = PlayerColor.RED.baseColor, topLeft = Offset(centerLeft, centerTop + cellSize), size = Size(cellSize, cellSize))
    // Green triangle from top
    drawRect(color = PlayerColor.GREEN.baseColor, topLeft = Offset(centerLeft + cellSize, centerTop), size = Size(cellSize, cellSize))
    // Yellow triangle from right
    drawRect(color = PlayerColor.YELLOW.baseColor, topLeft = Offset(centerLeft + cellSize * 2, centerTop + cellSize), size = Size(cellSize, cellSize))
    // Blue triangle from bottom
    drawRect(color = PlayerColor.BLUE.baseColor, topLeft = Offset(centerLeft + cellSize, centerTop + cellSize * 2), size = Size(cellSize, cellSize))
}

private fun DrawScope.drawTrackGrid(cellSize: Float) {
    val strokeWidth = 1.5f
    val gridColor = Color(0xFFB0BEC5)
    // Draw outer borders and internal grid lines
    for (i in 0..15) {
        drawLine(gridColor, Offset(i * cellSize, 0f), Offset(i * cellSize, size.height), strokeWidth)
        drawLine(gridColor, Offset(0f, i * cellSize), Offset(size.width, i * cellSize), strokeWidth)
    }
}

// Map board position index (0-51 main path, -1 base, 500-505 home stretch) to canvas offset (x, y)
private fun getTokenCanvasOffset(token: Token, color: PlayerColor, cellSize: Float): Offset? {
    if (token.position == -1) {
        // Base coordinates
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

    // Main path 0 to 51 coordinates mapping
    val mainPathCoords = getMainPathCoordinates()
    val pos = token.position % 52
    val coord = mainPathCoords.getOrElse(pos) { Offset(7f, 7f) }
    return Offset((coord.x + 0.5f) * cellSize, (coord.y + 0.5f) * cellSize)
}

private fun getMainPathCoordinates(): List<Offset> {
    // 52 standard Ludo track coordinates on 15x15 grid
    val list = mutableListOf<Offset>()
    // Red start (6, 1) going right
    for (i in 1..6) list.add(Offset(i.toFloat(), 6f))
    list.add(Offset(6f, 5f))
    list.add(Offset(6f, 4f))
    list.add(Offset(6f, 3f))
    list.add(Offset(6f, 2f))
    list.add(Offset(6f, 1f))
    list.add(Offset(6f, 0f))
    list.add(Offset(7f, 0f))
    list.add(Offset(8f, 0f))
    // Green start
    list.add(Offset(8f, 1f))
    list.add(Offset(8f, 2f))
    list.add(Offset(8f, 3f))
    list.add(Offset(8f, 4f))
    list.add(Offset(8f, 5f))
    for (i in 9..14) list.add(Offset(i.toFloat(), 6f))
    list.add(Offset(14f, 7f))
    list.add(Offset(14f, 8f))
    // Yellow start
    for (i in 13 downTo 9) list.add(Offset(i.toFloat(), 8f))
    list.add(Offset(8f, 9f))
    list.add(Offset(8f, 10f))
    list.add(Offset(8f, 11f))
    list.add(Offset(8f, 12f))
    list.add(Offset(8f, 13f))
    list.add(Offset(8f, 14f))
    list.add(Offset(7f, 14f))
    list.add(Offset(6f, 14f))
    // Blue start
    list.add(Offset(6f, 13f))
    list.add(Offset(6f, 12f))
    list.add(Offset(6f, 11f))
    list.add(Offset(6f, 10f))
    list.add(Offset(6f, 9f))
    for (i in 5 downTo 0) list.add(Offset(i.toFloat(), 8f))
    list.add(Offset(0f, 7f))
    list.add(Offset(0f, 6f))
    list.add(Offset(1f, 6f)) // completing 52
    return list
}

private fun findClickedToken(tapOffset: Offset, boardWidth: Float, players: List<Player>, currentTurnIndex: Int): Token? {
    val cellSize = boardWidth / 15f
    val currentPlayer = players.getOrNull(currentTurnIndex) ?: return null

    for (token in currentPlayer.tokens) {
        val tokenOffset = getTokenCanvasOffset(token, currentPlayer.color, cellSize) ?: continue
        val distance = (tapOffset - tokenOffset).getDistance()
        if (distance <= cellSize * 0.6f) {
            return token
        }
    }
    return null
}
