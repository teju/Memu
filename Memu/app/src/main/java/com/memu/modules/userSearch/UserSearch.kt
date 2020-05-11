package com.memu.modules.userSearch

data class UserSearch(
    val count: Int = 0,
    val status: String = "",
    val user_list: List<User> = listOf()
)