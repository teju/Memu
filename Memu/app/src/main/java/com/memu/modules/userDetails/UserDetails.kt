package com.memu.modules.userDetails

data class UserDetails(
    val address: Address = Address(),
    val status: String = "",
    val message: String = "",
    val personal_details: PersonalDetails = PersonalDetails(),
    val vehicle: List<Vehicle> = listOf()

)