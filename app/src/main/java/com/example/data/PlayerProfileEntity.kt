package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profiles")
data class PlayerProfileEntity(
    @PrimaryKey val id: String = "default_user",
    val playerName: String = "Player 1",
    val avatarEmoji: String = "🦁",
    val preferredColor: String = "RED",
    val coins: Long = 2500,
    val gems: Int = 120,
    val lastDailyBonusTime: Long = 0L,
    val dailyBonusStreak: Int = 0
)
