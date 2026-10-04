package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val speedX: Float,
    val speedY: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiOverlayView(
    modifier: Modifier = Modifier
) {
    val particles = remember {
        val colors = listOf(
            Color(0xFFFFD700), Color(0xFFFF5722), Color(0xFF4CAF50),
            Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFF00BCD4), Color(0xFFFFEB3B)
        )
        List(120) {
            ConfettiParticle(
                x = Random.nextFloat() * 1000f,
                y = Random.nextFloat() * -500f - 100f,
                color = colors.random(),
                size = Random.nextFloat() * 12f + 8f,
                speedX = Random.nextFloat() * 4f - 2f,
                speedY = Random.nextFloat() * 8f + 4f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 10f - 5f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            val currentX = (p.x + p.speedX * (progress % 1000f)) % width
            val currentY = (p.y + p.speedY * (progress % 1000f)) % height

            drawRect(
                color = p.color,
                topLeft = Offset(currentX, currentY),
                size = androidx.compose.ui.geometry.Size(p.size, p.size * 0.6f)
            )
        }
    }
}
