package com.memu.modules.endnavigationid

data class EndNavigationIDResponse(
    val message: String,
    val status: String,
    val trip_details: TripDetails
)