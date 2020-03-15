package com.memu.modules.completedRides

data class Completed(
    val date: String = "",
    val from_address: FromAddress = FromAddress(),
    val id: String = "",
    val status: String = "",
    val time: String = "",
    val to_address: ToAddress = ToAddress()
)