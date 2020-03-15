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
    val to_address: ToAddress = ToAddress()
)