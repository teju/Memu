package com.memu.modules.googleMaps

data class Bounds(
    val northeast: Northeast = Northeast(),
    val southwest: Southwest = Southwest()
)