package com.memu.modules.matched_buddies

import com.memu.modules.completedRides.MatchedBudy

data class MatchedBuddiesResponse(

    val status: String,
    val message: String,
    val matched_budies: List<MatchedBudy> = listOf()

    )