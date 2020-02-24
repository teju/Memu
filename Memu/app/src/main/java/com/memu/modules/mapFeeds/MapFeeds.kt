package com.memu.modules.mapFeeds

data class MapFeeds(
    val map_feeds: List<MapFeed> = listOf(),
    val message: String = "",
    val status: String = ""
)