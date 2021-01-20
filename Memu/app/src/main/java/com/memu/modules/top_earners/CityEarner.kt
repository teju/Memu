package com.memu.modules.top_earners

import com.memu.modules.userMainData.Photo

data class CityEarner(
    val city: String = "",
    val name: String = "",
    val total_points: String = "",
    val user_id: String = "",
    val photo: Photo
)