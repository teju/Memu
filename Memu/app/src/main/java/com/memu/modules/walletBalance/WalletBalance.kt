package com.memu.modules.checksum

data class WalletBalance(
    val balance: String,
    val referral_balance: Double = 0.0,
    val status: String
)