package com.memu.modules.endnavigation

data class EndNavigationResponse(
    val message: String,
    val status: String,
    val trip_details: TripDetails
)