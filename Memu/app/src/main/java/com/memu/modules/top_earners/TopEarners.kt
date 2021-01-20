package com.memu.modules.top_earners

data class TopEarners(
    val message: String,
    val status: String,
    val city_earners: List<CityEarner> = listOf(),
    val friend_earners: List<CityEarner> = listOf()
)