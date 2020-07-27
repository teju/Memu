package com.memu.modules.riderList

import com.memu.modules.UserSignup.Photo

data class Rider(
    val date: String?,
    val email: String?,
    val from_address: FromAddress?,
    val mobile: String?,
    val name: String?,
    val no_of_kms: Any?,
    val no_of_seats: String?,
    val status: String?,
    val time: String?,
    val to_address: ToAddress?,
    val trip_rider_id: String?,
    val route_per: String?,
    val id: String?,
    val photo: Photo = Photo(),
    val user_id: String?
)