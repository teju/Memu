package com.memu.modules.notification

data class NotificationResponse(
    val booked_date: String = "",
    val booked_time: String = "",
    val from_address: FromAddress,
    val name: String = "",
    val no_of_seats: String = "",
    val status: String = "",
    val to_address: ToAddress ,
    val trip_id: String = "",
    val trip_rider_id: String = "",
    val type: String = "",
    val isAccept: Boolean = false,
    val user_id: String = "",
    val freind_id: String = ""
)