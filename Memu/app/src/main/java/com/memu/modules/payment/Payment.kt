package com.memu.modules.checksum

data class Payment(
    val wallet_balance: String,
    val invoice_id: String,
    val message: String,
    val status: String
)