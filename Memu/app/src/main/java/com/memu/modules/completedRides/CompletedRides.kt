package com.memu.modules.completedRides

data class CompletedRides(
    val completed_list: List<Completed> = listOf(),
    val status: String = ""
)