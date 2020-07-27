package com.memu.modules.googleMaps

data class GeocodedWaypoint(
    val geocoder_status: String = "",
    val place_id: String = "",
    val types: List<String> = listOf()
)