package com.memu.modules.senRequest

data class SendRequest(
    val message: String = "",
    val status: String = "",
    val user_detail: UserDetail = UserDetail()
)