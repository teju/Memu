package com.memu.modules

data class GenericResponse(
    val message: String,
    val is_redirect: Boolean,
    val status: String
)