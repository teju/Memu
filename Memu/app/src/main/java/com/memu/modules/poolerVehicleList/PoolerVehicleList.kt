package com.memu.modules.poolerVehicleList

data class PoolerVehicleList(
    val message: String = "",
    val status: String = "",
    val vehicle_list: List<Vehicle> = listOf()
)