package com.memu.modules.userDetails

data class Vehicle(
    var id: String = "",
    var vehicle_brand: String = "",
    var vehicle_model_type: String = "",
    var vehicle_name: String = "",
    var vehicle_no: String = "",
    var showCancel: Boolean = false,
    var vehicle_type: Int = 0
)