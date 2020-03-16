package com.memu.modules.UserSignup

data class UserSignUp(
    val access_token: String,
    val email: String,
    val message: String,
    val mobile: String,
    val name: String,
    val role_type: String,
    val status: String,
    val photo: Photo,
    val user_id: Int
)