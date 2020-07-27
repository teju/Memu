package com.memu.modules.userSearch

import com.memu.modules.friendList.User

data class UserSearch(
    val count: Int = 0,
    val status: String = "",
    val user_list: List<User> = listOf()
)