package com.memu.modules.profileWall

data class ProfileWall(
    val activities: List<Activity> = listOf(),
    val message: String = "",
    val status: String = "",
    val total_count: String = ""
)