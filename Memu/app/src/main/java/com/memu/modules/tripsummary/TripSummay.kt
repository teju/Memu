package com.memu.modules.tripsummary

data class TripSummay(
    val distance_travelled: Int = 0,
    val message: String = "",
    val money_earned_spent: Int = 0,
    val reputation_coin: Int = 0,
    val status: String = "",
    val time_taken: Int = 0
)