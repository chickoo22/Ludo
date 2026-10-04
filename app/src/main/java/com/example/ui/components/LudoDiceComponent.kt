package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun LudoDiceComponent(
    diceValue: Int?,
    isRolling: Boolean,
    onRollRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dice_physics")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -360f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dice_rotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(85, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dice_scale"
    )

    val displayValue = if (isRolling) Random.nextInt(1, 7) else (diceValue ?: 6)

    Box(
        modifier = modifier
            .size(80.dp)
            .scale(if (isRolling) scale else 1f)
            .rotate(if (isRolling) rotation else 0f)
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFFFD700))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFCFD8DC), Color(0xFF90A4AE))
                )
            )
            .border(4.dp, if (isRolling) Color(0xFFFFD700) else Color(0xFF546E7A), RoundedCornerShape(20.dp))
            .clickable(enabled = !isRolling) {
                onRollRequested()
            },
        contentAlignment = Alignment.Center
    ) {
        DiceFace3D(value = displayValue)
    }
}

@Composable
fun DiceFace3D(value: Int) {
    val dotColor = Color(0xFF11171A)
    val dotHighlight = Color(0xFF546E7A)
    val dotSize = 12.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        @Composable
        fun Pip() {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(dotHighlight, dotColor)
                        )
                    )
            )
        }

        when (value) {
            1 -> {
                Pip()
            }
            2 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Pip()
                    Pip()
                }
            }
            3 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.align(Alignment.Start)) { Pip() }
                    Box(modifier = Modifier.align(Alignment.CenterHorizontally)) { Pip() }
                    Box(modifier = Modifier.align(Alignment.End)) { Pip() }
                }
            }
            4 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Pip(); Pip()
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Pip(); Pip()
                    }
                }
            }
            5 -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.align(Alignment.TopStart)) { Pip() }
                    Box(modifier = Modifier.align(Alignment.TopEnd)) { Pip() }
                    Box(modifier = Modifier.align(Alignment.Center)) { Pip() }
                    Box(modifier = Modifier.align(Alignment.BottomStart)) { Pip() }
                    Box(modifier = Modifier.align(Alignment.BottomEnd)) { Pip() }
                }
            }
            6 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Pip(); Pip()
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Pip(); Pip()
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Pip(); Pip()
                    }
                }
            }
        }
    }
}
