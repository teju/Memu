package com.memu.modules.completedRides

data class CompletedScheduled(
    val completed_list: List<Completed> = listOf(),
    val scheduled_list: List<Completed> = listOf(),
    val status: String = ""
)