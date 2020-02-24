package com.iapps.deera.etc

interface OTPExpiryListener {

    fun onExpiry()
    fun onStillValid()
    fun onFail()
}
