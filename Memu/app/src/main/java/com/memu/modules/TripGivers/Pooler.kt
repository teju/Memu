package com.memu.modules.TripGivers

data class Pooler(
    val date: String,
    val email: String,
    val from_address: FromAddress,
    val mobile: String,
    val name: String,
    val status: String,
    val time: String,
    val to_address: ToAddress,
    val trip_rider_id: String,
    val user_id: String
)