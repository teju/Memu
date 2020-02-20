package com.memu.modules.googleMaps

data class Route(
    val bounds: Bounds = Bounds(),
    val copyrights: String = "",
    val legs: List<Leg> = listOf(),
    val overview_polyline: OverviewPolyline = OverviewPolyline(),
    val summary: String = "",
    val warnings: List<Any> = listOf(),
    val waypoint_order: List<Any> = listOf()
)