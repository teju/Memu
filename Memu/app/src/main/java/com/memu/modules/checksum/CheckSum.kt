package com.memu.modules.checksum

data class CheckSum(
    val generate_signature: String,
    val status: String,
    val verify_signature: Boolean
)