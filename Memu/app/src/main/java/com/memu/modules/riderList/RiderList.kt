package com.memu.modules.riderList

data class RiderList(
    val count: String,
    val pooler_list: List<Rider>?,
    val rider_list: List<Rider>?,
    val status: String
)