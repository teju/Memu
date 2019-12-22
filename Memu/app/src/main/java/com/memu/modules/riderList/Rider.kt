package com.memu.modules.riderList

data class Rider(
    val date: String,
    val email: String,
    val from_address: FromAddress,
    val mobile: String,
    val name: String,
    val no_of_kms: Any,
    val no_of_seats: String,
    val status: String,
    val time: String,
    val to_address: ToAddress,
    val trip_rider_id: String,
    val user_id: String
)