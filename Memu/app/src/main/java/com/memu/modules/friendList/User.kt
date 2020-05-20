package com.memu.modules.friendList

import com.memu.modules.userSearch.Photo

data class User(
    val distance: String = "",
    val freind_id: String = "",
    val id: String = "",
    val lattitude: String = "",
    val longitude: String = "",
    val name: String = "",
    val photo: Photo = Photo()
)