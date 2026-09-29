package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_history")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val mode: String,
    val winnerName: String,
    val playersCount: Int,
    val durationSeconds: Long,
    val timestamp: Long = System.currentTimeMillis()
)
