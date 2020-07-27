package com.memu.modules.completedRides

data class ToAddress(
    val address_line1: String = "",
    val address_line2: Any? = Any(),
    val city: String = "",
    val country: String = "",
    val formatted_address: String = "",
    val lattitude: String = "",
    val longitude: String = "",
    val pincode: Any? = Any(),
    val state: String = ""
)