package com.iapps.gon.etc.callback

import com.memu.modules.completedRides.Completed

interface RecursiveListener {
    fun onButtonClicked(which: Completed)
}