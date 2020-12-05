package com.memu.modules.completedRides

data class Completed(
    val coins_earned: String = "",
    val coins_spent: String = "",
    val date: String = "",
    val from_address: FromAddress = FromAddress(),
    val id: String = "",
    val matched_budies: List<MatchedBudy> = listOf(),
    val status: String = "",
    val time: String = "",
    val days: String = "",
    val type: String = "",
    val vehicle_id: String = "",
    val is_recurring_ride: String = "",
    val no_of_seats: String = "",
    val to_address: ToAddress = ToAddress(),
    val trip_id: String = ""

    )