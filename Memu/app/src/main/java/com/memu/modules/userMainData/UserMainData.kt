package com.memu.modules.userMainData

data class UserMainData(
    val followers: Int = 0,
    val followings: Int = 0,
    val friends: Int = 0,
    val id: Int = 0,
    val likes: Int = 0,
    val messages: Int = 0,
    val name: String = "",
    val distance_shared: String = "",
    val photo: Photo = Photo(),
    val posts: Int = 0,
    val rides_shared: Int = 0,
    val rating: String = ""
)