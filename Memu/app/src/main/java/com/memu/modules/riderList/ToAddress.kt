package com.memu.modules.riderList

data class ToAddress(
    val address_line1: String,
    val address_line2: Any,
    val city: String,
    val country: String,
    val formatted_address: String,
    val lattitude: String,
    val longitude: String,
    val pincode: Any,
    val state: String
)