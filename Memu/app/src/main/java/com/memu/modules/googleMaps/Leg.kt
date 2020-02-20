package com.memu.modules.googleMaps

data class Leg(
    val distance: Distance = Distance(),
    val duration: Duration = Duration(),
    val end_address: String = "",
    val end_location: EndLocation = EndLocation(),
    val start_address: String = "",
    val start_location: StartLocation = StartLocation(),
    val steps: List<Step> = listOf(),
    val traffic_speed_entry: List<Any> = listOf(),
    val via_waypoint: List<Any> = listOf()
)