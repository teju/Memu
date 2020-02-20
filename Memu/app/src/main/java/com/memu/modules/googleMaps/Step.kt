package com.memu.modules.googleMaps

data class Step(
    val distance: Distance = Distance(),
    val duration: Duration = Duration(),
    val end_location: EndLocation = EndLocation(),
    val html_instructions: String = "",
    val maneuver: String = "",
    val polyline: Polyline = Polyline(),
    val start_location: StartLocation = StartLocation(),
    val travel_mode: String = ""
)