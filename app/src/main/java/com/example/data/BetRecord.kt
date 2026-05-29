package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bet_records")
data class BetRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "SPORTS", "SLOTS", "CRASH", "ROULETTE"
    val selection: String,
    val stakeAmount: Double,
    val odd: Double,
    val potentialPayout: Double,
    val isSettled: Boolean = false,
    val outcome: String = "PENDING", // "PENDING", "WON", "LOST"
    val timestamp: Long = System.currentTimeMillis()
)
