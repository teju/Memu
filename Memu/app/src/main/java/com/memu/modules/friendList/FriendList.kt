package com.memu.modules.friendList

data class FriendList(
    val count: Int = 0,
    val status: String = "",
    val user_list: List<User> = listOf()
)