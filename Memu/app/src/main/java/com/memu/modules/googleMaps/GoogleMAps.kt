package com.memu.modules.googleMaps

data class GoogleMAps(
    val geocoded_waypoints: List<GeocodedWaypoint> = listOf(),
    val routes: List<Route> = listOf(),
    val status: String = ""
)