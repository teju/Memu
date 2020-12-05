package com.iapps.gon.etc.callback

import com.memu.modules.checksum.WalletBalance

interface WalletBalanceListener {
    fun walletBalanceResponse(balance: WalletBalance)
}