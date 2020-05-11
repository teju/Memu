package com.memu.modules.profileWall

data class Activity(
    val address: Address = Address(),
    val date_time: String = "",
    val from_address: FromAddress = FromAddress(),
    val id: String = "",
    val image: Image = Image(),
    val likes: Int = 0,
    val logo: String = "",
    val message: String = "",
    val shares: Int = 0,
    val to_address: ToAddress = ToAddress(),
    val type: String = ""
)